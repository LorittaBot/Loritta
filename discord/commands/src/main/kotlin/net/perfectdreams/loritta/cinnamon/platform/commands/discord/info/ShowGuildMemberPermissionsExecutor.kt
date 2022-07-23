package net.perfectdreams.loritta.cinnamon.platform.commands.discord.info

import dev.kord.common.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.cinnamon.platform.utils.StoredGenericInteractionData

class ShowGuildMemberPermissionsExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.SHOW_GUILD_MEMBER_PERMISSIONS_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val decodedInteractionData = ComponentDataUtils.decode<StoredGenericInteractionData>(data)
        val interactionDataFromDatabase = Json.decodeFromJsonElement<GuildMemberPermissionsData>(
            context.loritta.services.interactionsData.getInteractionData(decodedInteractionData.interactionDataId) ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.InteractionDataIsMissingFromDatabaseGeneric),
                    Emotes.LoriSleeping
                )
            }
        )

        context.sendEphemeralMessage {
            embed {
                field(
                    "${Emotes.LoriSunglasses} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.Roles)}",
                    interactionDataFromDatabase.roles.joinToString { "<@&${it}>" },
                    false
                )

                val permissionList = interactionDataFromDatabase.permissions.values.toLocalized()?.joinToString(
                    ", ",
                    transform = { "`${context.i18nContext.get(it)}`" }
                )

                if (permissionList != null) {
                    field(
                        "${Emotes.LoriAngel} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.Permissions)}",
                        permissionList,
                        false
                    )
                }

                color = interactionDataFromDatabase.color?.let { Color(it) }
            }
        }
    }
}