package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.requests.InteractionRequestState
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils

open class ComponentContext(
    loritta: LorittaCinnamon,
    i18nContext: I18nContext,
    user: User,
    override val interaKTionsContext: ComponentContext
) : InteractionContext(loritta, i18nContext, user, interaKTionsContext) {
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
     * Decodes the [data] and checks if the [user] has the same user ID present in the [data].
     *
     * If it isn't equal, the context will [fail] or [failEphemerally], depending on the current request state, halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatchOrFailEphemerally
     */
    inline fun <reified T : SingleUserComponentData> decodeDataFromComponentAndRequireUserToMatch(dataAsString: String): T {
        val data = ComponentDataUtils.decode<T>(dataAsString)
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
    suspend inline fun <reified T : SingleUserComponentData> decodeDataFromComponentOrFromDatabaseIfPresentAndRequireUserToMatch(dataAsString: String): T? {
        val data = loritta.decodeDataFromComponentOrFromDatabase<T>(dataAsString) ?: return null
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
        dataAsString: String,
        block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit) = {
            styled(
                i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                Emotes.LoriSleeping
            )
        }
    ): T {
        return loritta.decodeDataFromComponentOrFromDatabase<T>(dataAsString)
            ?: if (interaKTionsContext.wasInitiallyDeferredEphemerally || interaKTionsContext.bridge.state.value == InteractionRequestState.NOT_REPLIED_YET)
                failEphemerally(block)
            else
                fail(block)
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
        dataAsString: String,
        block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit) = {
            styled(
                i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                Emotes.LoriSleeping
            )
        }
    ): T {
        return decodeDataFromComponentOrFromDatabaseIfPresentAndRequireUserToMatch(dataAsString)
            ?: if (interaKTionsContext.wasInitiallyDeferredEphemerally || interaKTionsContext.bridge.state.value == InteractionRequestState.NOT_REPLIED_YET)
                failEphemerally(block)
            else
                fail(block)
    }
}