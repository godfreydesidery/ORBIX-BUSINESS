package com.orbix.api.modules.finance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRequestRepository extends JpaRepository<DiscountRequest, Long> {

	Optional<DiscountRequest> findByServiceBillIdAndServiceBillName(Long serviceBillId, String serviceBillName);

	List<DiscountRequest> findAllByServiceBillIdInAndServiceBillName(List<Long> ids, String serviceName);

}
