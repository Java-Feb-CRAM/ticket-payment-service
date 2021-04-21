package com.smoothstack.utopia.ticketpaymentservice.payment;

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
public interface PaymentProvider {
  Charge retrieveCharge(String stripeId);
  Charge createCharge(Map<String, Object> params)
    throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException;
  Refund createRefund(Map<String, Object> params);
  Card cardFromCharge(Charge charge);
}
