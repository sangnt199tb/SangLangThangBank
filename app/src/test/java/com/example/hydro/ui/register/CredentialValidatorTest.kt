package com.example.hydro.ui.register

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialValidatorTest {

    @Test
    fun username_acceptsPhoneAndCustomNames() {
        assertNull(CredentialValidator.validateUsername("0912345678"))
        assertNull(CredentialValidator.validateUsername("nguyen.van_an"))
        assertNotNull(CredentialValidator.validateUsername(""))
        assertNotNull(CredentialValidator.validateUsername("an123")) // 5 ký tự, quá ngắn
        assertNotNull(CredentialValidator.validateUsername(".nguyenvan")) // bắt đầu bằng dấu
        assertNotNull(CredentialValidator.validateUsername("nguyenvan_")) // kết thúc bằng dấu
    }

    @Test
    fun cleanUsername_lowercasesAndDropsInvalidChars() {
        assertEquals("nguyenvan.an", CredentialValidator.cleanUsername("Nguyen Van.An"))
    }

    @Test
    fun cleanPassword_dropsSpacesAndAccents() {
        assertEquals("Mtkhu@1", CredentialValidator.cleanPassword("Mật khẩu@1"))
    }

    @Test
    fun password_mustMeetAllRules() {
        assertNull(CredentialValidator.validatePassword("Sang@2026", "0912345678"))
        assertNotNull(CredentialValidator.validatePassword("", "0912345678"))
        assertNotNull(CredentialValidator.validatePassword("Sang2026", "0912345678")) // thiếu ký tự đặc biệt
        assertNotNull(CredentialValidator.validatePassword("sang@2026", "0912345678")) // thiếu chữ hoa
        assertNotNull(CredentialValidator.validatePassword("Ab@1", "0912345678")) // quá ngắn
    }

    @Test
    fun password_mustNotContainUsername() {
        assertFalse(CredentialValidator.isRuleMet(PasswordRule.NOT_USERNAME, "Xx@0912345678", "0912345678"))
        assertFalse(CredentialValidator.isRuleMet(PasswordRule.NOT_USERNAME, "", "0912345678"))
        assertTrue(CredentialValidator.isRuleMet(PasswordRule.NOT_USERNAME, "Sang@2026", "0912345678"))
    }

    @Test
    fun confirmPassword_mustMatch() {
        assertNull(CredentialValidator.validateConfirmPassword("Sang@2026", "Sang@2026"))
        assertNotNull(CredentialValidator.validateConfirmPassword("Sang@2026", ""))
        assertNotNull(CredentialValidator.validateConfirmPassword("Sang@2026", "Sang@2025"))
    }
}
