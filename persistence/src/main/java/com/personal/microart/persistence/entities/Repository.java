package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "repositories")
public class Repository {

    @Builder
    public Repository(String name, MicroartUser user) {
        this.name = name;
        this.artefacts = new ArrayList<>();
        this.authorizedUsers = Set.of(user);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String name;

    @OneToMany
    private List<Artefact> artefacts;

    public Boolean addArtefact(Artefact artefact) {
        return this.artefacts.add(artefact);
    }

    public Boolean removeArtefact(Artefact artefact) {
        return this.artefacts.remove(artefact);
    }

    @ManyToMany
    private Set<MicroartUser> authorizedUsers;

    public Boolean addUser(MicroartUser user) {
        return this.authorizedUsers.add(user);
    }

    public Boolean removeUser(MicroartUser user) {
        if (this.authorizedUsers.size() == 1 && this.authorizedUsers.contains(user)) {
            return false;
        }

        return this.authorizedUsers.remove(user);
    }
}
