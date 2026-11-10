package com.example.userservice.web.dto;

/** Regexes shared by the request records. Annotation values must be compile-time constants, hence static final. */
final class ValidationRules {

    static final String USERNAME = "^[a-z0-9._-]{3,32}$";
    static final String PHONE = "^\\+?[0-9]{8,15}$";
    /** For optional text in an edit: null is fine (= unchanged), but "" or "   " is not. */
    static final String NOT_BLANK = ".*\\S.*";

    private ValidationRules() {
    }
}
