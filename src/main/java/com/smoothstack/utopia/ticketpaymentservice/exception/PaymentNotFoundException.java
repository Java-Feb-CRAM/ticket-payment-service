package com.smoothstack.utopia.ticketpaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Rob Maes
 * Apr 02 2021
 */
@ResponseStatus(
  value = HttpStatus.NOT_FOUND,
  reason = "The requested payment does not exist"
)
public class PaymentNotFoundException extends RuntimeException {}
