package com.library.lms.validation;

import com.library.lms.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputValidatorTest {

    @Test
    void acceptsValidBookId() {
        assertEquals("BK000000000000000001", InputValidator.requireBookId(" BK000000000000000001 "));
    }

    @Test
    void rejectsSqlMetaInBookId() {
        assertThrows(ApiException.class, () -> InputValidator.requireBookId("BK' OR 1=1 --"));
        assertThrows(ApiException.class, () -> InputValidator.requireBookId("BK; DROP TABLE Books"));
    }

    @Test
    void escapesLikeWildcards() {
        assertEquals("100\\% complete", InputValidator.escapeLike("100% complete"));
        assertEquals("a\\_b", InputValidator.escapeLike("a_b"));
    }
}
