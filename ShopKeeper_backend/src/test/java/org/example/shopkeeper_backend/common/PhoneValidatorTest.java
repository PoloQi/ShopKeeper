package org.example.shopkeeper_backend.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneValidatorTest {

    @Test
    void blankPhonePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check(null));
        assertDoesNotThrow(() -> PhoneValidator.check(""));
        assertDoesNotThrow(() -> PhoneValidator.check("   "));
    }

    @Test
    void validMobilePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check("13800138000"));
    }

    @Test
    void validLandlinePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check("020-12345678"));
        assertDoesNotThrow(() -> PhoneValidator.check("02012345678"));
        assertDoesNotThrow(() -> PhoneValidator.check("07551234567"));
    }

    @Test
    void surroundingSpacesAreStripped() {
        assertDoesNotThrow(() -> PhoneValidator.check("  13800138000 "));
    }

    @Test
    void normalizeStripsAndNullsBlank() {
        assertNull(PhoneValidator.normalize(null));
        assertNull(PhoneValidator.normalize(""));
        assertNull(PhoneValidator.normalize("   "));
        assertEquals("13800138000", PhoneValidator.normalize("  13800138000 "));
        assertEquals("020-12345678", PhoneValidator.normalize("020-12345678"));
    }

    @Test
    void invalidPhoneRejected() {
        assertThrows(BusinessException.class, () -> PhoneValidator.check("123456"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("12345678901"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("1380013800"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("abc"));
    }
}
