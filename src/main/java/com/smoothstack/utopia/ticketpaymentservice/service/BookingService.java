package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.shared.mailmodels.BillingMailModel;
import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.BookingAgent;
import com.smoothstack.utopia.shared.model.BookingGuest;
import com.smoothstack.utopia.shared.model.BookingPayment;
import com.smoothstack.utopia.shared.model.BookingUser;
import com.smoothstack.utopia.shared.model.Flight;
import com.smoothstack.utopia.shared.model.Passenger;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.service.EmailService;
import com.smoothstack.utopia.ticketpaymentservice.Bill;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingAgentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingGuestDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingPaymentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingUserDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.FlightDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.PassengerDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.UserDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.BaseBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateAgentBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateUserBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightFullException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentProcessingFailedException;
import com.smoothstack.utopia.ticketpaymentservice.exception.UserNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Rob Maes
 * Mar 17 2021
 */
@Service
public class BookingService {

  private final StripeService stripeService;
  private final EmailService emailService;
  private final BookingDao bookingDao;
  private final FlightDao flightDao;
  private final PassengerDao passengerDao;
  private final BookingGuestDao bookingGuestDao;
  private final BookingPaymentDao bookingPaymentDao;
  private final UserDao userDao;
  private final BookingUserDao bookingUserDao;
  private final BookingAgentDao bookingAgentDao;

  @Autowired
  public BookingService(
    StripeService stripeService,
    EmailService emailService,
    BookingDao bookingDao,
    FlightDao flightDao,
    PassengerDao passengerDao,
    BookingGuestDao bookingGuestDao,
    BookingPaymentDao bookingPaymentDao,
    UserDao userDao,
    BookingUserDao bookingUserDao,
    BookingAgentDao bookingAgentDao
  ) {
    this.stripeService = stripeService;
    this.emailService = emailService;
    this.bookingDao = bookingDao;
    this.flightDao = flightDao;
    this.passengerDao = passengerDao;
    this.bookingGuestDao = bookingGuestDao;
    this.bookingPaymentDao = bookingPaymentDao;
    this.userDao = userDao;
    this.bookingUserDao = bookingUserDao;
    this.bookingAgentDao = bookingAgentDao;
  }

  public List<Booking> getAllBookings() {
    return bookingDao.findAll();
  }

  public Booking getBooking(Long bookingId) {
    return bookingDao
      .findById(bookingId)
      .orElseThrow(BookingNotFoundException::new);
  }

  public Booking getBookingByConfirmationCode(String confirmationCode) {
    return bookingDao
      .findBookingByConfirmationCode(confirmationCode)
      .orElseThrow(BookingNotFoundException::new);
  }

  public List<Booking> getBookingsByUser(Long userId) {
    User user = userDao
      .findById(userId)
      .orElseThrow(UserNotFoundException::new);
    return bookingDao.findBookingByBookingUser_User(user);
  }

  @Transactional
  public void cancelBooking(Long bookingId) {
    Booking booking = bookingDao
      .findById(bookingId)
      .orElseThrow(BookingNotFoundException::new);
    BookingPayment payment = booking.getBookingPayment();
    stripeService.refundCharge(payment.getStripeId());
    payment.setRefunded(true);
    bookingPaymentDao.save(payment);
    booking.setIsActive(false);
    bookingDao.save(booking);
  }

