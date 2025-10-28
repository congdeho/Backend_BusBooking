package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAll();
    Optional<User> getById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
}

