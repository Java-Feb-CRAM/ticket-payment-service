package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rob Maes
 * Apr 01 2021
 */
@Repository
public interface BookingGuestDao extends JpaRepository<BookingGuest, Long> {}
