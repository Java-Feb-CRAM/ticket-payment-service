package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
public interface AirportDao extends JpaRepository<Airport, String> {}
