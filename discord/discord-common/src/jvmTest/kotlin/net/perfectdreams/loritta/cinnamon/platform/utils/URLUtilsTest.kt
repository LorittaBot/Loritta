package net.perfectdreams.loritta.cinnamon.platform.utils

class URLUtilsTest {
    fun `test url validation`() {
        assert(URLUtils.isValidURL("https://loritta.website/"))
        assert(!URLUtils.isValidURL("owo"))
    }

    fun `test http and https validation`() {
        assert(URLUtils.isValidHttpOrHttpsURL("https://loritta.website/"))
        assert(!URLUtils.isValidHttpOrHttpsURL("owo"))
    }
}