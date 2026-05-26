package com.example.blog.domain.comment.entity;

import com.example.blog.domain.post.entity.Post;
import com.example.blog.domain.user.entity.User;
import com.example.blog.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false)
    private boolean accepted;

    public static Comment of(User user, Post post, String content, Comment parentComment) {
        Comment comment = new Comment();
        comment.user = user;
        comment.post = post;
        comment.content = content;
        comment.parentComment = parentComment;
        comment.accepted = false;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public void accept() {
        this.accepted = true;
    }

    public void unaccept() {
        this.accepted = false;
    }
}
