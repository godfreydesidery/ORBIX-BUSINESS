/**
 * 
 */
package com.orbix.api.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.orbix.api.accessories.Formater;
import com.orbix.api.domain.Privilege1;
import com.orbix.api.domain.Role1;
import com.orbix.api.domain.Shortcut;
import com.orbix.api.domain.User1;
import com.orbix.api.exceptions.DuplicateEntryException;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.MissingInformationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.models.RecordModel;
import com.orbix.api.repositories.PrivilegeRepository1;
import com.orbix.api.repositories.RoleRepository1;
import com.orbix.api.repositories.ShortcutRepository;
import com.orbix.api.repositories.UserRepository1;
import com.orbix.api.security.Object_;
import com.orbix.api.security.Operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GODFREY
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
	
	private final UserRepository1 userRepository;
	private final RoleRepository1 roleRepository;
	private final PrivilegeRepository1 privilegeRepository;
	private final PasswordEncoder passwordEncoder;
	private final ShortcutRepository shortcutRepository;
	
	//private final UserService userService; //do not use this here
	private final DayService dayService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {		
		Optional<User1> u = userRepository.findByUsername(username);
		System.out.println(username);
		if(u.isEmpty()) {
			log.error("User not found in the database");
			throw new NotFoundException("User not found in database");
		}else {
			log.info("User found in database: {}", username);
		}
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		u.get().getRoles().forEach(role -> {
			role.getPrivileges().forEach(privilege -> {
				authorities.add(new SimpleGrantedAuthority(privilege.getName()));
			});
		});
		return new org.springframework.security.core.userdetails.User(u.get().getUsername(), u.get().getPassword(), authorities);
	}
	
	@Override
	public User1 saveUser(User1 user, HttpServletRequest request) {
		validateUser(user);
		log.info("Saving user to the database");
		if(user.getId() == null) {
			if(user.getUsername().equalsIgnoreCase("root")) {
				Optional<User1> u = userRepository.findByUsername("root");
				if(u.isPresent()) {
					throw new InvalidOperationException("root already exist");
				}
			}
			user.setCode(this.requestUserCode().getCode());
			user.setPassword(passwordEncoder.encode(user.getPassword()));			
		}else {
			User1 userToUpdate = userRepository.findById(user.getId()).get();
			if(!userToUpdate.getCode().equals(user.getCode())) {
				throw new InvalidOperationException("Changing user code is not allowed");
			}
			if(user.getPassword().equals("") || user.getPassword().equals(null)) {
				user.setPassword(userToUpdate.getPassword());
			}else {
				user.setPassword(passwordEncoder.encode(user.getPassword()));
			}
			user.setActive(true);// use this in the mean time before implementing actiavate and deactivate user
		}
		user = userRepository.saveAndFlush(user);
		
		/**
		 * First get user roles
		 */
		Collection<Role1> roleCollection = user.getRoles();		
		List<Role1> roles = List.copyOf(roleCollection);
		/**
		 * Check the roles for specific roles, these roles are special for they carry out critical operations
		 */
		
		
		return user;
	}
	
	private boolean validateUser(User1 user) {
		/**
		 * Validate Username, username should be >=6 and <=16 in length
		 */
		if((user.getUsername().length() < 3 || user.getUsername().length() > 50) && !user.getUsername().equalsIgnoreCase("root")) {
			throw new InvalidEntryException("Invalid length in username, length should be between 3 and 50");
		}
		/**
		 * Validate password, password should have a valid length
		 */
		if(user.getId() == null) {
			if(user.getPassword().equals("")) {
				throw new MissingInformationException("The password field is required");
			}else if(user.getPassword().length() < 4 || user.getPassword().length() >50) {
				throw new InvalidEntryException("Password length should be more than 5 and less than 51");
			}
		}else {
			if(user.getPassword().length() > 0 && (user.getPassword().length() < 6 || user.getPassword().length() > 50)){
				throw new InvalidEntryException("Password length should be more than 3 and less than 51");
			}
		}
		/**
		 * Validate names, first name and last name should be present
		 */
		if(user.getFirstName().equals("") || user.getLastName().equals("")) {
			throw new MissingInformationException("First name or Last name fields are missing");
		}
		/**
		 * Validate alias, alias field should be present
		 * Alias is a unique flag name that visually identifies a user in the system, also identified as Nickname
		 */
		if(user.getNickname().equals("")) {
			throw new MissingInformationException("The nickname field is missing");
		}		
		return true;
	}

	@Override
	public Role1 saveRole(Role1 role, HttpServletRequest request) {		
		if(role.getName().equalsIgnoreCase("ROOT")) {
			Optional<Role1> r = roleRepository.findByName("ROOT");
			if(r.isPresent()) {
				throw new InvalidOperationException("Can not modify the ROOT role");
			}
		}
		return roleRepository.save(role);
	}

	@Override
	public void addRoleToUser(String username, String rolename, HttpServletRequest request) {
		User1 user = userRepository.findByUsername(username).get();
		Role1 role = roleRepository.findByName(rolename).get();
		try {
			if(!user.getRoles().contains(role)) {
				user.getRoles().add(role);
			}
		}catch(Exception e) {
			log.info(e.getMessage());
		}					
	}

	@Override
	public User1 getUser(String username) {
		return userRepository.findByUsername(username).get();
	}

	@Override
	public List<User1> getUsers() {
		log.info("Fetching all users");
		return userRepository.findAll();
	}

	@Override
	public Privilege1 savePrivilege(Privilege1 privilege, HttpServletRequest request) {
		
		Optional<Privilege1> p = privilegeRepository.findByName(privilege.getName());
		if(p.isPresent()) {
			throw new InvalidOperationException("Privilege already exist");
		}
		
		log.info("Saving new privilege to the database");
		return privilegeRepository.save(privilege);
	}

	@Override
	public void addPrivilegeToRole(String roleName, String privilegeName) {
		Role1 role = roleRepository.findByName(roleName).get();
		Optional<Privilege1> p = privilegeRepository.findByName(privilegeName);
		
		try {
			if(!role.getPrivileges().contains(p.get())) {
				role.getPrivileges().add(p.get());
			}			
		}catch(Exception e) {
			log.info(e.getMessage());
		}			
	}
	
	@Override
	public void removePrivilegeFromRole(String roleName, String privilegeName) {
		Role1 role = roleRepository.findByName(roleName).get();
		Optional<Privilege1> p = privilegeRepository.findByName(privilegeName);
		
		try {
			role.getPrivileges().remove(p.get());			
		}catch(Exception e) {
			log.info(e.getMessage());
		}			
	}

	@Override
	public List<Role1> getRoles() {
		log.info("Fetching all roles");
		return roleRepository.findAll();
	}

	@Override
	public User1 getUserById(Long id) {
		return userRepository.findById(id).get();
	}
	
	@Override
	public String getNicknameByUserId(Long id) {
		if(id == null) {
			return "";
		}
		return userRepository.findById(id).get().getNickname();
	}

	@Override
	public boolean deleteUser(User1 user) {
		/**
		 * Delete a user if a user is deletable
		 */
		if(allowDeleteUser(user) == false) {
			throw new InvalidOperationException("Deleting this user is not allowed");
		}
		userRepository.delete(user);
		return true;
	}
	
	private boolean allowDeleteUser(User1 user) {
		
		return false;
	}

	@Override
	public Role1 getRole(String name) {
		return roleRepository.findByName(name).get();
	}

	@Override
	public List<String> getOperations() {
		List<String> operations = new ArrayList<String>();
		for(Field field : Operation.class.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			if(Modifier.isStatic(modifiers)) {
				String value = "";
				try {
					value = Operation.class.getDeclaredField(field.getName()).get(null).toString();
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
						| SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!value.equals("")) {
					operations.add(value);
				}			
			}
		}
		return operations;
	}

	@Override
	public List<String> getObjects() {
		List<String> objects = new ArrayList<String>();
		for(Field field : Object_.class.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			if(Modifier.isStatic(modifiers)) {
				String value = "";
				try {
					value = Object_.class.getDeclaredField(field.getName()).get(null).toString();
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
						| SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!value.equals("")) {
					if(value.contains("-")) {
						objects.add(value.substring(0, value.indexOf("-")));
					}else {
						objects.add(value);
					}
					
				}			
			}
		}
		List<String> subList = objects.subList(0, objects.size());
		Collections.sort(subList);
		return subList;
		//return objects;
	}

	@Override
	public List<String> getPrivileges(String roleName) {
		Collection<Privilege1> privileges = roleRepository.findByName(roleName).get().getPrivileges();
		List<String> privilegesList = new ArrayList<String>();
		for(Privilege1 privilege : privileges) {
			privilegesList.add(privilege.getName());
		}
		return privilegesList;
	}

	@Override
	public Role1 getRoleById(Long id) {
		return roleRepository.findById(id).get();
	}

	@Override
	public boolean deleteRole(Role1 role) {
		/**
		 * Delete a role if a role is deletable
		 */
		if(allowDeleteRole(role) == false) {
			throw new InvalidOperationException("Deleting this role is not allowed");
		}
		roleRepository.delete(role);
		return true;
	}
	
	private boolean allowDeleteRole(Role1 role) {
		/**
		 * Code to check if a role is deletable
		 * Returns false if not
		 */
		return false;
	}

	@Override
	public boolean createShortcut(String username, String name, String link, HttpServletRequest request) {	
		Shortcut shortcut = new Shortcut();
		
		try {
			User1 user = userRepository.findByUsername(username).get();
			shortcut.setUser(user);
			shortcut.setName(name);
			shortcut.setLink(link);
			Optional<User1> userExist = shortcutRepository.findByLinkAndUser(link, user);
			if(!userExist.isPresent()) {
				shortcutRepository.save(shortcut);
			}else {
				throw new DuplicateEntryException("Could not save shortcut, shortcut already exist");
			}
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean removeShortcut(String username, String name) {			
		try {
			User1 user = userRepository.findByUsername(username).get();
			
			Optional<Shortcut> shortcut = shortcutRepository.findByNameAndUser(name, user);
			if(shortcut.isPresent()) {
				shortcutRepository.delete(shortcut.get());
			}else {
				throw new DuplicateEntryException("Could not remove shortcut, shortcut does not exist");
			}
		}catch(Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public List<Shortcut> loadShortcuts(String username) {
		try {
			User1 user = userRepository.findByUsername(username).get();
			return shortcutRepository.findByUser(user);
		}catch(Exception e) {
			throw new InvalidOperationException("Could not load shortcuts");
		}
	}

	@Override
	public Long getUserId(HttpServletRequest request) {
		return userRepository.findByUsername(request.getUserPrincipal().getName()).get().getId();
	}
	
	@Override
	public User1 getUser(HttpServletRequest request) {
		return userRepository.findByUsername(request.getUserPrincipal().getName()).get();
	}
	
	public RecordModel requestUserCode() {
		Long id = 1L;
		try {
			id = userRepository.getLastId() + 1;
		}catch(Exception e) {
			
		}
		RecordModel model = new RecordModel();
		model.setCode("USR-"+Formater.formatSix(id.toString()));
		model.setNo("USR-"+Formater.formatSix(id.toString()));
		return model;
	}	
}
