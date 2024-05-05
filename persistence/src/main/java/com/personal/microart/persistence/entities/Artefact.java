package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "artefacts")

public class Artefact {

    @Builder
    public Artefact(String uri, String filename) {
        this.uri = uri;
        this.filename = filename;
        this.timestamp = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    private String uri;

    @Accessors(chain = true)
    @Setter
    private String filename;

    private LocalDateTime timestamp;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artefact artefact)) return false;

        return id.equals(artefact.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
