package net.perfectdreams.loritta.cinnamon.platform.components

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectMenuBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import net.perfectdreams.loritta.cinnamon.common.emotes.DiscordEmote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuExecutorDeclaration

fun ActionRowBuilder.selectMenu(
    executor: SelectMenuExecutorDeclaration,
    builder: SelectMenuBuilder.() -> Unit = {}
) {
    selectMenu(
        executor.id.value
    ) {
        builder.invoke(this)
    }
}

fun ActionRowBuilder.selectMenu(
    executor: SelectMenuExecutorDeclaration,
    data: String,
    builder: SelectMenuBuilder.() -> Unit = {}
) {
    selectMenu(
        "${executor.id.value}:$data"
    ) {
        builder.invoke(this)
    }
}

// https://youtrack.jetbrains.com/issue/KT-6519
@get:JvmSynthetic // Hide from Java callers
var SelectOptionBuilder.loriEmoji: Emote
    @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
    get() = throw UnsupportedOperationException()
    set(value) {
        this.emoji = when (value) {
            is DiscordEmote -> DiscordPartialEmoji(
                Snowflake(value.id),
                value.name,
                value.animated.optional()
            )
            is UnicodeEmote -> DiscordPartialEmoji(
                name = value.name
            )
        }
    }