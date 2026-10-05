package com.orbix.api.modules.identityandaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Branch;

public interface UserRepository extends JpaRepository <User, Long> {
Optional<User> findByUsername(String username);
	
	@Query("SELECT u.nickname FROM User u WHERE u.id =:id")
	String getNickname(Long id);
	/**
	 * @param rollNo
	 * @return
	 */
	Optional<User> findByCode(String c);
	
	@Query("SELECT MAX(u.id) FROM User u")
	Long getLastId();

	/**
	 * @param string
	 * @return
	 */
	boolean existsByUsername(String string);

	/**
	 * @param value
	 * @param value2
	 * @param value3
	 * @return
	 */
	List<User> findAllByFirstNameContainingOrMiddleNameContainingOrLastNameContaining(String value, String value2,
			String value3);

	Optional<User> findByNickname(String nickname);

	List<User> findAllByActive(boolean b);

	List<User> findAllByBranch(Branch branch);

	// Search on the columns the user list shows (c is the user's company, b the branch)
	@Query("SELECT u FROM User u LEFT JOIN u.company c LEFT JOIN u.branch b WHERE :search = '%%' OR LOWER(u.code) LIKE :search"
			+ " OR LOWER(u.username) LIKE :search OR LOWER(u.firstName) LIKE :search OR LOWER(u.lastName) LIKE :search"
			+ " OR LOWER(u.nickname) LIKE :search OR LOWER(u.type) LIKE :search OR LOWER(c.name) LIKE :search OR LOWER(b.name) LIKE :search")
	Page<User> getPageBySearch(@Param("search") String search, Pageable pageable);
}
