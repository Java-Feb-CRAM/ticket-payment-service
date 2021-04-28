package com.smoothstack.utopia.ticketpaymentservice.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Rob Maes
 * Apr 28 2021
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingDto {

  private Map<Long, CreatePassengerDto> passengers;
}
