package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.cache.data.UserData
import net.perfectdreams.loritta.cinnamon.discord.utils.sources.UserTokenSource
import org.junit.jupiter.api.Test

class MessageUtilsTest {
    @Test
    fun `test message generation`() {
        val input = "hello {user.name}! :lori_hi:"

        val parallaxMessage = MessageUtils.createMessage(
            input,
            listOf(
                UserTokenSource(
                    Kord.restOnly(Constants.FAKE_TOKEN),
                    UserData(
                        Snowflake(123170274651668480L),
                        "MrPowerGamerBR",
                        "4185",
                        null
                    ),
                    null
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