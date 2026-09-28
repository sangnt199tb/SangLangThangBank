package com.example.hydro.ui.onboarding

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingValidatorTest {

    @Test
    fun email_validAndInvalid() {
        assertNull(OnboardingValidator.validateEmail("nguyen.van.a@example.com"))
        assertNotNull(OnboardingValidator.validateEmail(""))
        assertNotNull(OnboardingValidator.validateEmail("abc@"))
        assertNotNull(OnboardingValidator.validateEmail("abc.example.com"))
    }

    @Test
    fun phone_acceptsVietnameseMobileNumbers() {
        assertNull(OnboardingValidator.validatePhone("0912345678"))
        assertNull(OnboardingValidator.validatePhone("0387654321"))
        assertNotNull(OnboardingValidator.validatePhone(""))
        assertNotNull(OnboardingValidator.validatePhone("091234567")) // thiếu 1 số
        assertNotNull(OnboardingValidator.validatePhone("0212345678")) // đầu số không hợp lệ
    }

    @Test
    fun misCode_isOptional() {
        assertNull(OnboardingValidator.validateMisCode(""))
        assertNull(OnboardingValidator.validateMisCode("TCB1234"))
        assertNotNull(OnboardingValidator.validateMisCode("AB1"))
    }

    @Test
    fun captcha_isCaseInsensitive() {
        assertNull(OnboardingValidator.validateCaptcha("k7px3", "K7PX3"))
        assertNotNull(OnboardingValidator.validateCaptcha("", "K7PX3"))
        assertNotNull(OnboardingValidator.validateCaptcha("K7PX4", "K7PX3"))
    }
}
