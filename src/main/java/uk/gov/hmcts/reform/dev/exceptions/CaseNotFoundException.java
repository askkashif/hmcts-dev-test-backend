package uk.gov.hmcts.reform.dev.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(Long id) {
        super("Case not found with id: " + id);
    }

    public CaseNotFoundException(String message) {
        super(message);
    }
}
