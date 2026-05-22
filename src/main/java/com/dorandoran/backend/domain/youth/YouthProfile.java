package com.dorandoran.backend.domain.youth;

import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.converter.StringListJsonConverter;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "youth_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_youth_profiles_youth_id", columnNames = "youth_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YouthProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_youth_profiles_user"))
    private User youth;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "keywords", columnDefinition = "JSON")
    private List<String> keywords;

    @Column(name = "greeting_comment", length = 50)
    private String greetingComment;

    @Column(name = "voice_sample_url", columnDefinition = "TEXT")
    private String voiceSampleUrl;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Builder
    private YouthProfile(User youth, String profileImageUrl, List<String> keywords,
                        String greetingComment, String voiceSampleUrl, boolean isCompleted) {
        this.youth = youth;
        this.profileImageUrl = profileImageUrl;
        this.keywords = keywords;
        this.greetingComment = greetingComment;
        this.voiceSampleUrl = voiceSampleUrl;
        this.isCompleted = isCompleted;
    }

    public void update(String profileImageUrl, List<String> keywords,
                       String greetingComment, String voiceSampleUrl) {
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (keywords != null) this.keywords = keywords;
        if (greetingComment != null) this.greetingComment = greetingComment;
        if (voiceSampleUrl != null) this.voiceSampleUrl = voiceSampleUrl;
    }

    public void markCompleted() {
        this.isCompleted = true;
    }
}
