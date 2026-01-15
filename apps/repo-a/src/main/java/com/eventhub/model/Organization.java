package com.eventhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "organization")
public class Organization {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	private String name;
	private String address;
	private String address2;
	private String city;
	private String country;
	private String state;
	private String postalCode;
	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<SourceType> sourceTypes;
}
