package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info

import dev.kord.common.Color
import dev.kord.core.entity.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.cinnamon.discord.utils.StoredGenericInteractionData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot

class ShowGuildMemberPermissionsExecutor(loritta: LorittaBot) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.SHOW_GUILD_MEMBER_PERMISSIONS_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decodedInteractionData = ComponentDataUtils.decode<StoredGenericInteractionData>(context.data)
        val interactionDataFromDatabase = Json.decodeFromJsonElement<GuildMemberPermissionsData>(
            context.loritta.pudding.interactionsData.getInteractionData(decodedInteractionData.interactionDataId)
                ?: context.failEphemerally {
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
                    interactionDataFromDatabase.roles.joinToString { "<@&${it}>" }.ifBlank { "\u200b" },
                    false
                )

                val permissionList = interactionDataFromDatabase.permissions.values.toLocalized()?.joinToString(
                    ", ",
                    transform = { "`${context.i18nContext.get(it)}`" }
                )

                if (permissionList != null) {
                    field(
                        "${Emotes.LoriAngel} ${context.i18nContext.get(UserCommand.I18N_PREFIX.Info.Member.Permissions)}",
                        permissionList.ifEmpty { "\u200B" },
                        false
                    )
                }

                color = interactionDataFromDatabase.color?.let { Color(it) }
            }
        }
    }
}