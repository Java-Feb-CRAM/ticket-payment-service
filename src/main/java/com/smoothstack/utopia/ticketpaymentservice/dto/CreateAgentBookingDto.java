package com.smoothstack.utopia.ticketpaymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Rob Maes
 * Mar 31 2021
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateAgentBookingDto extends BaseBookingDto {

  private Long agentId;
}
