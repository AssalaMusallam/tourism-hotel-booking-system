package com.swer313.projectstep1.user;

/**
 * يُرمى عندما يحاول مستخدم التسجيل بإيميل موجود مسبقاً.
 */
public class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(String message) {
        super(message);
    }
}