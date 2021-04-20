package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentRefundException;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
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
  StripeService(@Value("#{${stripe.apiKey}['stripeApiKey']}") String apiKey) {
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

  public String chargeCreditCard(String token, float amount)
    throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException {
    Map<String, Object> chargeParams = new HashMap<>();
    chargeParams.put("amount", (int) (amount * 100));
    chargeParams.put("currency", "USD");
    chargeParams.put("source", token);
    Charge charge = Charge.create(chargeParams);
    return charge.getId();
  }

  public void refundCharge(String stripeId) {
    Charge charge;
    try {
      charge = Charge.retrieve(stripeId);
    } catch (Exception e) {
      throw new PaymentNotFoundException();
    }
    String chargeId = charge.getId();
    Map<String, Object> params = new HashMap<>();
    params.put("charge", chargeId);
    params.put("reason", "requested_by_customer");
    try {
      Refund refund = Refund.create(params);
    } catch (
      AuthenticationException
      | InvalidRequestException
      | APIConnectionException
      | APIException
      | CardException e
    ) {
      throw new PaymentRefundException();
    }
  }
}
