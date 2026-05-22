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

    MATCH_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "M001", "Youth match limit exceeded"),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "Match not found"),

    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "SC001", "Schedule conflict detected"),

    ACTIVITY_RECORD_DUPLICATED(HttpStatus.CONFLICT, "AR001", "Activity record already exists for this schedule"),

    DEVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "D001", "Device token is invalid");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
