package com.jefflife.mudmk2.gameplay.application.service;

/**
 * Represents the result of a validation operation.
 * This class follows the Result pattern to avoid using exceptions for control flow.
 */
public record ValidationResult(boolean valid, String errorMessage) {

    /**
     * Creates a successful validation result.
     *
     * @return a valid result with no error message
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * Creates a failed validation result with an error message.
     *
     * @param errorMessage the error message
     * @return an invalid result with the specified error message
     */
    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
}