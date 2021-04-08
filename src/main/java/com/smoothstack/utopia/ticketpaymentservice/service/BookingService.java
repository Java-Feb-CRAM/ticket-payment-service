package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.UpdateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Rob Maes
 * Mar 17 2021
 */
@Service
public class BookingService {
  private final BookingDao bookingDao;

  @Autowired
  public BookingService(BookingDao bookingDao) {
    this.bookingDao = bookingDao;
  }

  public List<Booking> getAllBookings() {
    return bookingDao.findAll();
  }

  public Booking getBooking(Long bookingId) {
    Optional<Booking> bookingOptional = bookingDao.findById(bookingId);
    return bookingOptional.orElseThrow(BookingNotFoundException::new);
  }

  public void createBooking(CreateBookingDto createBookingDto) {

  }

  @Transactional
  public void updateBooking(Long bookingId, UpdateBookingDto updateBookingDto) {

  }

  public void deleteBooking(Long bookingId) {
    Optional<Booking> bookingOptional = bookingDao.findById(bookingId);
    //bookingDao.delete(bookingOptional.orElseThrow(BookingNotFoundException::new));
  }


}
