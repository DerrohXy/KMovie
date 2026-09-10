package com.kmovie.repository;

import com.kmovie.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    @Query("select u from User u where u.isDeleted = false and (u.username = :identifier or u.email = :identifier)")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByEmailAndIsDeletedFalse(String email);
}
