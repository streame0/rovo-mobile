package com.rovo.app.core.native

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NativeAddonPayloadTest {
    @Test
    fun nativeAddonPayloadOrNull_returnsPayloadForSuccess() {
        assertEquals("""{"id":"test"}""", """{"id":"test"}""".nativeAddonPayloadOrNull())
    }

    @Test
    fun nativeAddonPayloadOrNull_returnsNullForErrorJson() {
        assertNull("""{"error": "timeout"}""".nativeAddonPayloadOrNull())
    }
}
