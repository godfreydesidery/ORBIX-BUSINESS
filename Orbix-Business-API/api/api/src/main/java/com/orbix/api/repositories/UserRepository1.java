/**
 * 
 */
package com.orbix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.orbix.api.domain.User1;

/**
 * @author GODFREY
 *
 */
public interface UserRepository1 extends JpaRepository<User1, Long> {
	Optional<User1> findByUsername(String username);
	
	@Query("SELECT u.nickname FROM User u WHERE u.id =:id")
	String getNickname(Long id);
	/**
	 * @param rollNo
	 * @return
	 */
	Optional<User1> findByCode(String c);
	
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
	List<User1> findAllByFirstNameContainingOrMiddleNameContainingOrLastNameContaining(String value, String value2,
			String value3);

	Optional<User1> findByNickname(String nickname);

	List<User1> findAllByActive(boolean b);
}
