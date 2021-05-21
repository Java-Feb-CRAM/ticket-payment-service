package com.smoothstack.utopia.ticketpaymentservice.payment;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.Flight;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
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

  public void addLineItem(Flight flight, List<LocalDate> passengerDobs) {
    long regularPassengers = passengerDobs
      .stream()
      .filter(
        dob -> {
          long age = ChronoUnit.YEARS.between(dob, LocalDate.now());
          return age > 12 && age < 65;
        }
      )
      .count();
    long childPassengers = passengerDobs
      .stream()
      .filter(dob -> ChronoUnit.YEARS.between(dob, LocalDate.now()) >= 65)
      .count();
    long elderlyPassengers = passengerDobs
      .stream()
      .filter(dob -> ChronoUnit.YEARS.between(dob, LocalDate.now()) <= 12)
      .count();

    if (regularPassengers > 0) {
      String description =
        flight.getRoute().getOriginAirport().getIataId() +
        " &rarr; " +
        flight.getRoute().getDestinationAirport().getIataId() +
        " &times; " +
        regularPassengers +
        " passengers";
      Float price = flight.getSeatPrice() * regularPassengers;
      total += price;
      lineItems.put(description, "$ " + df.format(price));
    }

    if (elderlyPassengers > 0) {
      String description =
        flight.getRoute().getOriginAirport().getIataId() +
        " &rarr; " +
        flight.getRoute().getDestinationAirport().getIataId() +
        " &times; " +
        elderlyPassengers +
        " senior passengers";
      Float percentOff = flight.getSeatPrice() * .2f;
      Float price = (flight.getSeatPrice() - percentOff) * elderlyPassengers;
      total += price;
      lineItems.put(description, "$ " + df.format(price));
    }

    if (childPassengers > 0) {
      String description =
        flight.getRoute().getOriginAirport().getIataId() +
        " &rarr; " +
        flight.getRoute().getDestinationAirport().getIataId() +
        " &times; " +
        childPassengers +
        " child passengers";
      Float percentOff = flight.getSeatPrice() * .2f;
      Float price = (flight.getSeatPrice() - percentOff) * childPassengers;
      total += price;
      lineItems.put(description, "$ " + df.format(price));
    }
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
