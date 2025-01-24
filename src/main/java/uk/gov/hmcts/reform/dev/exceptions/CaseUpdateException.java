package uk.gov.hmcts.reform.dev.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CaseUpdateException extends RuntimeException {
    public CaseUpdateException(String message) {
        super(message);
    }

    public CaseUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
