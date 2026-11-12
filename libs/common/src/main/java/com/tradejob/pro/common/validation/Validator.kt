package com.tradejob.pro.common.validation

import androidx.annotation.StringRes

interface Validator<T> {
    fun validate(value: T): ValidationResult
}

class RequiredValidator(@StringRes private val errorMessage: Int) : Validator<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Error(errorMessage)
        } else {
            ValidationResult.Success
        }
    }
}

class NumberValidator(@StringRes private val errorMessage: Int) : Validator<String> {
    override fun validate(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.Success
        return if (value.toDoubleOrNull() == null) {
            ValidationResult.Error(errorMessage)
        } else {
            ValidationResult.Success
        }
    }
}

class EmailValidator(@StringRes private val errorMessage: Int) : Validator<String> {

    override fun validate(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.Success
        return if (!isValid(value)) {
            ValidationResult.Error(errorMessage)
        } else {
            ValidationResult.Success
        }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()

        fun isValid(value: String): Boolean = value.matches(EMAIL_REGEX)
    }
}

class PhoneValidator(@StringRes private val errorMessage: Int) : Validator<String> {
    override fun validate(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.Success
        // Simple validation for phone numbers (at least 9 digits)
        return if (value.length < 9 || value.any { !it.isDigit() && it != '+' && it != ' ' && it != '-' }) {
            ValidationResult.Error(errorMessage)
        } else {
            ValidationResult.Success
        }
    }
}
