package net.perfectdreams.loritta.cinnamon.platform.interactions.inviteblocker

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaPermission
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.disabledButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import org.jetbrains.exposed.sql.insert

class ActivateInviteBlockerBypassButtonClickExecutor(val m: LorittaCinnamon) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.ACTIVATE_INVITE_BLOCKER_BYPASS_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (context !is GuildComponentContext) // This should never be ran outside of a guild content anyway
            return

        context.deferUpdateMessage()
        val (_, roleId) = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ActivateInviteBlockerData>(data)

        if (roleId !in context.member.roles)
            context.failEphemerally {
                // TODO: Message
                content = "You don't have the role anymore!"
            }

        m.services.transaction {
            ServerRolePermissions.insert {
                it[ServerRolePermissions.guild] = context.guildId.toLong()
                it[ServerRolePermissions.roleId] = roleId.toLong()
                it[ServerRolePermissions.permission] = LorittaPermission.ALLOW_INVITES
            }
        }

        // Update message updates the original interaction message, in this case, where the button is
        // If we sent a message later, it would edit the sent message (because now that's the original interaction message)
        context.updateMessage {
            actionRow {
                disabledButton(
                    ButtonStyle.Primary,
                    context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.AllowSendingInvites)
                ) {
                    loriEmoji = Emotes.LoriPat
                }
            }
        }

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.BypassEnabled("<@&$roleId>")),
                Emotes.LoriHappy
            )
        }
    }
}