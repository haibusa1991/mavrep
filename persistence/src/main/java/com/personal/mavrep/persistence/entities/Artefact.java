package com.personal.mavrep.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "artefacts")
public class Artefact {

    @Builder
    public Artefact(String name, String version, String filename, String uri) {
        this.name = name;
        this.version = version;
        this.filename = filename;
        this.uri = uri;
        this.timestamp = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String version;

    @Setter
    private String filename;

    private String uri;

    private LocalDateTime timestamp;

}
