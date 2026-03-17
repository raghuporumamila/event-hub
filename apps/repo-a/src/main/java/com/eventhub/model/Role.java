package com.eventhub.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter // Only generate getters
@Setter // Only generate setters
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role", schema = "security")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
}
