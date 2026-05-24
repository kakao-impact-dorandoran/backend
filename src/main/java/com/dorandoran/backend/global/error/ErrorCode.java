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
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SC002", "Schedule not found"),
    INVALID_SCHEDULE_TIME_RANGE(HttpStatus.BAD_REQUEST, "SC003", "Invalid schedule time range"),
    SCHEDULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SC004", "Schedule access denied"),
    SCHEDULE_ALREADY_CANCELED(HttpStatus.CONFLICT, "SC005", "Schedule is already canceled"),
    SCHEDULE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "SC006", "Schedule is already completed"),
    MATCH_NOT_SCHEDULABLE(HttpStatus.CONFLICT, "SC007", "Match is not in a schedulable state"),
    SCHEDULE_OUT_OF_AVAILABLE_TIME(HttpStatus.CONFLICT, "SC008", "Schedule time is not within available time slots"),

    AVAILABLE_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "AT001", "Available time not found"),
    INVALID_AVAILABLE_TIME_RANGE(HttpStatus.BAD_REQUEST, "AT002", "Invalid available time range"),
    AVAILABLE_TIME_OVERLAPPED(HttpStatus.CONFLICT, "AT003", "Available time overlaps with existing entries"),
    AVAILABLE_TIME_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AT004", "Available time access denied"),
    INVALID_AVAILABLE_TIME_QUERY(HttpStatus.BAD_REQUEST, "AT005", "ownerType and ownerId must both be provided"),

    ACTIVITY_RECORD_DUPLICATED(HttpStatus.CONFLICT, "AR001", "Activity record already exists for this schedule"),
    ACTIVITY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "AR002", "Activity record not found"),
    ACTIVITY_RECORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AR003", "Activity record access denied"),
    ACTIVITY_RECORD_DUPLICATED_CALL_LOG(HttpStatus.CONFLICT, "AR004", "Activity record already exists for this call log"),
    INVALID_ACTIVITY_DURATION(HttpStatus.BAD_REQUEST, "AR005", "Invalid activity duration"),
    CALL_LOG_NOT_COMPLETED(HttpStatus.CONFLICT, "AR006", "Call log is not completed yet"),
    ACTIVITY_MATCH_MISMATCH(HttpStatus.BAD_REQUEST, "AR007", "Activity record match mismatch"),
    ACTIVITY_SCHEDULE_MISMATCH(HttpStatus.BAD_REQUEST, "AR008", "Schedule does not belong to the requested match"),
    ACTIVITY_CALL_LOG_MISMATCH(HttpStatus.BAD_REQUEST, "AR009", "Call log does not belong to the requested match"),
    ACTIVITY_MATCH_NOT_RECORDABLE(HttpStatus.CONFLICT, "AR010", "Match is not in a recordable state"),

    VOLUNTEER_STATS_NOT_FOUND(HttpStatus.NOT_FOUND, "VS001", "Volunteer stats not found"),

    CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Certificate not found"),
    CERTIFICATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CT002", "Certificate access denied"),
    CERTIFICATE_NOT_ENOUGH_ACTIVITY_TIME(HttpStatus.CONFLICT, "CT003", "발급 기준 시간이 부족합니다."),
    CERTIFICATE_SERIAL_DUPLICATED(HttpStatus.CONFLICT, "CT004", "Certificate serial already exists"),
    CERTIFICATE_REQUESTED_HOURS_INVALID(HttpStatus.BAD_REQUEST, "CT005", "발급 신청 시간이 유효하지 않습니다."),

    DEVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "D001", "Device token is invalid"),

    ELDER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "Elder not found"),
    ELDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E002", "Elder access denied"),
    NOT_A_GUARDIAN_USER(HttpStatus.FORBIDDEN, "E003", "User is not a guardian"),

    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "Device not found"),
    DEVICE_NOT_FOUND_FOR_ELDER(HttpStatus.NOT_FOUND, "D003", "Device not found for the elder"),
    DEVICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "D004", "Device access denied"),
    DEVICE_AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "D005", "Device authorization header is required"),
    INVALID_DEVICE_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "D006", "Invalid device authorization header"),
    DEVICE_NOT_REGISTERED(HttpStatus.FORBIDDEN, "D007", "Device is not registered"),

    CALL_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "CL001", "Call log not found"),
    CALL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CL002", "Call log access denied"),
    CALL_ALREADY_ENDED(HttpStatus.CONFLICT, "CL003", "Call log is already ended"),
    CALL_MATCH_MISMATCH(HttpStatus.BAD_REQUEST, "CL004", "Match does not match the requested call context"),
    CALL_SCHEDULE_MISMATCH(HttpStatus.BAD_REQUEST, "CL005", "Schedule does not belong to the requested match"),
    CALL_SCHEDULE_NOT_CONFIRMED(HttpStatus.CONFLICT, "CL006", "Schedule is not confirmed"),
    INVALID_CALL_TYPE(HttpStatus.BAD_REQUEST, "CL007", "Invalid call type"),

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "Report not found"),
    INVALID_REPORT_STATUS(HttpStatus.BAD_REQUEST, "R002", "Invalid report status"),
    REPORT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R003", "Report access denied"),

    MATCH_TERMINATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MT001", "Match termination request not found"),
    MATCH_TERMINATION_ALREADY_REQUESTED(HttpStatus.CONFLICT, "MT002", "Match termination already requested"),
    INVALID_MATCH_TERMINATION_STATUS(HttpStatus.BAD_REQUEST, "MT003", "Invalid match termination status"),
    MATCH_TERMINATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MT004", "Match termination access denied"),
    MATCH_ALREADY_ENDED(HttpStatus.CONFLICT, "MT005", "Match is already ended"),

    HELP_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "Help request not found"),
    HELP_REQUEST_ALREADY_HANDLED(HttpStatus.CONFLICT, "H002", "Help request is already handled"),
    INVALID_HELP_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "H003", "Invalid help request status");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
