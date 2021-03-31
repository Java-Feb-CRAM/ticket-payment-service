package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Rob Maes
 * Mar 30 2021
 */
public interface FlightDao extends JpaRepository<Flight, Long> {}
