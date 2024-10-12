package com.orbix.api.modules.identityandaccess;

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

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.DuplicateEntryException;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.MissingInformationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.CompanyRequestDTO;
import com.orbix.api.modules.adminunits.CompanyResponseDTO;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.utilities.Formatter;
import com.orbix.api.modules.utilities.RecordModel;
import com.orbix.api.modules.utilities.Shortcut;
import com.orbix.api.modules.utilities.ShortcutRepository;
import com.orbix.api.security.Object_;
import com.orbix.api.security.Operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceController implements UserService, UserDetailsService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PrivilegeRepository privilegeRepository;
	private final PasswordEncoder passwordEncoder;
	private final ShortcutRepository shortcutRepository;
	
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	
	//private final UserService userService; //do not use this here
	private final DayService dayService;
	
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {		
		Optional<User> u = userRepository.findByUsername(username);
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
	public User saveUser(User user, HttpServletRequest request) {
		validateUser(user);
		log.info("Saving user to the database");
		if(user.getId() == null) {
			
			User newUser = new User();
			
			if(user.getUsername().equalsIgnoreCase("root")) {
				Optional<User> u = userRepository.findByUsername("root");
				if(u.isPresent()) {
					throw new InvalidOperationException("root already exist");
				}
			}
			
			
			if(user.getType().equals("COMPANY-ROOT-USER") || user.getType().equals("COMPANY-USER")) {
//				if(user.getCompany().getId() == null) {
//					throw new InvalidOperationException("Company User must have a company");
//				}
				if(user.getCompany().getName().equals("")) {
					throw new InvalidOperationException("Company User must have a company");
				}
//				Optional<Company> company_ = companyRepository.findById(user.getCompany().getId());
				Optional<Company> company_ = companyRepository.findByName(user.getCompany().getName());
				if(company_.isEmpty()) {
					throw new NotFoundException("Company not found");
				}
				user.setCompany(company_.get());
//				if(user.getBranch().getId() == null) {
//					throw new InvalidOperationException("Company User must have a branch");
//				}
				if(user.getBranch().getName().equals("")) {
					throw new InvalidOperationException("Company User must have a branch");
				}
//				Optional<Branch> branch_ = branchRepository.findById(user.getBranch().getId());
				Optional<Branch> branch_ = branchRepository.findByName(user.getBranch().getName());
				if(branch_.isEmpty()) {
					throw new NotFoundException("Branch not found");
				}
				if(branch_.get().getCompany().getId() != company_.get().getId()) {
					throw new InvalidOperationException("Branch does not belong to the specified company");
				}
				user.setBranch(branch_.get());
				
				
				
				if(user.getUsername().contains("@")) {
					throw new InvalidEntryException("Invalid Entry in username");
				}
				
				user.setUsername(user.getUsername() + "@" + company_.get().getDomain().replace(" ", ""));
				
				
			}else if(user.getType().equals("SYSTEM-ROOT-USER") || user.getType().equals("SYSTEM-USER")) {
				user.setCompany(null);
				user.setBranch(null);
			}else {
				throw new InvalidOperationException("User type is invalid.");
			}
			
			user.setCode(this.requestUserCode().getCode());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
		}else {
			User userToUpdate = userRepository.findById(user.getId()).get();
			if(!userToUpdate.getCode().equals(user.getCode())) {
				throw new InvalidOperationException("Changing user code is not allowed");
			}
			if(user.getPassword().equals("") || user.getPassword().equals(null)) {
				user.setPassword(userToUpdate.getPassword());
			}else {
				user.setPassword(passwordEncoder.encode(user.getPassword()));
			}
			//user.setActive(true);// use this in the mean time before implementing actiavate and deactivate user
		
			user.setCompany(userToUpdate.getCompany());
			user.setBranch(userToUpdate.getBranch());
		}
		
		user = userRepository.saveAndFlush(user);
		
		/**
		 * First get user roles
		 */
		Collection<Role> roleCollection = user.getRoles();		
		List<Role> roles = List.copyOf(roleCollection);
		
		return user;
	}
	
	private boolean validateUser(User user) {
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
	public Role saveRole(Role role, HttpServletRequest request) {		
		if(role.getName().equalsIgnoreCase("ROOT")) {
			Optional<Role> r = roleRepository.findByName("ROOT");
			if(r.isPresent()) {
				throw new InvalidOperationException("Can not modify the ROOT role");
			}
		}
		try {
			if(this.getUser(request).getCompany() != null) {
				role.setCompany(this.getUser(request).getCompany());
				role.setName(role.getName() + "-" + this.getUser(request).getCompany().getId().toString());				
			}
		}catch(Exception e) {}
			
		return roleRepository.save(role);
	}

	@Override
	public void addRoleToUser(String username, String rolename, HttpServletRequest request) {
		User user = userRepository.findByUsername(username).get();
		Role role = roleRepository.findByName(rolename).get();
		try {
			if(!user.getRoles().contains(role)) {
				user.getRoles().add(role);
			}
		}catch(Exception e) {
			log.info(e.getMessage());
		}					
	}

	@Override
	public User getUser(String username) {
		return userRepository.findByUsername(username).get();
	}

	@Override
	public List<UserResponseDTO> getUsers() {
		log.info("Fetching all users");
		List<UserResponseDTO> userList = new ArrayList<>();
		for(User user : userRepository.findAll()) {
			userList.add(userResponseDTOMapper(user));
		}
		return userList;
	}

	@Override
	public Privilege savePrivilege(Privilege privilege, HttpServletRequest request) {
		
		Optional<Privilege> p = privilegeRepository.findByName(privilege.getName());
		if(p.isPresent()) {
			throw new InvalidOperationException("Privilege already exist");
		}
		
		log.info("Saving new privilege to the database");
		return privilegeRepository.save(privilege);
	}

	@Override
	public void addPrivilegeToRole(String roleName, String privilegeName) {
		Role role = roleRepository.findByName(roleName).get();
		Optional<Privilege> p = privilegeRepository.findByName(privilegeName);
		
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
		Role role = roleRepository.findByName(roleName).get();
		Optional<Privilege> p = privilegeRepository.findByName(privilegeName);
		
		try {
			role.getPrivileges().remove(p.get());			
		}catch(Exception e) {
			log.info(e.getMessage());
		}			
	}
	
	@Override
	public List<Role> getRoles() {
		log.info("Fetching all roles");
		return roleRepository.findAll();
	}

	@Override
	public List<RoleResponseDTO> getRolesCustom() {
		log.info("Fetching all roles");	
		
		List<RoleResponseDTO> roleList = new ArrayList<>();
		
		for(Role role : roleRepository.findAll()) {
			char character = '-';
			RoleResponseDTO roleResponse = new RoleResponseDTO();
			roleResponse.setId(role.getId().toString());
			roleResponse.setName(removeFromCharacter(role.getName(), character));
			roleResponse.setOwner(role.getOwner());
			if(role.getCompany() != null) {
				roleResponse.setCompanyName(role.getCompany().getName());
			}
			roleList.add(roleResponse);
		}		
		return roleList;
	}
	
	@Override
	public User getUserById(Long id) {
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
	public boolean deleteUser(User user) {
		/**
		 * Delete a user if a user is deletable
		 */
		if(allowDeleteUser(user) == false) {
			throw new InvalidOperationException("Deleting this user is not allowed");
		}
		userRepository.delete(user);
		return true;
	}
	
	private boolean allowDeleteUser(User user) {
		
		return false;
	}

	@Override
	public Role getRole(String name) {
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
		Collection<Privilege> privileges = roleRepository.findByName(roleName).get().getPrivileges();
		List<String> privilegesList = new ArrayList<String>();
		for(Privilege privilege : privileges) {
			privilegesList.add(privilege.getName());
		}
		return privilegesList;
	}

	@Override
	public Role getRoleById(Long id) {	
		
		Role role = roleRepository.findById(id).get();
		
		char character = '-';
		
		String readalbeRoleName = removeFromCharacter(role.getName(), character);
		
		
		role.setName(readalbeRoleName);
		
		return role;
	}
	
	public static String removeFromCharacter(String input, char character) {
        // Find the index of the specified character
        int index = input.indexOf(character);
        
        // If the character exists, return the substring up to that index
        if (index != -1) {
            return input.substring(0, index);
        }
        
        // If the character is not found, return the original string
        return input;
    }

	@Override
	public boolean deleteRole(Role role) {
		/**
		 * Delete a role if a role is deletable
		 */
		if(allowDeleteRole(role) == false) {
			throw new InvalidOperationException("Deleting this role is not allowed");
		}
		roleRepository.delete(role);
		return true;
	}
	
	private boolean allowDeleteRole(Role role) {
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
			User user = userRepository.findByUsername(username).get();
			shortcut.setUser(user);
			shortcut.setName(name);
			shortcut.setLink(link);
			Optional<User> userExist = shortcutRepository.findByLinkAndUser(link, user);
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
			User user = userRepository.findByUsername(username).get();
			
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
			User user = userRepository.findByUsername(username).get();
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
	public User getUser(HttpServletRequest request) {
		return userRepository.findByUsername(request.getUserPrincipal().getName()).get();
	}
	
	
	@Override
	public ApiCustomResponse activateUser(UserRequestDTO user, HttpServletRequest request) {
		Optional<User> user_ = userRepository.findById(user.getId());		
		if(user_.isEmpty()) {
			throw new NotFoundException("User not found");
		}		
		if(user_.get().isActive() == true) {
			throw new InvalidOperationException("User already active");
		}
		user_.get().setActive(true);
		userRepository.save(user_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "User Activated successifully");
	}
	
	@Override
	public ApiCustomResponse deactivateUser(UserRequestDTO user, HttpServletRequest request) {
		Optional<User> user_ = userRepository.findById(user.getId());		
		if(user_.isEmpty()) {
			throw new NotFoundException("User not found");
		}
		if(user_.get().getUsername().equals("root")) {
			throw new InvalidOperationException("Can not deactivate the root user");
		}
		if(user_.get().isActive() == false) {
			throw new InvalidOperationException("User already inactive");
		}
		user_.get().setActive(false);
		userRepository.save(user_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "User Activated successifully");
	}
	
	public RecordModel requestUserCode() {
		Long id = 1L;
		try {
			id = userRepository.getLastId() + 1;
		}catch(Exception e) {
			
		}
		RecordModel model = new RecordModel();
		model.setCode("USR-"+Formatter.formatTwelvePlain(id.toString()));
		model.setNo("USR-"+Formatter.formatTwelvePlain(id.toString()));
		return model;
	}
	
	
	private UserResponseDTO userResponseDTOMapper(User user) {
		UserResponseDTO userResponse = new UserResponseDTO();
		
		userResponse.setId(user.getId().toString());
		userResponse.setCode(user.getCode());
		userResponse.setUsername(user.getUsername());
		userResponse.setFirstName(user.getFirstName());
		userResponse.setMiddleName(user.getMiddleName());
		userResponse.setLastName(user.getLastName());
		userResponse.setNickname(user.getNickname());
		userResponse.setType(user.getType());
		if(user.getCompany() != null) {
			userResponse.setCompanyName(user.getCompany().getName());
		}
		if(user.getBranch() != null) {
			userResponse.setBranchName(user.getBranch().getName());
		}
		if(user.isActive()) {
			userResponse.setActive("Active");
		}else {
			userResponse.setActive("Inactive");
		}		
		return userResponse;
	}

	@Override
	public Company getUserCompany(HttpServletRequest request) {
		return userRepository.findByUsername(request.getUserPrincipal().getName()).get().getCompany();
	}

	@Override
	public Branch getUserBranch(HttpServletRequest request) {
		return userRepository.findByUsername(request.getUserPrincipal().getName()).get().getBranch();
	}
}
