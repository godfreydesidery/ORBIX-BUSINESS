package com.orbix.api.modules.adminunits;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.identityandaccess.User;

public interface ShopRepository extends JpaRepository<Shop, Long> {

	List<Shop> findAllByBranch(Branch branch);

	Optional<Shop> findByIdAndBranch(Long id, User user);

}
