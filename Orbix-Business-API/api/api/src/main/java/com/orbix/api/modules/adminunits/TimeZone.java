package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data  
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "time_zones")
public class TimeZone {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "abbreviation", nullable = false, unique = true)
    private String abbreviation;

    @Column(name = "utc_offset", nullable = false)
    private String utcOffset;

    @Column(name = "dst_offset")
    private String dstOffset;

    @Column(name = "is_standard_time", nullable = false)
    private Boolean isStandardTime;

    @Column(name = "dst_start")
    private OffsetDateTime dstStart;

    @Column(name = "dst_end")
    private OffsetDateTime dstEnd;

    @Column(name = "country_region", unique = true)
    private String countryRegion;

    @Column(name = "iana_code", nullable = false, unique = true)
    private String ianaCode;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    private LocalDateTime createdDateTime = LocalDateTime.now();
}
