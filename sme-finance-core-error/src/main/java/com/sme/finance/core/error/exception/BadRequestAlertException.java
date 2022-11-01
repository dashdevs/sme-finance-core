package com.sme.finance.core.error.exception;

import com.sme.finance.core.error.ErrorConstants;
import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@Getter
@SuppressWarnings({"java:S110", "unused"}) // Inheritance tree of classes should not be too deep
public class BadRequestAlertException extends AbstractThrowableProblem {

    public BadRequestAlertException(String detail) {
        this(ErrorConstants.DEFAULT_TYPE, detail);
    }

    public BadRequestAlertException(URI type, String detail) {
        super(type, "Invalid request", Status.BAD_REQUEST, detail);
    }
}
