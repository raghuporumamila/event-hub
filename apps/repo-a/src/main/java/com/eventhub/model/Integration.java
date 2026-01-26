package com.eventhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "integration", schema = "event")
public class Integration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;
    @OneToOne
    @JoinColumn(name = "source_id")
    private Source source;
    @OneToMany
    @JoinColumn(name = "target_id")
    private List<Target> targets;
}
