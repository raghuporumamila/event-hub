package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event", schema = "event")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	@ManyToOne
	@JoinColumn(name = "source_id")
	private Source source;
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	private String timestamp;
	private String payload;
}
