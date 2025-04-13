package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class StorageGoodReleaseRequestDTO {
	private Long storageId;
	private double qty;
}
