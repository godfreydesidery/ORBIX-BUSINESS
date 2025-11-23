package com.orbix.api.modules.weighbridge;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WeighBillReceivableServiceController implements WeighBillReceivableService {

	private final UserService userService;

	private final WeighBillReceivableRepository weighBillReceivableRepository;
	private final WeighRepository weighRepository;
	private final BillReceivableRepository billReceivableRepository;
//	private final WeighGoodReleaseRepository weighGoodReleaseRepository;
	private final DayService dayService;

	@Override
	public List<WeighBillReceivableResponseDTO> getAllByWeigh(Long weighId, HttpServletRequest request) {
		Weigh weigh = weighRepository.findById(weighId)
				.orElseThrow(() -> new NotFoundException("Weigh with ID " + weighId + " not found."));

		List<WeighBillReceivable> weighBillReceivables = weighBillReceivableRepository.findAllByWeigh(weigh);

		List<WeighBillReceivableResponseDTO> weighBillReceivableResponses = new ArrayList<>();
		for (WeighBillReceivable weighBillReceivable : weighBillReceivables) {
			weighBillReceivableResponses.add(weighBillReceivableDTOMapper(weighBillReceivable));
		}

		return weighBillReceivableResponses;
	}
	
	private WeighBillReceivableResponseDTO weighBillReceivableDTOMapper(
			WeighBillReceivable weighBillReceivable) {

		WeighBillReceivableResponseDTO weighBillReceivableResponseDTO = new WeighBillReceivableResponseDTO();

		weighBillReceivableResponseDTO.setId(weighBillReceivable.getId().toString());
		weighBillReceivableResponseDTO
				.setDescription(weighBillReceivable.getDescription());
		weighBillReceivableResponseDTO.setPrice(String.valueOf(weighBillReceivable.getPrice()));
		weighBillReceivableResponseDTO.setQty(String.valueOf(weighBillReceivable.getQty()));
		weighBillReceivableResponseDTO.setWeightOne(String.valueOf(weighBillReceivable.getWeightOne()));
		weighBillReceivableResponseDTO.setWeightTwo(String.valueOf(weighBillReceivable.getWeightTwo()));
		weighBillReceivableResponseDTO.setWeightThree(String.valueOf(weighBillReceivable.getWeightThree()));
		weighBillReceivableResponseDTO.setWeightFour(String.valueOf(weighBillReceivable.getWeightFour()));
		weighBillReceivableResponseDTO.setWeighStatus(weighBillReceivable.getWeighStatus());
		weighBillReceivableResponseDTO
				.setPayStatus(weighBillReceivable.getBillReceivable().getPayStatus().toString());
		weighBillReceivableResponseDTO.setWeighId(String.valueOf(weighBillReceivable.getWeigh().getId()));
//		weighBillReceivableResponseDTO.setBillingType(weighBillReceivable.getWeigh().getBillingType());
		weighBillReceivableResponseDTO.setDiscount(String.valueOf(weighBillReceivable.getDiscount()));
//		weighBillReceivableResponseDTO.setDiscountStatus(
//				weighBillReceivable.getDiscountStatus() != null ? weighBillReceivable.getDiscountStatus() : "");
		weighBillReceivableResponseDTO
				.setAmount(String.valueOf(weighBillReceivable.getBillReceivable().getAmount()));

		return weighBillReceivableResponseDTO;

	}

	@Override
	public WeighBillReceivableResponseDTO getWeighBillReceivable(Long id, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
