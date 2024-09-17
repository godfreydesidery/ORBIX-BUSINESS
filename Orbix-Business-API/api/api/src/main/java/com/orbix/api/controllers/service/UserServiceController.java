/**
 * 
 */
package com.orbix.api.controllers.service;

import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.accessories.Formater;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;

/**
 * @author GODFREY
 *
 */
@RestController
@Service
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserServiceController {
	
//	@Autowired
//	UserRepository userRepository;
//	
//	@Autowired
//	RoleRepository roleRepository;
//	
//	@Autowired
//	private PasswordEncoder bcryptEncoder;
//	
//	/**
//	 * Get all users
//	 * @param userId
//	 * @return
//	 * @throws Exception 
//	 */
//	@RequestMapping(
//			method = RequestMethod.GET, 
//			value = "/users", 
//			produces = MediaType.APPLICATION_JSON_VALUE)
//    public Iterable <User> getAllUsers(
//    		@RequestHeader(name = "user_id") Long userId,
//			@RequestHeader(name = "token") String token) {   		
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.READ)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//        return userRepository.findAll();
//    }
//	
//	/**
//	 * 
//	 * @param id
//	 * @param userId
//	 * @return
//	 */	
//	@RequestMapping(
//			method = RequestMethod.GET, 
//			value = "/users/get", 
//			produces=MediaType.APPLICATION_JSON_VALUE)
//    public User getUserById(
//    		@RequestParam(name = "id") Long id, 
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token) {
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.READ)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//        return userRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//    }
//	
//	/**
//	 * Create a new user
//	 * @param user
//	 * @param userId
//	 * @param token
//	 * @return
//	 * @throws Exception
//	 */
//	
//	@RequestMapping(
//			method = RequestMethod.POST, 
//			value="/users/create", 
//			produces=MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    @Transactional
//    public User createUser(
//    		@Valid @RequestBody User user, 
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token
//    		) throws Exception {  
//		
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.CREATE)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//		
//    	user.setRole(null);
//    	String random = String.valueOf(Math.random()).replace(".", "") + String.valueOf(Math.random()).replace(".", "");
//    	user.setRollNo(random); 
//    	user.setActive(0);    	
//    	Optional<Role> role;		
////		if(!user.getRole().getName().isEmpty()) {		
////    		role = roleRepository.findByName(user.getRole().getName());
////    		roleRepository.save(role.get());
////    		user.setRole(role.get());
////    	}else {
////    		user.setRole(null);
////    	}  	
//    	user.setPassword(bcryptEncoder.encode(user.getFirstName()));
//    	userRepository.save(user);
//    	String serial = user.getId().toString();    	
//    	String code = "U-"+Formater.formatSix(serial);
//    	user.setRollNo(code);   	
//    	return userRepository.save(user);
//    }
//	
//	//Edit an existing user
//	
//	@RequestMapping(
//			method = RequestMethod.PUT, 
//			value="/users/edit", 
//			produces=MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    @Transactional
//    public User updateUser(
//    		@Valid @RequestBody User userDetails, 
//    		@RequestParam(name = "id") Long id,   		
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token) throws Exception {
//		
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.UPDATE)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//		
//		User user = userRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//		if(user.getUsername().equals("admin")) {
//			throw new InvalidOperationException("Editing the admin profile is not allowed");
//		}
//		user.setRole(null);
//		System.out.println(userDetails.getPassword());
//		user.setUsername(userDetails.getUsername());
//		user.setFirstName(userDetails.getFirstName());
//		user.setSecondName(userDetails.getSecondName());
//		user.setLastName(userDetails.getLastName());
//		user.setPassword(bcryptEncoder.encode(userDetails.getPassword()));
//		Optional<Role> role;		
////		if(!userDetails.getRole().getName().isEmpty()) {		
////    		role = roleRepository.findByName(userDetails.getRole().getName());
////    		roleRepository.save(role.get());
////    		user.setRole(role.get());
////    	}else {
////    		user.setRole(null);
////    	}
//		
//		
////edit later 		
//    	return userRepository.save(user);
//    }
//	
//	/**
//	 * Delete a user
//	 * @param id
//	 * @param userId
//	 * @return
//	 */
//	@DeleteMapping("/users/delete")
//    @Transactional
//    public ResponseEntity<?> deleteUser(
//    		@RequestParam(name = "id") Long id,   		
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token) {
//		
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.DELETE)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//		
//    	User user = userRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("No matching User"));
//    	if(user.getUsername().equals("admin")) {
//    		throw new InvalidOperationException("Deleting the admin profile is not allowed");
//    	}
//    	if(id == userId) {
//    		throw new InvalidOperationException("User can not delete itself");
//    	}
//    	//this.checkUsageBeforeDelete(user);
//    	userRepository.delete(user);
//        return ResponseEntity.ok().build();
//    }
//	
//	/**
//	 * Activates a user, set 1 for active user
//	 * @param id
//	 * @param userId
//	 * @param token
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(
//			method = RequestMethod.PUT, 
//			value="/users/activate", 
//			produces=MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    @Transactional
//    public boolean  activateUser(
//    		@RequestParam(name = "id") Long id,   		
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token) throws Exception{
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.ACTIVATE)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//		User user = userRepository.findById(id)
//				.orElseThrow(() -> new NotFoundException("User not found"));
//				user.setActive(1);
//				userRepository.save(user);
//		return true;
//	}
//	
//	/**
//	 * 
//	 * @param id
//	 * @param userId
//	 * @param token
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(
//			method = RequestMethod.PUT, 
//			value="/users/deactivate", 
//			produces=MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    @Transactional
//    public boolean  deactivateUser(
//    		@RequestParam(name = "id") Long id,   		
//    		@RequestHeader("user_id") Long userId,
//    		@RequestHeader(name = "token") String token) throws Exception{
//		if (Authorize.authorize(userId, token, Authority.getAuthority(Objects.USER, Actions.DEACTIVATE)) == false) {
//			System.out.println("Access denied");
//			throw new InvalidOperationException("Access denied");
//		}
//		User user = userRepository.findById(id)
//				.orElseThrow(() -> new NotFoundException("User not found"));
//				user.setActive(0);
//				userRepository.save(user);
//		return true;
//	}
}
