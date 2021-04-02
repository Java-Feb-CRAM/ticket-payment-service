package com.smoothstack.utopia.ticketpaymentservice.controller;

import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import com.smoothstack.utopia.ticketpaymentservice.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rob Maes
 * Apr 02 2021
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(
  path = "/payments",
  produces = {
    MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
  }
)
public class PaymentController {

  private final StripeService stripeService;

  @Autowired
  public PaymentController(StripeService stripeService) {
    this.stripeService = stripeService;
  }

  @GetMapping(path = "{stripeId}")
  public PaymentInfoDto getPayment(@PathVariable("stripeId") String stripeId) {
    return this.stripeService.getPaymentInfo(stripeId);
  }
}
