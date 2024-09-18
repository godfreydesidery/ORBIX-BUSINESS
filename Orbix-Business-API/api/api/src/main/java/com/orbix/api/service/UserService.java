/**
 * 
 */
package com.orbix.api.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.domain.Privilege1;
import com.orbix.api.domain.Role1;
import com.orbix.api.domain.Shortcut;
import com.orbix.api.domain.User1;

/**
 * @author GODFREY
 *
 */
public interface UserService {
	User1 saveUser(User1 user, HttpServletRequest request);
	Role1 saveRole(Role1 role, HttpServletRequest request);
	Privilege1 savePrivilege(Privilege1 privilege, HttpServletRequest request);
	void addRoleToUser(String username, String roleName, HttpServletRequest request);
	User1 getUser(String username);
	User1 getUserById(Long id);
	String getNicknameByUserId(Long id);
	boolean deleteUser(User1 user);
	List<User1>getUsers(); //edit this to limit the number, for perfomance.
	void addPrivilegeToRole(String roleName, String privilegeName);
	void removePrivilegeFromRole(String roleName, String privilegeName);
	List<Role1>getRoles(); // return all the roles
	Role1 getRole(String name);
	Role1 getRoleById(Long id);
	boolean deleteRole(Role1 role);
	List<String>getOperations();
	List<String>getObjects();
	List<String>getPrivileges(String roleName);
	boolean createShortcut(String username, String name, String link, HttpServletRequest request);
	boolean removeShortcut(String username, String name);
	List<Shortcut> loadShortcuts(String username);
	
	Long getUserId(HttpServletRequest request);
	User1 getUser(HttpServletRequest request);
	
}
