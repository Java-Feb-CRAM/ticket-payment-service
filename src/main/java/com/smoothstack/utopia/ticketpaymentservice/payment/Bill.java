package com.smoothstack.utopia.ticketpaymentservice.payment;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.Flight;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Rob Maes
 * Apr 20 2021
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

  private static DecimalFormat df = new DecimalFormat("0.00");
  private Float total = 0.0f;
  private Map<String, String> lineItems = new LinkedHashMap<>();
  private Booking booking;

  public void addLineItem(Flight flight, Integer passengerCount) {
    String description =
      flight.getRoute().getOriginAirport().getIataId() +
      " &rarr; " +
      flight.getRoute().getDestinationAirport().getIataId() +
      " &times; " +
      passengerCount +
      " passengers";
    Float price = flight.getSeatPrice() * passengerCount;
    total += price;
    lineItems.put(description, "$ " + df.format(price));
  }

  public void addTaxLineItem(Float taxRate) {
    Float tax = taxRate * total;
    lineItems.put("Tax", "$ " + df.format(tax));
    total += tax;
  }

  public String getFormattedTotal() {
    return "$ " + df.format(total);
  }
}
