package com.smoothstack.utopia.ticketpaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
@ResponseStatus(
  value = HttpStatus.CONFLICT,
  reason = "The flight is full or does not have enough seats for the specified passengers"
)
public class FlightFullException extends RuntimeException {}
