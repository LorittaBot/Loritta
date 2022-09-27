package net.perfectdreams.loritta.cinnamon.discord.interactions.inviteblocker

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class ActivateInviteBlockerBypassButtonClickExecutor(loritta: LorittaBot) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.ACTIVATE_INVITE_BLOCKER_BYPASS_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext) // This should never be ran outside of a guild content anyway
            return

        context.deferUpdateMessage()
        val (_, roleId) = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ActivateInviteBlockerData>()

        if (roleId !in context.member.roleIds)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.YouDontHaveTheRoleAnymore("<@&$roleId>")),
                    Emotes.Error
                )
            }

        val success = loritta.pudding.transaction {
            // Check if it already exists
            if (ServerRolePermissions.select {
                    ServerRolePermissions.guild eq context.guildId.toLong() and
                            (ServerRolePermissions.roleId eq roleId.toLong()) and
                            (ServerRolePermissions.permission eq LorittaPermission.ALLOW_INVITES)
                }.count() == 1L
            ) {
                return@transaction false
            }

            ServerRolePermissions.insert {
                it[ServerRolePermissions.guild] = context.guildId.toLong()
                it[ServerRolePermissions.roleId] = roleId.toLong()
                it[ServerRolePermissions.permission] = LorittaPermission.ALLOW_INVITES
            }
            return@transaction true
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
            if (success) {
                styled(
                    context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.BypassEnabled("<@&$roleId>")),
                    Emotes.LoriHappy
                )
            } else {
                styled(
                    context.i18nContext.get(I18nKeysData.Modules.InviteBlocker.RoleAlreadyHasInviteBlockerBypass("<@&$roleId>")),
                    Emotes.Error
                )
            }
        }
    }
}