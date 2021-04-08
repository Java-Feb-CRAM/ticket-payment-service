package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Rob Maes
 * Mar 30 2021
 */
public interface PassengerDao extends JpaRepository<Passenger, Long> {}
