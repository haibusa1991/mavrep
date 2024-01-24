package com.personal.mavrep.persistence.repositories;

import com.personal.mavrep.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
