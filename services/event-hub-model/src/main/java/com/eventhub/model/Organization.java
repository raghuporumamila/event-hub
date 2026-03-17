package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organization", schema = "security")
public class Organization implements Serializable {
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
	/*
	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Source> sources;*/
}
