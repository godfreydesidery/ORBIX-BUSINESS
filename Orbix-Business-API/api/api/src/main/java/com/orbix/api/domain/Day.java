/**
 * 
 */
package com.orbix.api.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sun.istack.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author GODFREY
 *
 */
@Entity
@Data  
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "days")
public class Day {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;	
	@NotNull
	@Column(unique = true)
	private LocalDate bussinessDate = LocalDate.now();
	@Temporal(TemporalType.TIMESTAMP)
    private Date startedAt = new Date();
	@Temporal(TemporalType.TIMESTAMP)
    private Date endedAt = null;
	private String status = "STARTED";

}
