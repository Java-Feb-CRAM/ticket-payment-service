package com.smoothstack.utopia.ticketpaymentservice.dto;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Rob Maes
 * Apr 19 2021
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class BaseBookingDto {

  private String stripeToken;
  private Set<Long> flightIds;
  private List<CreatePassengerDto> passengers;
}
