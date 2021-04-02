package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.BookingGuest;
import com.smoothstack.utopia.shared.model.BookingPayment;
import com.smoothstack.utopia.shared.model.Flight;
import com.smoothstack.utopia.shared.model.Passenger;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingGuestDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingPaymentDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.FlightDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.PassengerDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateAgentBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateUserBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.UpdateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentException;
import com.stripe.model.Charge;
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
  private final BookingDao bookingDao;
  private final FlightDao flightDao;
  private final PassengerDao passengerDao;
  private final BookingGuestDao bookingGuestDao;
  private final BookingPaymentDao bookingPaymentDao;

  @Autowired
  public BookingService(
    StripeService stripeService,
    BookingDao bookingDao,
    FlightDao flightDao,
    PassengerDao passengerDao,
    BookingGuestDao bookingGuestDao,
    BookingPaymentDao bookingPaymentDao
  ) {
    this.stripeService = stripeService;
    this.bookingDao = bookingDao;
    this.flightDao = flightDao;
    this.passengerDao = passengerDao;
    this.bookingGuestDao = bookingGuestDao;
    this.bookingPaymentDao = bookingPaymentDao;
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

  @Transactional
  public Booking createAgentBooking(
    CreateAgentBookingDto createAgentBookingDto
  ) {
    return new Booking();
  }

  @Transactional
  public Booking createUserBooking(CreateUserBookingDto createUserBookingDto) {
    return new Booking();
  }

  private Float calculateTotal(Set<Flight> flights, int passengerCount) {
    Float subtotal = 0f;
    for (Flight flight : flights) {
      System.out.println(flight.getSeatPrice());
      System.out.println(passengerCount);
      subtotal += flight.getSeatPrice() * passengerCount;
    }
    Float tax = 0.0825f * subtotal;
    return subtotal + tax;
  }

  @Transactional
  public Booking createGuestBooking(
    CreateGuestBookingDto createGuestBookingDto
  ) {
    Set<Flight> flights = new HashSet<>();
    Set<Passenger> passengers = new HashSet<>();

    createGuestBookingDto
      .getFlightIds()
      .forEach(
        flightId -> {
          Flight flight = flightDao
            .findById(flightId)
            .orElseThrow(FlightNotFoundException::new);
          flights.add(flight);
        }
      );

    Float total = calculateTotal(
      flights,
      createGuestBookingDto.getPassengers().size()
    );

    String paymentId = "";

    try {
      Charge charge = stripeService.chargeCreditCard(
        createGuestBookingDto.getStripeToken(),
        total
      );
      paymentId = charge.getId();
    } catch (Exception e) {
      throw new PaymentException();
    }

    Booking booking = new Booking();
    booking.setIsActive(true);
    booking.setConfirmationCode(UUID.randomUUID().toString());
    booking.setFlights(flights);
    bookingDao.save(booking);

    BookingPayment bookingPayment = new BookingPayment();
    bookingPayment.setBookingId(booking.getId());
    bookingPayment.setRefunded(false);
    bookingPayment.setStripeId(paymentId);
    bookingPaymentDao.save(bookingPayment);

    BookingGuest guestBooking = new BookingGuest();
    guestBooking.setBookingId(booking.getId());
    guestBooking.setContactEmail(createGuestBookingDto.getGuestEmail());
    guestBooking.setContactPhone(createGuestBookingDto.getGuestPhone());
    bookingGuestDao.save(guestBooking);

    flights.forEach(
      flight -> {
        flight.getBookings().add(booking);
        flightDao.save(flight);
      }
    );

    createGuestBookingDto
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

    booking.setPassengers(passengers);
    bookingDao.save(booking);
    return booking;
  }

  public void updateBooking(
    Long bookingId,
    UpdateBookingDto updateBookingDto
  ) {}

  public void deleteBooking(Long bookingId) {}
}
