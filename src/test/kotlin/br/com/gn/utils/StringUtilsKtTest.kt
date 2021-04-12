package br.com.gn.utils

import br.com.gn.operation.OperationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.LocalDate

internal class StringUtilsKtTest {
    @Test
    fun `should return null for passing a blank value when trying to convert to LocalDate`() {
        assertNull("".toLocalDate())
    }

    @Test
    fun `should convert to LocalDate successfully`() {
        assertEquals(LocalDate.of(2020, 5, 2), "2020-05-02".toLocalDate())
    }

    @Test
    fun `should throw an exception when passing an invalid text when trying to convert to LocalDate`() {
        assertThrows<IllegalArgumentException> {
            "text".toLocalDate()
        }.run {
            assertEquals("Could not parse date from text", this.message)
        }
    }

    @Test
    fun `should return null for passing a blank value when trying to convert to BigDecimal`() {
        assertNull("".toBigDecimal())
    }

    @Test
    fun `should convert to BigDecimal successfully`() {
        assertEquals(BigDecimal(20), "20".toBigDecimal())
    }

    @Test
    fun `should throw an exception when passing an invalid text when trying to convert to BigDecimal`() {
        assertThrows<IllegalArgumentException> {
            "text".toBigDecimal()
        }.run {
            assertEquals("Could not parse BigDecimal from text", this.message)
        }
    }

    @Test
    fun `should return null for passing a blank value when trying to convert to Enum`() {
        assertNull("UNKNOWN".toEnum<OperationType>())
    }

    @Test
    fun `should convert to Enum successfully`() {
        assertEquals(OperationType.IMPORT, "IMPORT".toEnum<OperationType>())
    }

    @Test
    fun `should throw an exception when passing an invalid text when trying to convert to Enum`() {
        assertThrows<IllegalArgumentException> {
            "text".toEnum<OperationType>()
        }.run {
            assertEquals("Could not convert to Enum from text", this.message)
        }
    }
}