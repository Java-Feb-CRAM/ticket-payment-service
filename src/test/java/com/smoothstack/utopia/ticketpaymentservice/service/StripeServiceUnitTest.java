package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
class StripeServiceUnitTest {

  @Autowired
  private StripeService stripeService;

  @Test
  void testGetPaymentInfo() {
    PaymentInfoDto paymentInfo = stripeService.getPaymentInfo("STRIPE_ID");
    Assertions.assertEquals(1234L, paymentInfo.getAmount());
    Assertions.assertEquals(111L, paymentInfo.getCreated());
    Assertions.assertEquals("USD", paymentInfo.getCurrency());
    Assertions.assertEquals("VISA", paymentInfo.getCardBrand());
    Assertions.assertEquals("1234", paymentInfo.getLastFour());
  }

  @Test
  void testChargeCreditCard() throws Exception {
    String chargeId = stripeService.chargeCreditCard("TOKEN", 9.99f);
    Assertions.assertEquals("CHARGE_ID", chargeId);
  }

  @Test
  void testRefundCharge() {
    stripeService.refundCharge("STRIPE_ID");
  }
}
