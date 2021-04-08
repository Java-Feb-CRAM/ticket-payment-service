package com.smoothstack.utopia.ticketpaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Rob Maes
 * Apr 02 2021
 */
@ResponseStatus(
  value = HttpStatus.PAYMENT_REQUIRED,
  reason = "Unable to charge card and collect payment"
)
public class PaymentProcessingFailedException extends RuntimeException {}
