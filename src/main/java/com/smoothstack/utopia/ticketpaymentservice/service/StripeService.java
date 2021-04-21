package com.smoothstack.utopia.ticketpaymentservice.service;

import com.smoothstack.utopia.ticketpaymentservice.dto.PaymentInfoDto;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentRefundException;
import com.smoothstack.utopia.ticketpaymentservice.payment.PaymentProvider;
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

  private final PaymentProvider paymentProvider;

  @Autowired
  StripeService(PaymentProvider paymentProvider) {
    this.paymentProvider = paymentProvider;
  }

  public PaymentInfoDto getPaymentInfo(String stripeId) {
    Charge charge = paymentProvider.retrieveCharge(stripeId);
    PaymentInfoDto paymentInfo = new PaymentInfoDto();
    paymentInfo.setAmount(charge.getAmount());
    paymentInfo.setCreated(charge.getCreated());
    paymentInfo.setCurrency(charge.getCurrency());
    Card card = paymentProvider.cardFromCharge(charge);
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
    Charge charge = paymentProvider.createCharge(chargeParams);
    return charge.getId();
  }

  public void refundCharge(String stripeId) {
    Charge charge = paymentProvider.retrieveCharge(stripeId);
    String chargeId = charge.getId();
    Map<String, Object> params = new HashMap<>();
    params.put("charge", chargeId);
    params.put("reason", "requested_by_customer");
    Refund refund = paymentProvider.createRefund(params);
  }
}
