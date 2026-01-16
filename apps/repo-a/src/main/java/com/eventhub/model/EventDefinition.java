package com.eventhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "event", schema = "event_definition")
public class EventDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String orgId;
	private String sourceId;
	private String sourceName;
	private String workspace;
	private String eventName;
	private String schema;
}
