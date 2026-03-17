package com.eventhub.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "target", schema = "event")
public class Target implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private TargetTypeEnum type;
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	@ManyToOne
	@JoinColumn(name = "org_id")
	private Organization organization;
	private String credentials;
	@ManyToOne
	@JoinTable(
			name = "integration", // Must match the name above
			joinColumns = @JoinColumn(name = "target_id"),
			inverseJoinColumns = @JoinColumn(name = "source_id")
	)
	@JsonBackReference
	private Source source;
}
