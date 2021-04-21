package com.smoothstack.utopia.ticketpaymentservice.dao;

import com.smoothstack.utopia.shared.model.Route;
import com.smoothstack.utopia.shared.model.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Rob Maes
 * Apr 20 2021
 */
public interface UserRoleDao extends JpaRepository<UserRole, Long> {
  Optional<UserRole> findUserRoleByName(String name);
}
