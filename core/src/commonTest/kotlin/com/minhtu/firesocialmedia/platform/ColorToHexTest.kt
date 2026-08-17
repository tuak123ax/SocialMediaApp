package com.minhtu.firesocialmedia.platform

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [Color.toHex], the only pure/testable logic left in
 * `core`'s commonMain after the domain/usecases and data/repository
 * layers were migrated out to the feature modules. It converts a
 * Compose [Color] into an "#AARRGGBB" hex string, clamping each
 * channel to the 0..255 range.
 */
class ColorToHexTest {

    @Test
    fun `opaque black converts to hex`() {
        val result = Color(0xFF000000).toHex()
        assertEquals("#FF000000", result)
    }

    @Test
    fun `opaque white converts to hex`() {
        val result = Color(0xFFFFFFFF).toHex()
        assertEquals("#FFFFFFFF", result)
    }

    @Test
    fun `fully transparent color converts to hex with zero alpha`() {
        val result = Color(0x00123456).toHex()
        assertEquals("#00123456", result)
    }

    @Test
    fun `arbitrary opaque color preserves channel order aarrggbb`() {
        // red = 0x11, green = 0x22, blue = 0x33, alpha = 0xFF
        val result = Color(0xFF112233).toHex()
        assertEquals("#FF112233", result)
    }

    @Test
    fun `semi transparent color rounds alpha channel correctly`() {
        // alpha 0.5f * 255 = 127.5 -> rounds to 128 (0x80)
        val result = Color(red = 1f, green = 0f, blue = 0f, alpha = 0.5f).toHex()
        assertEquals("#80FF0000", result)
    }

    @Test
    fun `unspecified color falls back to opaque white`() {
        val result = Color.Unspecified.toHex()
        assertEquals("#FFFFFFFF", result)
    }

    @Test
    fun `lowercase hex digits are uppercased`() {
        val result = Color(red = 0.6666667f, green = 0.4f, blue = 0.8f, alpha = 1f).toHex()
        // Every character in the output must already be uppercase (or a digit).
        assertEquals(result, result.uppercase())
    }
}
