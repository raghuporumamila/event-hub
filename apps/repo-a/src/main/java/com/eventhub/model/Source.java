package com.eventhub.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "source", schema = "event")
public class Source implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private SourceTypeEnum type;
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	@ManyToOne
	@JoinColumn(name = "org_id")
	private Organization organization;
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JsonManagedReference
	@JoinTable(
			name = "integration",
			schema = "event",// The physical link table
			joinColumns = @JoinColumn(name = "source_id"),
			inverseJoinColumns = @JoinColumn(name = "target_id")
	)
	private List<Target> targets;
}
