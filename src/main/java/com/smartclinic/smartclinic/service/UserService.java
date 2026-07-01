package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.entity.User;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Basic CRUD service for User.
 *
 * Note: regular signup should go through POST /auth/register
 * (AuthService), which also returns a JWT immediately. This service's
 * createUser/updateUser exist for administrative management of accounts
 * (the corresponding controller endpoints are restricted to ADMIN) and
 * still hash any incoming password with the same PasswordEncoder used at
 * registration, so accounts created either way remain consistent and a
 * raw password is never persisted.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existing.setRole(updatedUser.getRole());
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
    }
}
