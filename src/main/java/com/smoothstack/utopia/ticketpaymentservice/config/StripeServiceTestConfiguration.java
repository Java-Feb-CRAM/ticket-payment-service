package com.smoothstack.utopia.ticketpaymentservice.config;

import com.smoothstack.utopia.ticketpaymentservice.service.StripeService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
@Profile("test")
@Configuration
public class StripeServiceTestConfiguration {

  @Bean
  @Primary
  public StripeService stripeService() {
    return Mockito.mock(StripeService.class);
  }
}
