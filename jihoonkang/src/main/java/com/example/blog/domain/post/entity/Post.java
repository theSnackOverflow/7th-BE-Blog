package com.example.blog.domain.post.entity;

import com.example.blog.domain.comment.entity.Comment;
import com.example.blog.domain.user.entity.User;
import com.example.blog.global.entity.BaseEntity;
import com.example.blog.global.exception.BusinessException;
import com.example.blog.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PostStatus status;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    public static Post of(User user, String title, String content, PostStatus status) {
        Post post = new Post();
        post.user = user;
        post.title = title;
        post.content = content;
        post.status = status != null ? status : PostStatus.PUBLISHED;
        return post;
    }

    public void update(String title, String content, PostStatus status) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void hide() {
        if (this.status == PostStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.POST_ALREADY_HIDDEN);
        }
        this.status = PostStatus.HIDDEN;
    }
}
