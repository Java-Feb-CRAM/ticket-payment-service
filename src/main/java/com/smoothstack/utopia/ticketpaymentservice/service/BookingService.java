package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.Flight;
import com.smoothstack.utopia.shared.model.Passenger;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.FlightDao;
import com.smoothstack.utopia.ticketpaymentservice.dao.PassengerDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.UpdateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.FlightNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

  private final BookingDao bookingDao;
  private final FlightDao flightDao;
  private final PassengerDao passengerDao;

  @Autowired
  public BookingService(
    BookingDao bookingDao,
    FlightDao flightDao,
    PassengerDao passengerDao
  ) {
    this.bookingDao = bookingDao;
    this.flightDao = flightDao;
    this.passengerDao = passengerDao;
  }

  public List<Booking> getAllBookings() {
    return bookingDao.findAll();
  }

  public Booking getBooking(Long bookingId) {
    return bookingDao
      .findById(bookingId)
      .orElseThrow(BookingNotFoundException::new);
  }

  @Transactional
  public Booking createBooking(CreateBookingDto createBookingDto) {
    Set<Flight> flights = new HashSet<>();
    Set<Passenger> passengers = new HashSet<>();

    createBookingDto
      .getFlightIds()
      .forEach(
        flightId -> {
          Flight flight = flightDao
            .findById(flightId)
            .orElseThrow(FlightNotFoundException::new);
          flights.add(flight);
          System.out.println("flight!!!");
        }
      );

    Booking booking = new Booking();
    booking.setIsActive(false);
    booking.setConfirmationCode(UUID.randomUUID().toString());
    booking.setFlights(flights);
    bookingDao.save(booking);

    flights.forEach(
      flight -> {
        flight.getBookings().add(booking);
        flightDao.save(flight);
      }
    );

    createBookingDto
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
