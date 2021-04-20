package com.smoothstack.utopia.ticketpaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Rob Maes
 * Apr 20 2021
 */
@ResponseStatus(
  value = HttpStatus.PAYMENT_REQUIRED,
  reason = "Unable to refund charge to card"
)
public class PaymentRefundException extends RuntimeException {}
