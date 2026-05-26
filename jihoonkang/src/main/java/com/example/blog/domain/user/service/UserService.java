package com.example.blog.domain.user.service;

import com.example.blog.domain.user.dto.UserCreateRequest;
import com.example.blog.domain.user.dto.UserResponse;
import com.example.blog.domain.user.dto.UserUpdateRequest;
import com.example.blog.domain.user.entity.Provider;
import com.example.blog.domain.user.entity.User;
import com.example.blog.domain.user.repository.UserRepository;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import com.example.blog.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmailAndProvider(request.email(), Provider.LOCAL)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        User user = User.ofLocal(request.username(), request.email(), hashedPassword, request.profileUrl());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public UserResponse update(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.update(request.username(), request.profileUrl());
        return UserResponse.from(user);
    }

    public void delete(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
