package com.smoothstack.utopia.ticketpaymentservice.service;

import com.netflix.discovery.converters.Auto;
import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentNotFoundException;
import com.stripe.Stripe;
import com.stripe.model.Card;
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
    Stripe.apiKey = apiKey;
  }

  public PaymentInfoDto getPaymentInfo(String stripeId) {
    Charge charge;
    try {
      charge = Charge.retrieve(stripeId);
    } catch (Exception e) {
      throw new PaymentNotFoundException();
    }
    PaymentInfoDto paymentInfo = new PaymentInfoDto();
    paymentInfo.setAmount(charge.getAmount());
    paymentInfo.setCreated(charge.getCreated());
    paymentInfo.setCurrency(charge.getCurrency());
    Card card = (Card) charge.getSource();
    paymentInfo.setCardBrand(card.getBrand());
    paymentInfo.setLastFour(card.getLast4());
    return paymentInfo;
  }

  public Charge chargeCreditCard(String token, float amount) throws Exception {
    Map<String, Object> chargeParams = new HashMap<String, Object>();
    chargeParams.put("amount", (int) (amount * 100));
    chargeParams.put("currency", "USD");
    chargeParams.put("source", token);
    return Charge.create(chargeParams);
  }
}
