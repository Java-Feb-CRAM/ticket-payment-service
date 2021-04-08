package com.smoothstack.utopia.ticketpaymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Rob Maes
 * Apr 02 2021
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDto {

  private Long amount;
  private Long created;
  private String currency;
  private String cardBrand;
  private String lastFour;
}
