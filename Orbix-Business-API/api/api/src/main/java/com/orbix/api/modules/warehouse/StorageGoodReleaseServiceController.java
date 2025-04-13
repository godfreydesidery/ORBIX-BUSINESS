package com.orbix.api.modules.warehouse;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StorageGoodReleaseServiceController implements StorageGoodReleaseService {
	
	private final StorageRepository storageRepository;
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	private final StorageGoodReleaseRepository storageGoodReleaseRepository;
	
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public StorageGoodReleaseResponseDTO createStorageGoodRelease(
			StorageGoodReleaseRequestDTO storageGoodreleaseRequest, HttpServletRequest request) {
		
		Storage storage = storageRepository.findById(storageGoodreleaseRequest.getStorageId())
                .orElseThrow(() -> new NotFoundException("Storage with ID " + storageGoodreleaseRequest.getStorageId() + " not found."));
		
		double qtyToRelease = storageGoodreleaseRequest.getQty();
		double qtyReleased = 0;
		double cleared = 0;
		double qtyCanRelease = 0;
		
		if(qtyToRelease <= 0) {
			throw new InvalidOperationException("zero or negative is not allowed");
		}
		
		List<StorageGoodRelease> sgrs = storageGoodReleaseRepository.findAllByStorage(storage);
		
		for(StorageGoodRelease sgr : sgrs) {
			if(sgr.getStatus().equals("APPROVED")) {
				qtyReleased = qtyReleased + sgr.getQty();
			}		
		}
		
		List<StorageBillReceivable> sbrvs = storageBillReceivableRepository.findAllByStorage(storage);
		
		for(StorageBillReceivable sbrv : sbrvs) {
			if(sbrv.getBillReceivable().getPayStatus().toString().equals("PAID")) {
				cleared = cleared + sbrv.getQty();
			}
		}
		
		qtyCanRelease = cleared - qtyReleased;
		
		if(qtyToRelease > qtyCanRelease) {
			throw new InvalidOperationException("Releasing more than qty available for release");
		}
		
		storage.setCurrentQty(storage.getCurrentQty() - qtyToRelease);
		
		storage = storageRepository.save(storage);
		
		StorageGoodRelease storageGoodRelease = new StorageGoodRelease();
		
		storageGoodRelease.setNo(storage.getNo() + Math.random());
		storageGoodRelease.setQty(qtyToRelease);
		
		storageGoodRelease.setStatus("APPROVED");
		storageGoodRelease.setStorage(storage);
		storageGoodRelease.setCreatedByUser(userService.getUser(request));
		storageGoodRelease.setCreatedDateTime(dayService.getTimeStamp());
		storageGoodRelease.setApprovedByUser(userService.getUser(request));
		storageGoodRelease.setApprovedDateTime(dayService.getTimeStamp());
		
		storageGoodRelease = storageGoodReleaseRepository.save(storageGoodRelease);
		
		storageGoodRelease.setNo(storage.getNo() + "/" + storageGoodRelease.getId().toString());
		
		storageGoodRelease = storageGoodReleaseRepository.save(storageGoodRelease);
		
		return storageGoodReleaseResponseDTOMapper(storageGoodRelease);
		
	}
	
	@Override
	public StorageGoodReleaseDetail showStorageGoodReleaseDetail(Long storageId, HttpServletRequest request) {
		Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new NotFoundException("Storage with ID " + storageId + " not found."));
		
		double initialQty = storage.getInitialQty();
		double availableQty = storage.getCurrentQty();
		double qtyReleased = 0;
		double cleared = 0;
		double qtyCanRelease = 0;
		
		List<StorageGoodRelease> sgrs = storageGoodReleaseRepository.findAllByStorage(storage);
		
		for(StorageGoodRelease sgr : sgrs) {
			if(sgr.getStatus().equals("APPROVED")) {
				qtyReleased = qtyReleased + sgr.getQty();
			}		
		}
		
		List<StorageBillReceivable> sbrvs = storageBillReceivableRepository.findAllByStorage(storage);
		
		for(StorageBillReceivable sbrv : sbrvs) {
			if(sbrv.getBillReceivable().getPayStatus().toString().equals("PAID")) {
				cleared = cleared + sbrv.getQty();
			}
		}
		
		qtyCanRelease = cleared - qtyReleased;
		
		StorageGoodReleaseDetail storageGoodReleaseDetail = new StorageGoodReleaseDetail();
		storageGoodReleaseDetail.setAvailableForRelease(qtyCanRelease);
		storageGoodReleaseDetail.setCurrentQty(availableQty);
		storageGoodReleaseDetail.setInitialQty(initialQty);
		storageGoodReleaseDetail.setReleasedQty(qtyReleased);
		
		return storageGoodReleaseDetail;
	}
	
	@Override
	public List<StorageGoodReleaseResponseDTO> getStorageGoodReleases(Long storageId, HttpServletRequest request) {
		Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new NotFoundException("Storage with ID " + storageId + " not found."));
		
		List<StorageGoodRelease> storageGoodReleases = storageGoodReleaseRepository.findAllByStorage(storage);
		List<StorageGoodReleaseResponseDTO> StorageGoodReleaseResponses = new ArrayList<>();
		for(StorageGoodRelease storageGoodRelease : storageGoodReleases) {
			StorageGoodReleaseResponseDTO storageGoodReleaseResponse = storageGoodReleaseResponseDTOMapper(storageGoodRelease);
			StorageGoodReleaseResponses.add(storageGoodReleaseResponse);			
		}
		return StorageGoodReleaseResponses;
	}
	
	private StorageGoodReleaseResponseDTO storageGoodReleaseResponseDTOMapper(StorageGoodRelease storageGoodRelease) {
		StorageGoodReleaseResponseDTO storageGoodReleaseResponse = new StorageGoodReleaseResponseDTO();		
		storageGoodReleaseResponse.setId(storageGoodRelease.getId().toString());		
		storageGoodReleaseResponse.setNo(storageGoodRelease.getNo());
		storageGoodReleaseResponse.setQty(String.valueOf(storageGoodRelease.getQty()));
		storageGoodReleaseResponse.setStatus(storageGoodRelease.getStatus());
		storageGoodReleaseResponse.setStorageId(storageGoodRelease.getStorage().getId().toString());
		storageGoodReleaseResponse.setReleaseDate(
			    storageGoodRelease.getApprovedDateTime() != null
			        ? storageGoodRelease.getApprovedDateTime().toString()
			        : ""
			);
		storageGoodReleaseResponse.setClientName(storageGoodRelease.getStorage().getOwnerFirstName() + " " + storageGoodRelease.getStorage().getOwnerLastName());;
		storageGoodReleaseResponse.setGoodName(storageGoodRelease.getStorage().getGoodName());
		storageGoodReleaseResponse.setUnitPrice(String.valueOf(storageGoodRelease.getStorage().getBillingAmount()));
		storageGoodReleaseResponse.setTotal(String.valueOf((storageGoodRelease.getQty() * storageGoodRelease.getStorage().getBillingAmount())));
		return storageGoodReleaseResponse;
	}

	@Override
	public StorageGoodReleaseResponseDTO get(Long id, HttpServletRequest request) {
		StorageGoodRelease storageGoodRelease = storageGoodReleaseRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Release note with ID " + id + " not found."));
		
		return storageGoodReleaseResponseDTOMapper(storageGoodRelease);
	}

	
}

@Data
class StorageGoodReleaseDetail{
	double initialQty;
	double currentQty;
	double releasedQty;
	double availableForRelease;
}
