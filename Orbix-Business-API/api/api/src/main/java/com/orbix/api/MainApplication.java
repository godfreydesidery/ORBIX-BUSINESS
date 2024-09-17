package com.orbix.api;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orbix.api.domain.CompanyProfile;
import com.orbix.api.domain.Day;
import com.orbix.api.domain.Privilege;
import com.orbix.api.domain.Role;
import com.orbix.api.domain.User;
import com.orbix.api.repositories.CompanyProfileRepository;
import com.orbix.api.repositories.DayRepository;
import com.orbix.api.repositories.PrivilegeRepository;
import com.orbix.api.repositories.RoleRepository;
import com.orbix.api.repositories.UserRepository;
import com.orbix.api.security.Object_;
import com.orbix.api.security.Operation;
import com.orbix.api.service.CompanyProfileService;
import com.orbix.api.service.DayService;
import com.orbix.api.service.UserService;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication()
@ComponentScan(basePackages={"com.orbix.api"})
@EnableJpaAuditing
@EnableAutoConfiguration
@EnableSwagger2
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Data
@RequiredArgsConstructor
public class MainApplication {
	protected ConfigurableApplicationContext springContext;

    DayRepository dayRepository;
    CompanyProfileRepository companyProfileRepository;
    UserService userService;
    private final PrivilegeRepository privilegeRepository;
    

