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
@Table(name = "organization", schema = "security")
public class Organization {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String name;
	private String address;
	private String address2;
	private String city;
	private String country;
	private String state;
	@Column(name = "postalcode")
	private String postalCode;
	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
	private List<SourceType> sourceTypes;
}
