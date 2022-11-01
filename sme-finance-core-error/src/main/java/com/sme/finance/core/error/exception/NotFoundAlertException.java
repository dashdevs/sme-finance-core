package com.sme.finance.core.error.exception;

import com.sme.finance.core.error.ErrorConstants;
import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@Getter
@SuppressWarnings({"java:S110", "unused"}) // Inheritance tree of classes should not be too deep
public class NotFoundAlertException extends AbstractThrowableProblem {

    public NotFoundAlertException(String entityName, Object entityId) {
        this(ErrorConstants.DEFAULT_TYPE, String.format("%s=%s doesn't exist", entityName, entityId));
    }

    public NotFoundAlertException(URI type, String detail) {
        super(type, "Not found", Status.NOT_FOUND, detail);
    }
}
