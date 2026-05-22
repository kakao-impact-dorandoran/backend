package com.dorandoran.backend.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method not allowed"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Internal server error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "Invalid type value"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "Access denied"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C007", "Unauthorized"),

    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "U001", "Email already exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "User not found"),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "U003", "Password does not match"),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "U004", "운영 정책 위반으로 이용이 제한되었습니다."),
    YOUTH_PENDING_APPROVAL(HttpStatus.FORBIDDEN, "U005", "관리자 승인 대기 중입니다."),
    YOUTH_REJECTED(HttpStatus.FORBIDDEN, "U006", "가입 신청이 반려되었습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Expired token"),

    KEYWORD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Y001", "Keyword limit exceeded (max 5)"),
    FORBIDDEN_WORD_INCLUDED(HttpStatus.BAD_REQUEST, "Y002", "Greeting contains forbidden words"),
    YOUTH_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Y003", "Youth profile not found"),
    YOUTH_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Y004", "Youth profile already exists"),
    NOT_A_YOUTH_USER(HttpStatus.BAD_REQUEST, "Y005", "Target user is not a youth"),
    INVALID_APPROVAL_STATUS(HttpStatus.BAD_REQUEST, "Y006", "Invalid approval status"),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "Y007", "Rejection reason is required for REJECTED status"),

    CANNOT_BAN_ADMIN(HttpStatus.FORBIDDEN, "U007", "관리자 계정은 제재할 수 없습니다."),
    CANNOT_BAN_SELF(HttpStatus.FORBIDDEN, "U008", "자기 자신은 제재할 수 없습니다."),
    USER_ALREADY_SUSPENDED(HttpStatus.CONFLICT, "U009", "이미 제재된 사용자입니다."),

    MATCH_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "M001", "Youth match limit exceeded"),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "Match not found"),
    MATCH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "M003", "Match access denied"),
    DUPLICATE_MATCH(HttpStatus.CONFLICT, "M004", "Active match already exists for this youth and elder"),
    ICEBREAKING_MESSAGE_REQUIRED(HttpStatus.BAD_REQUEST, "M005", "Icebreaking message is required"),
    ELDER_NOT_AVAILABLE(HttpStatus.CONFLICT, "M006", "Elder is not available for matching"),

    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "SC001", "Schedule conflict detected"),

    ACTIVITY_RECORD_DUPLICATED(HttpStatus.CONFLICT, "AR001", "Activity record already exists for this schedule"),

    DEVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "D001", "Device token is invalid"),

    ELDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "Elder not found"),
    ELDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E002", "Elder access denied"),
    NOT_A_GUARDIAN_USER(HttpStatus.FORBIDDEN, "E003", "User is not a guardian"),

    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "Device not found"),
    DEVICE_NOT_FOUND_FOR_ELDER(HttpStatus.NOT_FOUND, "D003", "Device not found for the elder"),
    DEVICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "D004", "Device access denied");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
