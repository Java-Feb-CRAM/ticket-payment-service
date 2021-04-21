package com.smoothstack.utopia.ticketpaymentservice.payment;

import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentNotFoundException;
import com.smoothstack.utopia.ticketpaymentservice.exception.PaymentRefundException;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import java.util.Map;

/**
 * @author Rob Maes
 * Apr 21 2021
 */
public class StripePaymentProvider implements PaymentProvider {

  @Override
  public Charge retrieveCharge(String stripeId) {
    Charge charge;
    try {
      charge = Charge.retrieve(stripeId);
    } catch (Exception e) {
      throw new PaymentNotFoundException();
    }
    return charge;
  }

  @Override
  public Charge createCharge(Map<String, Object> params)
    throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
    return Charge.create(params);
  }

  @Override
  public Refund createRefund(Map<String, Object> params) {
    Refund refund;
    try {
      refund = Refund.create(params);
    } catch (
      AuthenticationException
      | InvalidRequestException
      | APIConnectionException
      | CardException
      | APIException e
    ) {
      throw new PaymentRefundException();
    }
    return refund;
  }

  @Override
  public Card cardFromCharge(Charge charge) {
    return (Card) charge.getSource();
  }
}
