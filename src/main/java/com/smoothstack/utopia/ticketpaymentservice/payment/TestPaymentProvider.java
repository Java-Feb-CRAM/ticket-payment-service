package com.smoothstack.utopia.ticketpaymentservice.payment;

import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import java.util.Map;

/**
 * @author Rob Maes
 * Apr 21 2021
 */
public class TestPaymentProvider implements PaymentProvider {

  @Override
  public Charge retrieveCharge(String stripeId) {
    Charge charge = new Charge();
    charge.setAmount(1234L);
    charge.setCreated(111L);
    charge.setCurrency("USD");
    charge.setId("CHARGE_ID");
    return charge;
  }

  @Override
  public Charge createCharge(Map<String, Object> params) {
    Charge charge = new Charge();
    charge.setId("CHARGE_ID");
    charge.setCurrency(params.get("currency").toString());
    charge.setAmount(Long.parseLong(params.get("amount").toString()));
    return charge;
  }

  @Override
  public Refund createRefund(Map<String, Object> params) {
    Refund refund = new Refund();
    refund.setCharge(params.get("charge").toString());
    refund.setReason(params.get("reason").toString());
    return refund;
  }

  @Override
  public Card cardFromCharge(Charge charge) {
    Card card = new Card();
    card.setBrand("VISA");
    card.setLast4("1234");
    return card;
  }
}
