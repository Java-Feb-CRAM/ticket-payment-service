package com.smoothstack.utopia.ticketpaymentservice.config;

import com.smoothstack.utopia.ticketpaymentservice.payment.PaymentProvider;
import com.smoothstack.utopia.ticketpaymentservice.payment.StripePaymentProvider;
import com.smoothstack.utopia.ticketpaymentservice.payment.TestPaymentProvider;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Rob Maes
 * Apr 21 2021
 */
@Configuration
public class PaymentConfig {

  @Value("#{${stripe.apiKey}['stripeApiKey']}")
  private String apiKey;

  @Profile({ "dev", "prod" })
  @Bean
  public PaymentProvider stripePaymentProvider() {
    Stripe.apiKey = apiKey;
    return new StripePaymentProvider();
  }

  @Profile("test")
  @Bean
  public PaymentProvider testPaymentProvider() {
    return new TestPaymentProvider();
  }
}
