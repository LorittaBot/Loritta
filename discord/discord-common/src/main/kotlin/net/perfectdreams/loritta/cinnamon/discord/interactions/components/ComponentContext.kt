package net.perfectdreams.loritta.cinnamon.discord.interactions.components

import dev.kord.common.entity.ComponentType
import dev.kord.common.entity.DiscordChatComponent
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.common.entity.optional.value
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.requests.InteractionRequestState
import net.perfectdreams.discordinteraktions.platforms.kord.utils.runIfNotMissing
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.DiscordEmote

open class ComponentContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    override val interaKTionsContext: ComponentContext
) : InteractionContext(loritta, i18nContext, user, interaKTionsContext) {
    val data: String
        get() = interaKTionsContext.data
    val dataOrNull: String?
        get() = interaKTionsContext.dataOrNull

    suspend fun deferUpdateMessage() = interaKTionsContext.deferUpdateMessage()

    suspend inline fun updateMessage(block: InteractionOrFollowupMessageModifyBuilder.() -> (Unit)) = interaKTionsContext.updateMessage(block)

    /**
     * Checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will run [block]
     *
     * @see SingleUserComponentData
     * @see failEphemerally
     */
    fun requireUserToMatch(data: SingleUserComponentData, block: () -> (Unit)) {
        if (data.userId != user.id)
            block.invoke()
    }

    /**
     * Checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will [fail] or [failEphemerally], depending if the command was deferred ephemerally or not.
     *
     * If the command still haven't been replied or deferred, the command will default to a ephemeral fail.
     *
     * @see SingleUserComponentData
     * @see requireUserToMatch
     */
    fun requireUserToMatchOrContextuallyFail(data: SingleUserComponentData) = requireUserToMatch(data) {
        val b: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {
            styled(
                i18nContext.get(
                    I18nKeysData.Commands.YouArentTheUserSingleUser(
                        mentionUser(data.userId, false)
                    )
                ),
                Emotes.LoriRage
            )
        }

        // We also check for "Deferred Update Message" because we can actually reply with an ephemeral message even if it was marked as a update message
        if (interaKTionsContext.wasInitiallyDeferredEphemerally || interaKTionsContext.bridge.state.value == InteractionRequestState.NOT_REPLIED_YET || interaKTionsContext.bridge.state.value == InteractionRequestState.DEFERRED_UPDATE_MESSAGE)
            failEphemerally(b)
        else
            fail(b)
    }

    /**
     * Checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will [failEphemerally], halting execution.
     *
     * @see SingleUserComponentData
     * @see failEphemerally
     */
    fun requireUserToMatchOrFail(data: SingleUserComponentData) = requireUserToMatch(data) {
        fail {
            styled(
                i18nContext.get(
                    I18nKeysData.Commands.YouArentTheUserSingleUser(
                        mentionUser(data.userId, false)
                    )
                ),
                Emotes.LoriRage
            )
        }
    }

    /**
     * Checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will [failEphemerally], halting execution.
     *
     * @see SingleUserComponentData
     * @see failEphemerally
     */
    fun requireUserToMatchOrFailEphemerally(data: SingleUserComponentData) = requireUserToMatch(data) {
        failEphemerally {
            styled(
                i18nContext.get(
                    I18nKeysData.Commands.YouArentTheUserSingleUser(
                        mentionUser(data.userId, false)
                    )
                ),
                Emotes.LoriRage
            )
        }
    }

    /**
     * Decodes the [data] to [T].
     *
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatchOrFailEphemerally
     */
    inline fun <reified T> decodeDataFromComponent(): T {
        return ComponentDataUtils.decode<T>(data)
    }

    /**
     * Decodes the [data] and checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will [fail] or [failEphemerally], depending on the current request state, halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatchOrFailEphemerally
     */
    inline fun <reified T : SingleUserComponentData> decodeDataFromComponentAndRequireUserToMatch(): T {
        val data = ComponentDataUtils.decode<T>(data)
        requireUserToMatchOrContextuallyFail(data)
        return data
    }

    /**
     * Decodes the [data] or pulls it from the database if needed and checks if the [user] has the same user ID present in the [data].
     *
     * If the data is not present on the database, null will be returned.
     *
     * If it isn't equal, the context will [fail] or [failEphemerally], depending on the current request state, halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatchOrFailEphemerally
     * @see decodeDataFromComponentAndRequireUserToMatch
     */
    suspend inline fun <reified T : SingleUserComponentData> decodeDataFromComponentOrFromDatabaseIfPresentAndRequireUserToMatch(): T? {
        val data = loritta.decodeDataFromComponentOrFromDatabase<T>(data) ?: return null
        requireUserToMatchOrContextuallyFail(data)
        return data
    }

    /**
     * Decodes the [data] or pulls it from the database if needed.
     *
     * If the data is not present on the database, the context will [fail] or [failEphemerally] with the [block] message, depending on the current request state, halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     */
    suspend inline fun <reified T> decodeDataFromComponentOrFromDatabase(
        block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit) = {
            styled(
                i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                Emotes.LoriSleeping
            )
        }
    ): T {
        return loritta.decodeDataFromComponentOrFromDatabase<T>(data)
            ?: if (interaKTionsContext.wasInitiallyDeferredEphemerally || interaKTionsContext.bridge.state.value == InteractionRequestState.NOT_REPLIED_YET)
                failEphemerally(block)
            else
                fail(block)
    }

    /**
     * Updates the current message to disable all components in the message that the component is attached to, and sets the message content
     * to "Loading...".
     *
     * On the component that the user clicked, the text in it will be replaced with "Loading..."
     *
     * **This should not be used if you are planning to send a follow-up message, only for when you are going to update the message where the component is attached to!**
     *
     * **This should not be used for components that can be used by multiple users!** (If it doesn't use [SingleUserComponentData], then you shouldn't use this!)
     *
     * **This should only be used after you validated that the user can use the component!** (Example: After checking with [decodeDataFromComponentAndRequireUserToMatch])
     */
    suspend fun updateMessageSetLoadingState(
        updateMessageContent: Boolean = true,
        disableComponents: Boolean = true,
        loadingEmoji: DiscordEmote = LoadingEmojis.random()
    ) {
        updateMessage {
            if (updateMessageContent)
                styled(
                    i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                    loadingEmoji
                )

            if (disableComponents)
                disableComponents(loadingEmoji, this)
        }
    }

    /**
     * Disables all components in the message that the component is attached to on the [builder].
     *
     * On the component that the user clicked, the text in it will be replaced with "Loading..."
     */
    fun disableComponents(loadingEmoji: DiscordEmote, builder: InteractionOrFollowupMessageModifyBuilder) {
        // The message property isn't null in a component interaction
        interaKTionsContext.discordInteraction.message.value!!.components.value!!.forEach {
            it as DiscordChatComponent // Here only a DiscordChatComponent can exist

            if (it.type == ComponentType.ActionRow) {
                builder.actionRow {
                    it.components.value!!.forEach {
                        it as DiscordChatComponent // Again, only a DiscordChatComponent can exist here

                        when (it.type) {
                            ComponentType.ActionRow -> error("This shouldn't exist here!")
                            ComponentType.Button -> {
                                interactionButton(
                                    it.style.value!!, // The style shouldn't be null if it is a button
                                    generateDisabledComponentId()
                                ) {
                                    disabled = true
                                    label = it.label.value

                                    // We want to get the *raw* custom ID, not the one that was already processed by Discord InteraKTions
                                    if (interaKTionsContext.discordInteraction.data.customId.value == it.customId.value)
                                        emoji = DiscordPartialEmoji(
                                            Snowflake(loadingEmoji.id),
                                            loadingEmoji.name,
                                            animated = loadingEmoji.animated.optional()
                                        )
                                    else
                                        emoji = it.emoji.value
                                }
                            }
                            ComponentType.SelectMenu -> {
                                selectMenu(generateDisabledComponentId()) {
                                    val minValues = it.minValues.value ?: 1
                                    val maxValues = it.maxValues.value ?: 1

                                    this.disabled = true
                                    runIfNotMissing(it.placeholder) { this.placeholder = it }
                                    runIfNotMissing(it.options) { optionList ->
                                        if (optionList != null) {
                                            if (minValues == 1 && maxValues == 1) {
                                                // We will use our own custom "Loading" option, sweet!
                                                option(i18nContext.get(I18nKeysData.Website.Dashboard.Loading), "loading_psst_hey_u_are_cute_uwu") { // heh easter egg
                                                    this.emoji = DiscordPartialEmoji(
                                                        Snowflake(loadingEmoji.id),
                                                        loadingEmoji.name,
                                                        animated = loadingEmoji.animated.optional()
                                                    )
                                                    this.default = true
                                                }
                                            } else {
                                                // If not, we will insert the current options as is, to avoiding shifting the content around
                                                optionList.forEach {
                                                    option(it.label, it.value) {
                                                        runIfNotMissing(it.description) { this.description = it }
                                                        runIfNotMissing(it.emoji) { this.emoji = it }
                                                        // We need to get the current values list from the interaction itself, to avoid the user changing the select menu value, then when Loritta updates the message
                                                        // the selection is *gone*
                                                        this.default = it.value in (interaKTionsContext.discordInteraction.data.values.value ?: emptyList())
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    this.allowedValues = minValues..maxValues
                                }
                            }
                            ComponentType.TextInput -> error("This shouldn't exist here!")
                            is ComponentType.Unknown -> error("This shouldn't exist here!")
                        }
                    }
                }
            }
        }
    }

    /**
     * Decodes the [data] or pulls it from the database if needed and checks if the [user] has the same user ID present in the [data].
     *
     * If the data is not present on the database, the context will [fail] or [failEphemerally] with the [block] message, depending on the current request state, halting execution.
     *
     * If it isn't equal, the context will [fail] or [failEphemerally], depending on the current request state, halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatchOrFailEphemerally
     * @see decodeDataFromComponentAndRequireUserToMatch
     */
    suspend inline fun <reified T : SingleUserComponentData> decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch(
        block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit) = {
            styled(
                i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                Emotes.LoriSleeping
            )
        }
    ): T {
        return decodeDataFromComponentOrFromDatabaseIfPresentAndRequireUserToMatch()
            ?: if (interaKTionsContext.wasInitiallyDeferredEphemerally || interaKTionsContext.bridge.state.value == InteractionRequestState.NOT_REPLIED_YET)
                failEphemerally(block)
            else
                fail(block)
    }
}