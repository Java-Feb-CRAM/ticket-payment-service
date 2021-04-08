package com.smoothstack.utopia.ticketpaymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
@SpringBootTest
class TicketPaymentServiceApplicationTests {

  @Test
  void contextLoads() {}
}
