package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_definition", schema = "event")
public class EventDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	@ManyToOne
	@JoinColumn(name = "source_id")
	private Source source;
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	private String eventName;
	@Column(name = "payload_schema", columnDefinition = "text")
	private String payloadSchema;
}
