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
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightFullException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentProcessingFailedException;
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
  public Booking createGuestBooking(
    CreateGuestBookingDto createGuestBookingDto
  ) {
    int numPassengers = createGuestBookingDto.getPassengers().size();
    Set<Flight> flights = new HashSet<>();
    Set<Passenger> passengers = new HashSet<>();

    // take the list of flight ids and find each flight object
    createGuestBookingDto
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
    Float total = calculateTotal(flights, numPassengers);

    // attempt to charge card for total price and save payment id
    String paymentId = "";
    try {
      paymentId =
        stripeService.chargeCreditCard(
          createGuestBookingDto.getStripeToken(),
          total
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

    /*
      Create guest booking
      Associate with booking
      Add guest email
      Add guest phone
     */
    BookingGuest guestBooking = new BookingGuest();
    guestBooking.setBookingId(booking.getId());
    guestBooking.setContactEmail(createGuestBookingDto.getGuestEmail());
    guestBooking.setContactPhone(createGuestBookingDto.getGuestPhone());
    bookingGuestDao.save(guestBooking);

    // Associate each flight with the new booking
    flights.forEach(
      flight -> {
        flight.getBookings().add(booking);
        flightDao.save(flight);
      }
    );

    // create a new passenger for each passenger and associate it with the booking
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

    // associate the passengers with the booking
    booking.setPassengers(passengers);
    // update the booking
    bookingDao.save(booking);
    // grab the booking with all of its associated data and return it
    return bookingDao.getOne(booking.getId());
  }

  private Float calculateTotal(Set<Flight> flights, int passengerCount) {
    // loop through each flight
    float subtotal = 0f;
    for (Flight flight : flights) {
      // get the flight seat price, multiply it by the num of passengers and add it to the counter
      subtotal += flight.getSeatPrice() * passengerCount;
    }
    // calculate sales tax
    float tax = 0.0825f * subtotal;
    // return grand total
    return subtotal + tax;
  }
}
