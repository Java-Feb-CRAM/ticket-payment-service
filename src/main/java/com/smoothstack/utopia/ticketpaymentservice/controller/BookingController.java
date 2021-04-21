package com.smoothstack.utopia.ticketpaymentservice.controller;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateAgentBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateGuestBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.dto.CreateUserBookingDto;
import com.smoothstack.utopia.ticketpaymentservice.service.BookingService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rob Maes
 * Mar 17 2021
 */

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

  @GetMapping(path = "/confirmation/{confirmationCode}")
  public Booking getBookingByConfirmationCode(
    @PathVariable("confirmationCode") String confirmationCode
  ) {
    return bookingService.getBookingByConfirmationCode(confirmationCode);
  }

  @GetMapping(path = "/user/{userId}")
  public List<Booking> getBookingsByUser(@PathVariable("userId") Long userId) {
    return bookingService.getBookingsByUser(userId);
  }

  @PostMapping(path = "/guest")
  @ResponseStatus(HttpStatus.CREATED)
  public Booking createGuestBooking(
    @Valid @RequestBody CreateGuestBookingDto createGuestBookingDto
  ) {
    return bookingService.createGuestBooking(createGuestBookingDto);
  }

  @PostMapping(path = "/user")
  @ResponseStatus(HttpStatus.CREATED)
  public Booking createUserBooking(
    @Valid @RequestBody CreateUserBookingDto createUserBookingDto
  ) {
    return bookingService.createUserBooking(createUserBookingDto);
  }

  @PostMapping(path = "/agent")
  @ResponseStatus(HttpStatus.CREATED)
  public Booking createAgentBooking(
    @Valid @RequestBody CreateAgentBookingDto createAgentBookingDto
  ) {
    return bookingService.createAgentBooking(createAgentBookingDto);
  }

  @DeleteMapping(path = "{bookingId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelBooking(@PathVariable("bookingId") Long bookingId) {
    bookingService.cancelBooking(bookingId);
  }
}
