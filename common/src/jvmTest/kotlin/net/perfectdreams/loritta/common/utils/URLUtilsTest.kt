package net.perfectdreams.loritta.common.utils

import net.perfectdreams.loritta.common.utils.URLUtils
import org.junit.Test

class URLUtilsTest {
    @Test
    fun `test url validation`() {
        assert(URLUtils.isValidURL("https://loritta.website/"))
        assert(!URLUtils.isValidURL("owo"))
    }

    @Test
    fun `test http and https validation`() {
        assert(URLUtils.isValidHttpOrHttpsURL("https://loritta.website/"))
        assert(!URLUtils.isValidHttpOrHttpsURL("owo"))
    }
}