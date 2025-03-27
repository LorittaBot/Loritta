package net.perfectdreams.loritta.morenitta.interactions

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class InteractivityManager {
    companion object {
        // "Interaction tokens are valid for 15 minutes, meaning you can respond to an interaction within that amount of time."
        // However technically we don't care about the token invalidation time if a component is clicked
        val INTERACTION_INVALIDATION_DELAY = 5.minutes
    }

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun buttonForUser(
        targetUser: User,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, button, callback)

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
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun buttonForUser(
        targetUserId: Long,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        button
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
    ) = button(
        UnleashedButton.of(style, label, null)
            .let {
                JDAButtonBuilder(it).apply(builder).button
            },
        callback
    )

    /**
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun button(
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = callback
        return button
            .withId(UnleashedComponentId(buttonId).toString())
    }

    /**
     * Creates an disabled button
     */
    fun disabledButton(
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {}
    ): Button {
        val buttonId = UUID.randomUUID()
        // In recent JDA updates, JDA trips a check if the label && emoji are empty
        // This is bad for us, because we use this as a builder and, in some things, we set the emoji after the button is created, which
        // completely borks out any buttons that have an empty label + button
        //
        // To work around this, we set a " " label to bypass the check
        // This MUST be refactored later, because if JDA changes the check again, this WILL break!
        return Button.of(style, "disabled:$buttonId", label.let { if (it.isEmpty()) " " else it })
            .let {
                JDAButtonBuilder(it).apply(builder)
                    .apply {
                        disabled = true
                    }
                    .button
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
                emoji = value.toJDA()
            }

        var disabled
            get() = button.isDisabled
            set(value) {
                this.button = button.withDisabled(value)
            }
    }
}