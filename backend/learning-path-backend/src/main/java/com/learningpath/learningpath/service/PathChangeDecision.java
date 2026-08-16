package com.learningpath.learningpath.service;

/**
 * Purely deterministic output of the PathChangeDetector.
 * No AI involved — this is computed exclusively from persisted learner state.
 */
public record PathChangeDecision(

        /**
         * Whether the learner's path should be regenerated.
         */
        boolean shouldAdapt,

        /**
         * Human-readable reason explaining the decision.
         */
        String reason
) {
    public static PathChangeDecision noChange() {
        return new PathChangeDecision(false, "No meaningful learner state change detected. Current path remains optimal.");
    }

    public static PathChangeDecision adapt(String reason) {
        return new PathChangeDecision(true, reason);
    }
}
