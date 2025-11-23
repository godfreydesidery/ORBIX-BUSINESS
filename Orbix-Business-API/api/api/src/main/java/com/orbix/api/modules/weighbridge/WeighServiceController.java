package com.orbix.api.modules.weighbridge;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.SystemProfileRepository;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialist;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistRepository;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistResponseDTO;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistServiceController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WeighServiceController implements WeighService {
	
	private final WeighRepository weighRepository;
	private final UserService userService;
	private final DayService dayService;
	private final BranchRepository branchRepository;

	@Override
	public List<WeighResponseDTO> getAllWeighs(HttpServletRequest request) {
//		List<Weigh> weighs = weighRepository.findAll();
//		List<WeighResponseDTO> weighResponses = new ArrayList<>();
//
//		for(Weigh weigh : weighs) {
//			weighResponses.add(weighResponseDTOMapper(weigh));					
//		}		
//		return weighResponses;
		
		LocalDateTime last48Hours = LocalDateTime.now().minusHours(48);
		List<Weigh> weighs = weighRepository.findByCreatedDateTimeAfter(last48Hours);
		List<WeighResponseDTO> weighResponses = new ArrayList<>();

		for (Weigh weigh : weighs) {
			weighResponses.add(weighResponseDTOMapper(weigh));
		}
		return weighResponses;
	}

	@Override
	public List<WeighResponseDTO> getAllPendingOrCheckedInWeighs(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllPendingOrCheckedInWeighsByWarehouse(Long warehouseId,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllWithDiscounts(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllCheckedInWeighsByWarehouse(Long warehouseId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllRecentCheckedOutWeighsByWarehouse(Long warehouseId,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllCleared(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getTodayCheckedOut(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WeighResponseDTO> getAllCheckedInWeighs(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WeighResponseDTO get(Long id, HttpServletRequest request) {
		Optional<Weigh> weigh_ = weighRepository.findById(id);
		if (weigh_.isEmpty()) {
			throw new NotFoundException("Record not found");
		};
		
		return weighResponseDTOMapper(weigh_.get());
	}

	@Override
	public List<WeighBillReceivableResponseDTO> getWeighBillReceivables(Long id, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WeighResponseDTO createWeigh(WeighRequestDTO weighRequest, HttpServletRequest request) {
		
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}
		
		Weigh weigh = new Weigh();
		weigh.setNo(String.valueOf(Math.random()));
		weigh.setRefNo(weighRequest.getRegNo());
		weigh.setRegNo(weighRequest.getRegNo());
		weigh.setOwnerPhoneNo(weighRequest.getOwnerPhoneNo());
		weigh.setOwnerFirstName(weighRequest.getOwnerName());
		weigh.setOwnerLastName(weighRequest.getOwnerName());
		weigh.setWeighStatus(weighRequest.getWeighStatus());
		weigh.setBranch(branch_.get());
		weigh.setCreatedByUser(userService.getUser(request));
		weigh.setCreatedDateTime(dayService.getTimeStamp());
		weigh = weighRepository.save(weigh);
		weigh.setNo("W" + weigh.getId().toString());
		weigh = weighRepository.save(weigh);
		
		WeighResponseDTO response = new WeighResponseDTO();
		
		response.setId(weigh.getId().toString());
		response.setNo(weigh.getNo());
		response.setOwnerName(weigh.getOwnerFirstName());
		response.setRegNo(weigh.getRegNo());
		response.setRefNo(weigh.getRegNo());
		
		return response;
		
	}

	@Override
	public WeighResponseDTO updateWeigh(WeighRequestDTO weighRequest, HttpServletRequest request) {
		Optional<Weigh> weigh_ = weighRepository.findById(weighRequest.getId());
		if (weigh_.isEmpty()) {
			throw new NotFoundException("Record not found");
		}

		Weigh weigh = weigh_.get();
		weigh.setRefNo(weighRequest.getRegNo());
		weigh.setRegNo(weighRequest.getRegNo());
		weigh.setOwnerPhoneNo(weighRequest.getOwnerPhoneNo());
		weigh.setOwnerFirstName(weighRequest.getOwnerName());
		weigh.setOwnerLastName(weighRequest.getOwnerName());
		weigh.setWeighStatus(weighRequest.getWeighStatus());

		weigh = weighRepository.save(weigh);

		return weighResponseDTOMapper(weigh);
	}

	@Override
	public WeighResponseDTO checkIn(WeighRequestDTO weighRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WeighResponseDTO checkOut(WeighRequestDTO weighRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WeighBillReceivableResponseDTO createWeighBillReceivable(Long weighId, LocalDateTime startedAt,
			LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private WeighResponseDTO weighResponseDTOMapper(Weigh weigh) {
		WeighResponseDTO weighResponse = new WeighResponseDTO();
		
		weighResponse.setId(weigh.getId().toString());
		weighResponse.setNo(weigh.getNo());
		weighResponse.setOwnerName(weigh.getOwnerFirstName());
		weighResponse.setOwnerPhoneNo(weigh.getOwnerPhoneNo());
		weighResponse.setRegNo(weigh.getRegNo());
		weighResponse.setRefNo(weigh.getRegNo());
		weighResponse.setWeighStatus(weigh.getWeighStatus());
		weighResponse.setRecheck(String.valueOf(weigh.getRecheck()));
		weighResponse.setOwnerFirstName(weigh.getOwnerFirstName());

		
		return weighResponse;
	}

	@Override
	public boolean recheck(Long id) {
		Optional<Weigh> weigh_ = weighRepository.findById(id);
		Weigh weigh = weigh_.get();
		weigh.setRecheck(weigh.getRecheck() + 1);
		weighRepository.save(weigh);
		return true;
	}

}
