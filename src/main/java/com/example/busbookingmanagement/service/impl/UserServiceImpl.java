package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.entity.User;
import com.example.busbookingmanagement.repository.UserRepository;
import com.example.busbookingmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() { return userRepository.findAll(); }

    public Optional<User> getById(Long id) { return userRepository.findById(id); }

    public User create(User user) { return userRepository.save(user); }

    public User update(Long id, User user) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setFullName(user.getFullName());
                    u.setPhone(user.getPhone());
                    u.setStatus(user.getStatus());
                    if (user.getPassword() != null && !user.getPassword().isBlank()) {
                        u.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    return userRepository.save(u);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void delete(Long id) { userRepository.deleteById(id); }
}
