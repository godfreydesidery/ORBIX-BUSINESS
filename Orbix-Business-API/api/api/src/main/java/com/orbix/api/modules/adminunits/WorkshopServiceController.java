package com.orbix.api.modules.adminunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkshopServiceController implements WorkshopService {
	
	private final BranchRepository branchRepository;
	private final WorkshopRepository workshopRepository;
	private final UserService userService;
	private final DayService dayService;
	
	/**
	 * 
	 */
	@Override
	public List<WorkshopResponseDTO> getAllWorkshops(HttpServletRequest request) {
		List<Workshop> workshops = workshopRepository.findAll();
		List<WorkshopResponseDTO> workshopResponses = new ArrayList<>();

		for(Workshop workshop : workshops) {
			workshopResponses.add(workshopResponseDTOMapper(workshop));					
		}		
		return workshopResponses;
	}

	/**
	 * 
	 */
	@Override
	public WorkshopResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Workshop> _workshop = workshopRepository.findById(id);
		if(_workshop.isEmpty()) {
			throw new NotFoundException("Workshop not found");
		}		
		return workshopResponseDTOMapper(_workshop.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public WorkshopResponseDTO createWorkshop(
			WorkshopRequestDTO workshopRequest, 
			HttpServletRequest request) {
		
		
		
		Optional<Branch> branch_ = branchRepository.findById(userService.getUser(request).getBranch().getId());
		if(branch_.isEmpty()) {
			throw new InvalidEntryException("Branch not found in database");
		}
		
		Workshop workshop = new Workshop();
		workshop.setCode(workshopRequest.getCode());
		workshop.setName(workshopRequest.getName());
		workshop.setLocationName(workshopRequest.getLocationName());		
		workshop.setBranch(branch_.get());
		
		workshop.setWorkshopCategory(workshopRequest.getWorkshopCategory());
		
		workshop.setCreatedByUser(userService.getUser(request));
		workshop.setCreatedDateTime(dayService.getTimeStamp());
		
		workshop = workshopRepository.save(workshop);
		/**Create  company code*/
		workshop.setCode("SHOP/"+ workshop.getId().toString());
		workshop = workshopRepository.save(workshop);
		
		return workshopResponseDTOMapper(workshop);
	}

	@Override
	public WorkshopResponseDTO updateWorkshop(WorkshopRequestDTO workshopRequest, HttpServletRequest request) {

		Optional<Workshop> workshop_ = workshopRepository.findById(workshopRequest.getId());
		if(workshop_.isEmpty()) {
			throw new NotFoundException("Workshop not be found in database");
		}		
		if(!validateWorkshopData(workshopRequest)) {
			throw new InvalidEntryException("Could not validate workshop data");
		}	
		
		Workshop workshop = workshop_.get();
		workshop.setName(workshopRequest.getName());
		workshop.setLocationName(workshopRequest.getLocationName());
		workshop.setWorkshopCategory(workshopRequest.getWorkshopCategory());
		workshop = workshopRepository.save(workshop);		
		return workshopResponseDTOMapper(workshop);
	}
	
	private WorkshopResponseDTO workshopResponseDTOMapper(Workshop workshop) {
		WorkshopResponseDTO workshopResponse = new WorkshopResponseDTO();
		workshopResponse.setId(workshop.getId().toString());
		workshopResponse.setCode(workshop.getCode());
		workshopResponse.setName(workshop.getName());
		workshopResponse.setLocationName(workshop.getLocationName());
		workshopResponse.setWorkshopCategory(String.valueOf(workshop.getWorkshopCategory()));
		workshopResponse.setBranchId(workshop.getBranch().getId().toString());		
		if(workshop.isActive()) {
			workshopResponse.setActive("Active");
		}else {
			workshopResponse.setActive("Inactive");
		}
		workshopResponse.setOtherInfo("Branch: " + workshop.getBranch().getName() + " Location: " + workshop.getLocationName());
		return workshopResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request) {
		Optional<Workshop> workshop_ = workshopRepository.findById(workshop.getId());		
		if(workshop_.isEmpty()) {
			throw new NotFoundException("Workshop not found");
		}		
		if(workshop_.get().isActive() == true) {
			throw new InvalidOperationException("Workshop already active");
		}
		workshop_.get().setActive(true);
		workshopRepository.save(workshop_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Workshop Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request) {
		Optional<Workshop> workshop_ = workshopRepository.findById(workshop.getId());		
		if(workshop_.isEmpty()) {
			throw new NotFoundException("Workshop not found");
		}		
		if(workshop_.get().isActive() == false) {
			throw new InvalidOperationException("Workshop already inactive");
		}
		workshop_.get().setActive(false);
		workshopRepository.save(workshop_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Workshop Deactivated successifully");
	}
	
	/**
	 * 
	 * @param workshopRequest
	 * @return
	 */
	boolean validateWorkshopData(WorkshopRequestDTO workshopRequest) {
		
		return true;
	}

	@Override
	public List<WorkshopResponseDTO> getBranchAvailableWorkshopsByUser(HttpServletRequest request) {
		User user = userService.getUser(request);
		List<Workshop> workshops = workshopRepository.findAllByBranch(user.getBranch());
		
		List<WorkshopResponseDTO> workshopResponses = new ArrayList<>();

		for(Workshop workshop : workshops) {
			workshopResponses.add(workshopResponseDTOMapper(workshop));					
		}		
		return workshopResponses;
	}
	
	@Override
	public WorkshopResponseDTO getSelectedWorkshop(
			Long id, 
			HttpServletRequest request) {
		Optional<Workshop> _workshop = workshopRepository.findByIdAndBranch(id, userService.getUser(request));
		if(_workshop.isEmpty()) {
			throw new NotFoundException("Workshop not found");
		}		
		return workshopResponseDTOMapper(_workshop.get());	
	}
	
	@Override
	public List<WorkshopResponseDTO> getBranchWorkshops(HttpServletRequest request) {
		List<Workshop> workshops = workshopRepository.findAllByBranch(userService.getUserBranch(request));
		
		List<WorkshopResponseDTO> workshopResponses = new ArrayList<>();

		for(Workshop workshop : workshops) {
			workshopResponses.add(workshopResponseDTOMapper(workshop));					
		}		
		return workshopResponses;
	}
}
