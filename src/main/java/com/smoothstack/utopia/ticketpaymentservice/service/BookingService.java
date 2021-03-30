package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.ticketpaymentservice.dao.BookingDao;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.UpdateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.BookingNotFoundException;
import java.util.List;
import java.util.Optional;
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

  @Autowired
  public BookingService(BookingDao bookingDao) {
    this.bookingDao = bookingDao;
  }

  public List<Booking> getAllBookings() {
    return bookingDao.findAll();
  }

  public Booking getBooking(Long bookingId) {
    return bookingDao
      .findById(bookingId)
      .orElseThrow(BookingNotFoundException::new);
  }

  public Booking createBooking(CreateBookingDto createBookingDto) {
    return new Booking();
  }

  public void updateBooking(
    Long bookingId,
    UpdateBookingDto updateBookingDto
  ) {}

  public void deleteBooking(Long bookingId) {}
}
