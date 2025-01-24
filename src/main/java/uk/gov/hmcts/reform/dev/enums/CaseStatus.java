package uk.gov.hmcts.reform.dev.enums;

public enum CaseStatus {
    NEW,
    IN_PROGRESS,
    ON_HOLD,
    RESOLVED,
    CLOSED;

    public static boolean isValid(String status) {
        try {
            valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
