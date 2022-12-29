package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank

import dev.kord.common.entity.Permission
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class EditXpExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val mode = string("mode", XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Text) {
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Add, "ADD")
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Remove, "REMOVE")
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Set, "SET")
        }

        val user = optionalUser("user", XpCommand.XP_EDIT_I18N_PREFIX.Options.User.Text)

        val value = optionalInteger("value", XpCommand.XP_EDIT_I18N_PREFIX.Options.Value.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        if (Permission.ManageGuild !in context.member.getPermissions()) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.UserDoesntHavePermissionDiscord(
                            context.i18nContext.get(I18nKeysData.Permissions.ManageGuild)
                        )
                    ),
                    Emotes.LoriZap
                )
            }
        }

        val userToBeEdited = args[options.user] ?: context.member
        val xpValue = args[options.value] ?: 0

        context.deferChannelMessage()

        val guildId = context.guildId

        val userIsMember = (userToBeEdited.asMemberOrNull(guildId) != null)
        val localConfig = loritta.getOrCreateServerConfig(guildId.toLong())

        val localProfile = localConfig.getUserData(loritta, userToBeEdited.id.toLong(), userIsMember)
        val oldUserXp = localProfile.xp

        var newXpValue = when (args[options.mode]) {
            "SET" -> { xpValue }
            "ADD" -> { oldUserXp+xpValue }
            "REMOVE" -> { oldUserXp-xpValue }
            else -> { xpValue }
        }

        if (newXpValue < 0) { newXpValue = 0 }

        loritta.pudding.transaction {
            GuildProfiles.update({ GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.userId eq userToBeEdited.id.toLong()) }) {
                it[xp] = newXpValue
            }
        }

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    XpCommand.XP_EDIT_I18N_PREFIX.EditedXp(userToBeEdited.mention, oldUserXp, newXpValue)
                ),
                Emotes.LoriShining
            )
        }
    }
}