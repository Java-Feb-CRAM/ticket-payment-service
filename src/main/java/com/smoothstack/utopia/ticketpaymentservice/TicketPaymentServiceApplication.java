package com.smoothstack.utopia.ticketpaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@EntityScan(basePackages = "com.smoothstack.utopia.shared.model")
@ComponentScan(
  basePackages = {
    "com.smoothstack.utopia.shared",
    "com.smoothstack.utopia.ticketpaymentservice",
  }
)
@SpringBootApplication
public class TicketPaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketPaymentServiceApplication.class, args);
  }
}
