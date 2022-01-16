package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
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
     * If it isn't equal, the context will [failEphemerally], halting execution.
     *
     * @see SingleUserComponentData
     * @see failEphemerally
     */
    fun requireUserToMatch(data: SingleUserComponentData) {
        if (data.userId != user.id)
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
     * If it isn't equal, the context will [failEphemerally], halting execution.
     *
     * @see SingleUserComponentData
     * @see ComponentDataUtils
     * @see failEphemerally
     * @see requireUserToMatch
     */
    inline fun <reified T : SingleUserComponentData> decodeViaComponentDataUtilsAndRequireUserToMatch(dataAsString: String): T {
        val data = ComponentDataUtils.decode<T>(dataAsString)
        requireUserToMatch(data)
        return data
    }
}