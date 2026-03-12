package com.vision.mathcut.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "JSON", nullable = false)
    private String canvasData;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String thumbnail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private Long createdBy;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long updatedBy;

    @Builder
    public Cut(User user, String title, String canvasData, String thumbnail, Long createdBy) {
        this.user = user;
        this.title = title;
        this.canvasData = canvasData;
        this.thumbnail = thumbnail;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public void update(String title, String canvasData, String thumbnail, Long updatedBy) {
        this.title = title;
        this.canvasData = canvasData;
        this.thumbnail = thumbnail;
        this.updatedBy = updatedBy;
    }
}
