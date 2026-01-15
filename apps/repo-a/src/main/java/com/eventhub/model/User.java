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
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String email;
	private String name;
	private String role;
	@ManyToOne
	@JoinColumn(name = "default_workspace_id")
	private Workspace defaultWorkspace;
	@ManyToOne
	@JoinColumn(name = "orgId")
	private Organization organization;
}
