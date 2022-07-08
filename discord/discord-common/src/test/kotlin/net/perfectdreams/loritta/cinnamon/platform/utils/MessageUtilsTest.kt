package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.platform.utils.sources.UserTokenSource
import org.junit.jupiter.api.Test

class MessageUtilsTest {
    @Test
    fun `test message generation`() {
        val input = "hello {user.name}! :lori_hi:"

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
            mapOf(),
            listOf(),
            listOf(),
            listOf(
                DiscordEmoji(
                    Snowflake(972187812554211418),
                    "lori_hi"
                )
            )
        )

        require(parallaxMessage.content == "hello MrPowerGamerBR! <:lori_hi:972187812554211418>") { "Content didn't match!" }
    }
}