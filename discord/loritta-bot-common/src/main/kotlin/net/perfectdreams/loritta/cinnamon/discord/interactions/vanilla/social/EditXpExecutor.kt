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
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirst
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.entities.LorittaEmote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class EditXpExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", XpCommand.XP_EDIT_I18N_PREFIX.Options.User.Text)

        val value = optionalInteger("value", XpCommand.XP_EDIT_I18N_PREFIX.Options.Value.Text)

        val mode = optionalString("mode", XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Text) {
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Add, "ADD")
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Remove, "REMOVE")
            choice(XpCommand.XP_EDIT_I18N_PREFIX.Options.Mode.Choice.Set, "SET")
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        if (Permission.ManageGuild !in context.member.getPermissions()) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.UserDoesntHavePermissionDiscord(
                        context.i18nContext.get(I18nKeysData.Permissions.BanMembers)
                    )),
                    Emotes.LoriZap
                )
            }
        }

        val userToBeEdited = args[options.user] ?: context.member
        val xpValue = args[options.value] ?: 0
        val mode = args[options.mode] ?: "SET"

        context.deferChannelMessage()

        val guildId = context.guildId
        val filter = GuildProfiles.guildId eq guildId.toLong() and (GuildProfiles.userId eq userToBeEdited.id.toLong())

        val localProfile = loritta.pudding.transaction { GuildProfiles.selectFirstOrNull { (filter) } }
        val oldUserXp = localProfile?.get(GuildProfiles.xp) ?: 0

        var newXpValue = when (mode) {
            "SET" -> { xpValue }
            "ADD" -> { oldUserXp+xpValue }
            "REMOVE" -> { oldUserXp-xpValue }
            else -> { xpValue }
        }

        if (newXpValue < 0) { newXpValue = 0 }

        loritta.pudding.transaction {
            GuildProfiles.update({ filter }) {
                it[GuildProfiles.xp] = newXpValue
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