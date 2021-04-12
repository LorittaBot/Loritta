package net.perfectdreams.loritta.common.locale

import org.junit.Test
import kotlin.test.assertEquals

class BaseLocaleTest {
    private val locale = BaseLocale(
        "default",
        mapOf(
            "loritta.hello" to "Loritta is very cute!"
        ),
        mapOf(
            "loritta.list" to listOf(
                "Loritta",
                "Pantufa",
                "Gabriela"
            )
        )
    )

    @Test
    fun `check locale string`() {
        val result = locale[LocaleKeyData("loritta.hello")]

        assertEquals(result, "Loritta is very cute!", "Retrieved message from locale doesn't match!")
    }

    @Test
    fun `check locale list`() {
        val result = locale.getList(LocaleKeyData("loritta.list"))

        assertEquals(result[0], "Loritta", "First entry of retrieved list from locale doesn't match!")
        assertEquals(result[1], "Pantufa", "First entry of retrieved list from locale doesn't match!")
        assertEquals(result[2], "Gabriela", "First entry of retrieved list from locale doesn't match!")
    }

    @Test
    fun `check unknown locale key`() {
        val result = locale[LocaleKeyData("loritta.unknown")]

        assertEquals(result, "!!{loritta.unknown}!!", "Unknown key format doesn't match!")
    }
}