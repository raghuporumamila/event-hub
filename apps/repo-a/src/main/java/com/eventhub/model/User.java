package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", schema = "security")
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String password;
	private String name;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	private Role role;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "default_workspace_id")
	private Workspace defaultWorkspace;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;
}
