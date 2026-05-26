package com.example.blog.domain.user.entity;

import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.post.entity.Post;
import com.example.blog.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "provider"}),
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "profile_url", length = 200)
    private String profileUrl;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 200)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id", length = 50)
    private String providerId;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    public static User ofLocal(String username, String email, String password, String profileUrl) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.password = password;
        user.profileUrl = profileUrl;
        user.role = Role.USER;
        user.provider = Provider.LOCAL;
        return user;
    }

    public static User ofKakao(String username, String email, String profileUrl, String providerId) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.profileUrl = profileUrl;
        user.role = Role.USER;
        user.provider = Provider.KAKAO;
        user.providerId = providerId;
        return user;
    }

    public void update(String username, String profileUrl) {
        if (username != null) {
            this.username = username;
        }
        if (profileUrl != null) {
            this.profileUrl = profileUrl;
        }
    }
}
