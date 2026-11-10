package com.example.userservice.web;

/** One entry of the "errors" list in a 400 response: which JSON field, and what is wrong with it. */
record FieldProblem(String field, String message) {
}
