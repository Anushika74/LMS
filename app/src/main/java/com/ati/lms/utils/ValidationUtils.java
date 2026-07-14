package com.ati.lms.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * ValidationUtils - Form validation helper methods
 */
public class ValidationUtils {

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password strength (min 6 chars, at least 1 letter and 1 number)
     */
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    /**
     * Check if passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }

    /**
     * Validate non-empty field
     */
    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && text.trim().length() > 0;
    }

    /**
     * Validate full name (at least 2 characters)
     */
    public static boolean isValidName(String name) {
        return isNotEmpty(name) && name.trim().length() >= 2;
    }

    /**
     * Validate student ID format
     */
    public static boolean isValidStudentId(String studentId) {
        return isNotEmpty(studentId) && studentId.trim().length() >= 3;
    }

    /**
     * Get password strength description
     */
    public static String getPasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) return "";
        if (password.length() < 6) return "Too short";

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        int strength = 0;
        if (hasUpper) strength++;
        if (hasLower) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        if (password.length() >= 8) strength++;

        if (strength <= 2) return "Weak";
        if (strength <= 3) return "Medium";
        return "Strong";
    }
}