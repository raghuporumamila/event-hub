package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event", schema = "event")
public class Event implements Serializable {
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
	@Column(name = "payload", columnDefinition = "text")
	private String payload;
	@ManyToOne
	@JoinColumn(name = "event_definition_id")
	private EventDefinition eventDefinition;
}
