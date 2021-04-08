package com.smoothstack.utopia.ticketpaymentservice.controller;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.ticketpaymentservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Rob Maes
 * Mar 17 2021
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

  private final BookingService bookingService;

  @Autowired
  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @GetMapping
  public List<Booking> getAllBookings() {
    return bookingService.getAllBookings();
  }

  @GetMapping(path = "{bookingId}")
  public Booking getBooking(@PathVariable("bookingId") Long bookingId) {
    return bookingService.getBooking(bookingId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createBooking() {}

  @PutMapping(path="{bookingId}")
  public void updateBooking(@PathVariable("bookingId") Long bookingId) {

  }

  @DeleteMapping(path="{bookingId}")
  public void deleteBooking(@PathVariable("bookingId") Long bookingId) {
    bookingService.deleteBooking(bookingId);
  }
}
