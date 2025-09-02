package com.lendora.users.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.lendora.common.exception.ForbiddenException;

@Component
public class InternalAuthValidator {
    private final String expected;

    public InternalAuthValidator(@Value("${lendora.services.auth.internal.secret}") String expected) {
        this.expected = expected;
    }

    public void check(String provided) {
        if (provided == null || !provided.equals(expected)) {
            throw new ForbiddenException("Invalid internal auth header");
        }
    }
}