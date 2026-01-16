package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "source", schema = "event")
public class Source {
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
}
