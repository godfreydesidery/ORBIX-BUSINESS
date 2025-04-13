package com.orbix.api.modules.warehouse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageGoodReleaseRepository extends JpaRepository<StorageGoodRelease, Long> {

	List<StorageGoodRelease> findAllByStorage(Storage storage);

}
