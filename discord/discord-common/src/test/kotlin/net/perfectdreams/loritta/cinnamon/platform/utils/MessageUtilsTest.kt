package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.platform.utils.sources.UserTokenSource
import org.junit.jupiter.api.Test

class MessageUtilsTest {
    @Test
    fun `test message generation`() {
        val input = "hello {user.name}!"

        val parallaxMessage = MessageUtils.createMessage(
            input,
            listOf(
                UserTokenSource(
                    DiscordUser(
                        Snowflake(123170274651668480L),
                        "MrPowerGamerBR",
                        "4185",
                        null
                    )
                )
            ),
            mapOf()
        )

        require(parallaxMessage.content == "hello MrPowerGamerBR!") { "Content didn't match!" }
    }
}