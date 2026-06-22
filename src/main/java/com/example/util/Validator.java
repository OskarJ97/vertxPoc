package com.example.util;

import com.example.exception.ValidationException;

import java.util.regex.Pattern;

public class Validator {

    private static final int LOGIN_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 100;
    private static final int NAME_MAX_LENGTH = 200;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validator() {}

    public static void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new ValidationException("Login cannot be blank");
        }
        if (login.length() > LOGIN_MAX_LENGTH) {
            throw new ValidationException("Login must not exceed: " + LOGIN_MAX_LENGTH + ",characters");
        }
        if (!EMAIL_PATTERN.matcher(login).matches()) {
            throw new ValidationException("Login must be a valid email address");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password cannot be blank");
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new ValidationException("Password must be at least: " + PASSWORD_MIN_LENGTH + ",characters");
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new ValidationException("Password must not exceed: " + PASSWORD_MAX_LENGTH + ",characters");
        }
    }

    public static void validateItemName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be blank");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ValidationException("Name must not exceed: " + NAME_MAX_LENGTH + ",characters");
        }
    }
}
