package com.smartclinic.smartclinic.repository;

import com.smartclinic.smartclinic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ⚠️ STANDARD USE (may trigger lazy loading if relationships are accessed later)
    Optional<User> findByEmail(String email);

    // ✅ SAFE VERSION FOR AUTH (NO ENTITY GRAPH SIDE EFFECTS)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailOnly(String email);
}