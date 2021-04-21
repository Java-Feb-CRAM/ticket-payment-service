package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.Booking;
import com.smoothstack.utopia.shared.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rob Maes
 * Mar 17 2021
 */
@Repository
public interface BookingDao extends JpaRepository<Booking, Long> {
  Optional<Booking> findBookingByConfirmationCode(String confirmationCode);
  List<Booking> findBookingByBookingUser_User(User user);
}
