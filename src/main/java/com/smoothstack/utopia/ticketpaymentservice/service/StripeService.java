package com.smoothstack.utopia.ticketpaymentservice.service;

import com.netflix.discovery.converters.Auto;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Rob Maes
 * Apr 01 2021
 */
@Service
public class StripeService {

  @Autowired
  StripeService(@Value("${stripe.apiKey}") String apiKey) {
    System.out.println("Stripe INIT");
    System.out.println(apiKey);
    Stripe.apiKey = apiKey;
  }

  public Charge chargeCreditCard(String token, float amount) throws Exception {
    Map<String, Object> chargeParams = new HashMap<String, Object>();
    chargeParams.put("amount", (int) (amount * 100));
    chargeParams.put("currency", "USD");
    chargeParams.put("source", token);
    return Charge.create(chargeParams);
  }
}
