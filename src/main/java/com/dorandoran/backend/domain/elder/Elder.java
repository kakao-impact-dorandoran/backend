package com.dorandoran.backend.domain.elder;

import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.converter.StringListJsonConverter;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "elders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Elder extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_elders_guardian"))
    private User guardian;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "age_group", length = 20)
    private String ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(name = "greeting_comment", length = 50)
    private String greetingComment;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "interests", columnDefinition = "JSON")
    private List<String> interests;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_call_type", length = 20, nullable = false)
    private CallType preferredCallType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20, nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "request_notes", columnDefinition = "TEXT")
    private String requestNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ElderStatus status;

    @Builder
    private Elder(User guardian, String name, String ageGroup, Gender gender, String profileImageUrl,
                  String greetingComment, String phoneNumber, String address,
                  List<String> interests, CallType preferredCallType, DifficultyLevel difficultyLevel,
                  String requestNotes, ElderStatus status) {
        this.guardian = guardian;
        this.name = name;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.greetingComment = greetingComment;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.interests = interests;
        this.preferredCallType = preferredCallType == null ? CallType.VIDEO : preferredCallType;
        this.difficultyLevel = difficultyLevel == null ? DifficultyLevel.LOW : difficultyLevel;
        this.requestNotes = requestNotes;
        this.status = status == null ? ElderStatus.AVAILABLE : status;
    }

    public void update(String name, String ageGroup, Gender gender, String profileImageUrl,
                       String greetingComment, String phoneNumber, String address,
                       List<String> interests, CallType preferredCallType,
                       DifficultyLevel difficultyLevel, String requestNotes) {
        if (name != null) this.name = name;
        if (ageGroup != null) this.ageGroup = ageGroup;
        if (gender != null) this.gender = gender;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (greetingComment != null) this.greetingComment = greetingComment;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (address != null) this.address = address;
        if (interests != null) this.interests = interests;
        if (preferredCallType != null) this.preferredCallType = preferredCallType;
        if (difficultyLevel != null) this.difficultyLevel = difficultyLevel;
        if (requestNotes != null) this.requestNotes = requestNotes;
    }

    public void markMatched() {
        this.status = ElderStatus.MATCHED;
    }

    public void markAvailable() {
        this.status = ElderStatus.AVAILABLE;
    }

    public void markInactive() {
        this.status = ElderStatus.INACTIVE;
    }
}
