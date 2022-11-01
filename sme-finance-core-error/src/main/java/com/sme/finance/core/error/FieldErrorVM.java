package com.sme.finance.core.error;

import java.io.Serializable;

public record FieldErrorVM(String objectName, String field, String message) implements Serializable {
}
