package com.learningpath.ai.validation;

import java.util.Collections;
import java.util.List;

public record ValidationResult(
        boolean valid,
        List<String> errors,
        List<String> warnings
) {
    public static ValidationResult ok() {
        return new ValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult okWithWarnings(List<String> warnings) {
        return new ValidationResult(true, Collections.emptyList(), warnings != null ? warnings : Collections.emptyList());
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, errors != null ? errors : Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult invalid(String singleError) {
        return new ValidationResult(false, List.of(singleError), Collections.emptyList());
    }

    public static ValidationResult of(List<String> errors, List<String> warnings) {
        boolean isValid = errors == null || errors.isEmpty();
        return new ValidationResult(
                isValid,
                errors != null ? errors : Collections.emptyList(),
                warnings != null ? warnings : Collections.emptyList()
        );
    }
}