  @Transactional
  protected Bill createBooking(BaseBookingDto baseBookingDto) {
    int numPassengers = baseBookingDto.getPassengers().size();
    Set<Flight> flights = new HashSet<>();
    Set<Passenger> passengers = new HashSet<>();

    // take the list of flight ids and find each flight object
    baseBookingDto
      .getFlightIds()
      .forEach(
        flightId -> {
          Flight flight = flightDao
            .findById(flightId)
            .orElseThrow(FlightNotFoundException::new);
          // if trying to book more passengers than flight has available seats, throw error
          if (flight.getAvailableSeats() < numPassengers) {
            throw new FlightFullException();
          }
          flights.add(flight);
        }
      );

    // calculate total price based on seat prices and number of passengers
    Bill bill = calculateTotal(flights, numPassengers);

    // attempt to charge card for total price and save payment id
    String paymentId = "";
    try {
      paymentId =
        stripeService.chargeCreditCard(
          baseBookingDto.getStripeToken(),
          bill.getTotal()
        );
    } catch (Exception e) {
      throw new PaymentProcessingFailedException();
    }

    /*
      Create new booking
      set as active,
      generate UUID for confirmation code,
      attach specified flights to booking
     */
    Booking booking = new Booking();
    booking.setIsActive(true);
    booking.setConfirmationCode(UUID.randomUUID().toString());
    booking.setFlights(flights);
    bookingDao.save(booking);

    /*
      Create booking payment
      Associate with booking
      Set refunded to false
      Add Stripe transaction ID
     */
    BookingPayment bookingPayment = new BookingPayment();
    bookingPayment.setBookingId(booking.getId());
    bookingPayment.setRefunded(false);
    bookingPayment.setStripeId(paymentId);
    bookingPaymentDao.save(bookingPayment);

    // Associate each flight with the new booking
    flights.forEach(
      flight -> {
        flight.getBookings().add(booking);
        flightDao.save(flight);
      }
    );

    // create a new passenger for each passenger and associate it with the booking
    baseBookingDto
      .getPassengers()
      .forEach(
        createPassengerDto -> {
          Passenger passenger = new Passenger();
          passenger.setGivenName(createPassengerDto.getGivenName());
          passenger.setFamilyName(createPassengerDto.getFamilyName());
          passenger.setGender(createPassengerDto.getGender());
          passenger.setDob(createPassengerDto.getDob());
          passenger.setAddress(createPassengerDto.getAddress());
          passenger.setBooking(booking);
          passengerDao.save(passenger);
          passengers.add(passenger);
        }
      );

    // associate the passengers with the booking
    booking.setPassengers(passengers);
    // update the booking
    bookingDao.save(booking);
    bill.setBooking(booking);
    return bill;
  }

  @Transactional
  public Booking createGuestBooking(
    CreateGuestBookingDto createGuestBookingDto
  ) {
    Bill bill = createBooking(createGuestBookingDto);
    Booking booking = bill.getBooking();
    BookingGuest bookingGuest = new BookingGuest();
    bookingGuest.setBookingId(booking.getId());
    bookingGuest.setContactEmail(createGuestBookingDto.getGuestEmail());
    bookingGuest.setContactPhone(createGuestBookingDto.getGuestPhone());
    bookingGuestDao.save(bookingGuest);
    emailBill(
      bill,
      createGuestBookingDto.getGuestEmail(),
      booking.getConfirmationCode(),
      "",
      ""
    );
    return bookingDao.findById(booking.getId()).get();
  }

  @Transactional
  public Booking createUserBooking(CreateUserBookingDto createUserBookingDto) {
    Bill bill = createBooking(createUserBookingDto);
    Booking booking = bill.getBooking();
    BookingUser bookingUser = new BookingUser();
    User user = userDao
      .findById(createUserBookingDto.getUserId())
      .orElseThrow(UserNotFoundException::new);
    bookingUser.setUser(user);
    bookingUser.setBookingId(booking.getId());
    bookingUserDao.save(bookingUser);
    emailBill(
      bill,
      user.getEmail(),
      booking.getConfirmationCode(),
      user.getGivenName(),
      user.getFamilyName()
    );
    return bookingDao.findById(booking.getId()).get();
  }

  @Transactional
  public Booking createAgentBooking(
    CreateAgentBookingDto createAgentBookingDto
  ) {
    Bill bill = createBooking(createAgentBookingDto);
    Booking booking = bill.getBooking();
    BookingAgent bookingAgent = new BookingAgent();
    User user = userDao
      .findById(createAgentBookingDto.getAgentId())
      .orElseThrow(UserNotFoundException::new);
    bookingAgent.setAgent(user);
    bookingAgent.setBookingId(booking.getId());
    bookingAgentDao.save(bookingAgent);
    return bookingDao.findById(booking.getId()).get();
  }

  private void emailBill(
    Bill bill,
    String email,
    String confirmationCode,
    String givenName,
    String familyName
  ) {
    BillingMailModel model = new BillingMailModel();
    model.setTotalAmount(bill.getFormattedTotal());
    model.setItems(bill.getLineItems());
    model.setGivenName(givenName);
    model.setFamilyName(familyName);
    model.setConfirmationCode(confirmationCode);
    emailService.send(email, EmailService.MailTemplate.BILLING, model);
  }

  private Bill calculateTotal(Set<Flight> flights, int passengerCount) {
    Bill bill = new Bill();
    // loop through each flight
    for (Flight flight : flights) {
      // add the flight to the bill
      bill.addLineItem(flight, passengerCount);
    }
    // add sales tax to the bill
    bill.addTaxLineItem(0.0825f);
    // return bill
    return bill;
  }
}