	private final RoleRepository roleRepository;
	private final DayService dayService;
	private final UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Don't do this in production, use a proper list  of allowed origins
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
	}
	
	@PostConstruct
	  public void setUp() {
	    objectMapper.registerModule(new JavaTimeModule());
	  }
	
	public static void main(String[] args) throws Throwable {
		SpringApplication.run(MainApplication.class, args);
		
		
		
	}
	
	
	
	
	
	
	
	@Bean
	void updateRecords() {
		//thread to update patient records periodically
		UpdatePatient updatePatient = new UpdatePatient(
		
				dayService
				);
	    Thread updatePatientThread = new Thread(updatePatient);
	    updatePatientThread.start();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	CommandLineRunner run(UserService userService, DayService dayService, CompanyProfileService companyProfileService) {
		return args -> {
			if(!companyProfileService.hasData()) {
				log.info("Creating mock company");
				CompanyProfile company = new CompanyProfile(null, "Company Name","Contact Name", null, "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "NAN", "", "", 0, "", "NAN");
				companyProfileService.saveCompanyProfile(company);
			}
			
			if(!dayService.hasData()) {
				/**
				 * Creating the first day
				 */
				log.info("Creating the first day "+(new Day()).toString());
				dayService.saveDay(new Day());
			}
			
			List<String> roleNames = new ArrayList<>();
			roleNames.add("ROOT");
			roleNames.add("ADMIN");
			roleNames.add("RECEPTION");
			roleNames.add("CASHIER");
			roleNames.add("HUMAN-RESOURCE");
			roleNames.add("PROCUREMENT");
			roleNames.add("MANAGER");
			roleNames.add("ACCOUNTANT");
			//roleNames.add("STORE-KEEPER");
			roleNames.add("STORE-PERSON");
			roleNames.add("MANAGEMENT");
			roleNames.add("CLINICIAN");
			roleNames.add("NURSE");
			roleNames.add("PHARMACIST");
			roleNames.add("LABORATORIST");
			roleNames.add("RADIOGRAPHER");
			roleNames.add("RADIOLOGIST");
			
			for(String roleName : roleNames) {
				if(!roleRepository.existsByName(roleName)) {
					try {
						userService.saveRole(new Role(null, roleName, "SYSTEM", null), null);
					}catch(Exception e) {}	
				}
			}
			
			List<Role> rs = roleRepository.findAllByOwner(null);
			for(Role r : rs) {
				r.setOwner("SYSTEM");
				roleRepository.save(r);
			}
			
			if(!userRepository.existsByUsername("root")) {
				try {
					userService.saveUser(new User(null, "ROOT", "Root", "Root", "Root", "Root@Root", "root", "r00tpA55", null, true, new ArrayList<>(), null, null, LocalDateTime.now()), null);
				}catch(Exception e) {}	
			}
					
			try {
				userService.addRoleToUser("root", "ROOT", null);
			}catch(Exception e) {}		
			
			Field[] objectFields = Object_.class.getDeclaredFields();
			//Field[] operationFields = Operation.class.getDeclaredFields();
			for(int i = 0; i < objectFields.length; i++) {
				//String objectWithProhibition = objectFields[i].get(objectFields[i].getName()).toString();
				String objectWithAllowedOperation = objectFields[i].get(objectFields[i].getName()).toString();
				//String prohibitedSequence = "";
				String allowedSequence = "";
				String object = "";
				if(objectWithAllowedOperation.contains("-")) {								        
					allowedSequence = objectWithAllowedOperation.substring(objectWithAllowedOperation.lastIndexOf("-") + 1);
				}else {
					allowedSequence = "";
				}
				if(allowedSequence.equals("")) {
					//object = objectWithProhibition;
					object = "";
				}else {
					//object = objectWithProhibition.substring(0, objectWithProhibition.indexOf("-"));
					object = objectWithAllowedOperation.substring(0, objectWithAllowedOperation.indexOf("-"));
				}
				//List<String> prohibitedOperations = new ArrayList<>();
				List<String> allowedOperations = new ArrayList<>();
				Scanner sc = new Scanner(allowedSequence);
				if(!allowedSequence.equals("")) {
					while (sc.hasNext()) {
						allowedOperations.add(sc.next());						
					}
					sc.close();
				}
				
				for(String allowedOperation : allowedOperations) {
					Privilege privilege = new Privilege();
					privilege.setName(object+"-"+allowedOperation);
					
					try {
						if(!privilegeRepository.existsByName(privilege.getName())) {
							userService.savePrivilege(privilege, null);
						}
					}catch(Exception e) {
						System.out.println("Could not save privilege");
					}
					
				}
				
			}
			try {
				userService.addPrivilegeToRole("ROOT", "ADMIN-ACCESS");
			}catch(Exception e) {}	
			try {
				userService.addPrivilegeToRole("ROOT", "USER-ALL");				
			}catch(Exception e) {}	
			try {
				userService.addPrivilegeToRole("ROOT", "ROLE-ALL");	
			}catch(Exception e) {}	
			
			Field[] operationFields = Operation.class.getDeclaredFields();
			List<String> operations = new ArrayList<>();
			for(int i = 0; i < operationFields.length; i++) {
				String operation = operationFields[i].get(operationFields[i].getName()).toString();
				try {
					operations.add(operation);
				}catch(Exception e) {}
			}
			List<Privilege> destroyedPrivileges = new ArrayList<>();	
			
			for(Role role : roleRepository.findAll()) {
				for(Privilege privilege : role.getPrivileges()) {
					String op2 = privilege.getName().substring(privilege.getName().lastIndexOf("-") + 1);
					if(!operations.contains(op2)) {
						userService.removePrivilegeFromRole(role.getName(), privilege.getName());
						if(!destroyedPrivileges.contains(privilege)) {
							destroyedPrivileges.add(privilege);
						}
					}
				}
			}
			
			for(Privilege privilege : privilegeRepository.findAll()) {
				String op2 = privilege.getName().substring(privilege.getName().lastIndexOf("-") + 1);
				if(!operations.contains(op2)) {
					if(!destroyedPrivileges.contains(privilege)) {
						destroyedPrivileges.add(privilege);
					}
				}
			}
			
			for(Privilege privilege : destroyedPrivileges) {
				privilegeRepository.delete(privilege);
			}
			
			
		};
	}
	
	@Bean
   public Docket erpApi() {
      return new Docket(DocumentationType.SWAGGER_2).select()
         .apis(RequestHandlerSelectors.basePackage("com.orbix.api")).build();
   }
	
	@Bean
	public CommonsMultipartResolver multipartResolver() {
	    CommonsMultipartResolver multipart = new CommonsMultipartResolver();
	    multipart.setMaxUploadSize(50 * 1024 * 1024); //maximum 50MB
	    return multipart;
	}

	@Bean
	@Order(0)
	public MultipartFilter multipartFilter() {
	    MultipartFilter multipartFilter = new MultipartFilter();
	    multipartFilter.setMultipartResolverBeanName("multipartResolver");
	    return multipartFilter;
	}
	
	
	
}
