package net.perfectdreams.loritta.cinnamon.discord.interactions.components

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.component.SelectMenuBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.ComponentData
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun ActionRowBuilder.interactiveButton(
    style: ButtonStyle,
    executor: ButtonExecutorDeclaration,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    interactionButton(
        style,
        executor.id
    ) {
        builder.invoke(this)
    }
}

fun ActionRowBuilder.interactiveButton(
    style: ButtonStyle,
    label: String,
    executor: ButtonExecutorDeclaration,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit = {}
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    interactionButton(
        style,
        executor.id
    ) {
        this.label = label
        builder.invoke(this)
    }
}

fun ActionRowBuilder.interactiveButton(
    style: ButtonStyle,
    executor: ButtonExecutorDeclaration,
    data: String,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    interactionButton(
        style,
        "${executor.id}:$data"
    ) {
        builder.invoke(this)
    }
}

/**
 * Creates a interactive button with data stored in a hybrid manner
 *
 * @see LorittaCinnamon.encodeDataForComponentOrStoreInDatabase
 */
suspend inline fun <reified T : ComponentData> ActionRowBuilder.interactiveButtonWithHybridData(
    loritta: LorittaCinnamon,
    style: ButtonStyle,
    executor: ButtonExecutorDeclaration,
    data: T,
    ttl: Duration = 15.minutes,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    val encodedData = loritta.encodeDataForComponentOrStoreInDatabase(data, ttl)
    interactionButton(
        style,
        "${executor.id}:$encodedData"
    ) {
        builder.invoke(this)
    }
}

/**
 * Creates a interactive button with data stored in the database
 *
 * @see LorittaCinnamon.encodeDataForComponentOnDatabase
 */
suspend inline fun <reified T : ComponentData> ActionRowBuilder.interactiveButtonWithDatabaseData(
    loritta: LorittaCinnamon,
    style: ButtonStyle,
    executor: ButtonExecutorDeclaration,
    data: T,
    ttl: Duration = 15.minutes,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    val encodedData = loritta.encodeDataForComponentOnDatabase(data, ttl).data

    interactionButton(
        style,
        "${executor.id}:$encodedData"
    ) {
        builder.invoke(this)
    }
}

fun ActionRowBuilder.interactiveButton(
    style: ButtonStyle,
    label: String,
    executor: ButtonExecutorDeclaration,
    data: String,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit = {}
) {
    require(style != ButtonStyle.Link) { "You cannot use a ButtonStyle.Link style in a interactive button! Please use \"linkButton(...)\" if you want to create a button with a link" }

    interactionButton(
        style,
        "${executor.id}:$data"
    ) {
        this.label = label
        builder.invoke(this)
    }
}

fun ActionRowBuilder.selectMenu(
    executor: SelectMenuExecutorDeclaration,
    builder: SelectMenuBuilder.() -> Unit = {}
) {
    selectMenu(
        executor.id
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
        "${executor.id}:$data"
    ) {
        builder.invoke(this)
    }
}

fun generateDisabledComponentId() = "disabled:${UUID.randomUUID()}"

/**
 * Creates a disabled button with a random ID
 */
fun ActionRowBuilder.disabledButton(
    style: ButtonStyle,
    label: String,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit = {}
) {
    interactionButton(style, generateDisabledComponentId()) {
        this.label = label
        this.disabled = true
        builder.invoke(this)
    }
}

/**
 * Creates a disabled button with a random ID
 */
fun ActionRowBuilder.disabledButton(
    style: ButtonStyle,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit = {}
) {
    interactionButton(style, generateDisabledComponentId()) {
        this.disabled = true
        builder.invoke(this)
    }
}

// https://youtrack.jetbrains.com/issue/KT-6519
@get:JvmSynthetic // Hide from Java callers
var SelectOptionBuilder.loriEmoji: net.perfectdreams.loritta.cinnamon.emotes.Emote
    @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
    get() = throw UnsupportedOperationException()
    set(value) {
        this.emoji = when (value) {
            is net.perfectdreams.loritta.cinnamon.emotes.DiscordEmote -> DiscordPartialEmoji(
                Snowflake(value.id),
                value.name,
                value.animated.optional()
            )
            is net.perfectdreams.loritta.cinnamon.emotes.UnicodeEmote -> DiscordPartialEmoji(
                name = value.name
            )
        }
    }

// https://youtrack.jetbrains.com/issue/KT-6519
@get:JvmSynthetic // Hide from Java callers
var ButtonBuilder.loriEmoji: net.perfectdreams.loritta.cinnamon.emotes.Emote
    @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
    get() = throw UnsupportedOperationException()
    set(value) {
        this.emoji = when (value) {
            is net.perfectdreams.loritta.cinnamon.emotes.DiscordEmote -> DiscordPartialEmoji(
                Snowflake(value.id),
                value.name,
                value.animated.optional()
            )
            is net.perfectdreams.loritta.cinnamon.emotes.UnicodeEmote -> DiscordPartialEmoji(
                name = value.name
            )
        }
    }