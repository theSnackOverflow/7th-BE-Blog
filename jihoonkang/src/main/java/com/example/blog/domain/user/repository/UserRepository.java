package com.example.blog.domain.user.repository;

import com.example.blog.domain.user.entity.Provider;
import com.example.blog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndProvider(String email, Provider provider);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndProvider(String email, Provider provider);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
