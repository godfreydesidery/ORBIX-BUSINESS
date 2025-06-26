package com.orbix.api.api.vehicleandequipmentparking;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyParkingStatusResponseDTO {
	private int month;        // 1 = Jan, ..., 12 = Dec
    private long checkedIn;   // Count of vehicles checked in that month
    private long checkedOut;  // Count of vehicles checked out that month
    
    public String getMonthName() {
        return Month.of(this.month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}
