package com.chat.aj.expensetracker.common.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Authenticated caller is known but not permitted to perform the action
 * (not a group member, not the owner, not the resource creator, etc).
 * Use {@link UnauthorizedException} only when the caller could not be authenticated at all.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
