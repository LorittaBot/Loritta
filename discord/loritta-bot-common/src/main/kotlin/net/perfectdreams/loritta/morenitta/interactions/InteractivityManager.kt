package net.perfectdreams.loritta.morenitta.interactions

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class InteractivityManager {
    companion object {
        // "Interaction tokens are valid for 15 minutes, meaning you can respond to an interaction within that amount of time."
        // However technically we don't care about the token invalidation time if a component is clicked
        val INTERACTION_INVALIDATION_DELAY = 5.minutes
    }

    val scope = CoroutineScope(Dispatchers.Default)

    val buttonInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ComponentContext) -> (Unit)>()
        .asMap()
    val selectMenuInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ComponentContext, List<String>) -> (Unit)>()
        .asMap()
    val modalCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(INTERACTION_INVALIDATION_DELAY.toJavaDuration())
        .build<UUID, suspend (ModalContext, ModalArguments) -> (Unit)>()
        .asMap()

    /**
     * Creates an interactive button
     */
    fun buttonForUser(
        targetUser: User,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, style, label, builder, callback)

    /**
     * Creates an interactive button
     */
    fun buttonForUser(
        targetUserId: Long,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        style,
        label,
        builder
    ) {
        if (targetUserId != it.user.idLong) {
            it.reply(true) {
                styled(
                    it.i18nContext.get(I18nKeysData.Commands.YouArentTheUserSingleUser("<@$targetUserId>")),
                    Emotes.LoriRage
                )
            }
            return@button
        }

        callback.invoke(it)
    }

    /**
     * Creates an interactive button
     */
    fun button(
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = callback
        return Button.of(style, UnleashedComponentId(buttonId).toString(), label)
            .let {
                JDAButtonBuilder(it).apply(builder).button
            }
    }

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenuForUser(
        targetUser: User,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenuForUser(targetUser.idLong, builder, callback)

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenuForUser(
        targetUserId: Long,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenu(
        builder
    ) { context, strings ->
        if (targetUserId != context.user.idLong) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.YouArentTheUserSingleUser("<@$targetUserId>")),
                    Emotes.LoriRage
                )
            }
            return@stringSelectMenu
        }

        callback.invoke(context, strings)
    }

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenu(
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ): StringSelectMenu {
        val buttonId = UUID.randomUUID()
        selectMenuInteractionCallbacks[buttonId] = callback
        return StringSelectMenu.create(UnleashedComponentId(buttonId).toString())
            .apply(builder)
            .build()
    }

    class JDAButtonBuilder(internal var button: Button) {
        // https://youtrack.jetbrains.com/issue/KT-6519
        @get:JvmSynthetic // Hide from Java callers
        var emoji: Emoji
            @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
            get() = throw UnsupportedOperationException()
            set(value) {
                button = button.withEmoji(value)
            }

        @get:JvmSynthetic // Hide from Java callers
        var loriEmoji: Emote
            @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
            get() = throw UnsupportedOperationException()
            set(value) {
                emoji = when (value) {
                    is DiscordEmote -> Emoji.fromCustom(
                        value.name,
                        value.id,
                        value.animated
                    )
                    is UnicodeEmote -> Emoji.fromUnicode(value.name)
                }
            }

        var disabled
            get() = button.isDisabled
            set(value) {
                this.button = button.withDisabled(value)
            }
    }
}