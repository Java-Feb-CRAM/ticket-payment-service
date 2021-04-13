package com.smoothstack.utopia.ticketpaymentservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
class StripeServiceIntTest {

  @Autowired
  private StripeService stripeService;

  @Test
  void testChargeCreditCard() throws Exception {
    Mockito
      .when(stripeService.chargeCreditCard("ABC-DEF", 10.0f))
      .thenReturn("XYZ");
    Assertions.assertEquals(
      "XYZ",
      stripeService.chargeCreditCard("ABC-DEF", 10.0f)
    );
  }
}
