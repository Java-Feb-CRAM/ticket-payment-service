package com.smoothstack.utopia.ticketpaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Rob Maes
 * Apr 28 2021
 */
@ResponseStatus(
  value = HttpStatus.NOT_FOUND,
  reason = "The requested booking does not exist"
)
public class PassengerNotFoundException extends RuntimeException {}
