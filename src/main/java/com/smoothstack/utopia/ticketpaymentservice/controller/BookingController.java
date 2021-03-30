package com.smoothstack.utopia.ticketpaymentservice.controller;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.UpdateBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.service.BookingService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rob Maes
 * Mar 17 2021
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(
  path = "/bookings",
  produces = {
    MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
  }
)
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
  public Booking createBooking(
    @Valid @RequestBody CreateBookingDto createBookingDto
  ) {
    return bookingService.createBooking(createBookingDto);
  }

  @PutMapping(path = "{bookingId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateBooking(
    @PathVariable("bookingId") Long bookingId,
    @Valid @RequestBody UpdateBookingDto updateBookingDto
  ) {
    bookingService.updateBooking(bookingId, updateBookingDto);
  }

  @DeleteMapping(path = "{bookingId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBooking(@PathVariable("bookingId") Long bookingId) {
    bookingService.deleteBooking(bookingId);
  }
}