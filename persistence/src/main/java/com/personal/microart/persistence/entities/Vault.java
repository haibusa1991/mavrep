package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "vaults")
public class Vault {

    @Builder
    public Vault(String name, MicroartUser user) {
        this.name = name;
        this.artefacts = new ArrayList<>();
        this.authorizedUsers = new HashSet<>();
        this.authorizedUsers.add(user);
        this.isPublic = true;
        this.owner = user;
    }

    public static Vault empty() {
        Vault vault = new Vault();
        vault.name = "";
        vault.artefacts = new ArrayList<>();
        vault.authorizedUsers = new HashSet<>();
        vault.isPublic = true;
        vault.owner = MicroartUser.empty();

        return vault;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Artefact> artefacts;

    public Boolean addArtefact(Artefact artefact) {
        return this.artefacts.add(artefact);
    }

    public Boolean removeArtefact(Artefact artefact) {
        return this.artefacts.remove(artefact);
    }

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<MicroartUser> authorizedUsers;

    @ManyToOne
    private MicroartUser owner;

    @Setter
    @Accessors(fluent = true)
    private Boolean isPublic;

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
