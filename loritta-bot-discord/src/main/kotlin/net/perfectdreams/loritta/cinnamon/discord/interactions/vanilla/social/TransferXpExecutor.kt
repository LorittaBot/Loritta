package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

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
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

class TransferXpExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta)  {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = user("user", XpCommand.XP_TRANSFER_I18N_PREFIX.Options.User.Text)

        val target = user("target", XpCommand.XP_TRANSFER_I18N_PREFIX.Options.Target.Text)

        val mode = string("mode", XpCommand.XP_TRANSFER_I18N_PREFIX.Options.Mode.Text) {
            choice(XpCommand.XP_TRANSFER_I18N_PREFIX.Options.Mode.Choice.Add, "ADD")
            choice(XpCommand.XP_TRANSFER_I18N_PREFIX.Options.Mode.Choice.Set, "SET")
        }
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
                    )),
                    Emotes.LoriZap
                )
            }
        }

        val userToBeEdited = args[options.user]
        val targetToBeEdited = args[options.target]

        context.deferChannelMessage()

        if (userToBeEdited.id == targetToBeEdited.id) {
            context.fail {
                styled(
                    context.i18nContext.get(XpCommand.XP_TRANSFER_I18N_PREFIX.UserIsTheSameAsTarget),
                    Emotes.LoriFire
                )
            }
        }

        val guildId = context.guildId
        val filter = GuildProfiles.guildId eq guildId.toLong()

        val userProfile = loritta.pudding.transaction { GuildProfiles.selectFirstOrNull { (filter and (GuildProfiles.userId eq userToBeEdited.id.toLong())) } }
        val targetProfile = loritta.pudding.transaction { GuildProfiles.selectFirstOrNull { (filter and (GuildProfiles.userId eq targetToBeEdited.id.toLong())) } }

        if (userProfile?.get(GuildProfiles.xp)!! <= 0) {
            context.fail {
                styled(
                    context.i18nContext.get(XpCommand.XP_TRANSFER_I18N_PREFIX.UserHas0Xp),
                    Emotes.LoriHm
                )
            }
        }

        val targetXp = when (args[options.mode]) {
            "ADD" -> { (userProfile.get(GuildProfiles.xp)) + (targetProfile?.get(GuildProfiles.xp)?: 0) }
            else -> { (userProfile.get(GuildProfiles.xp)) }
        }

        loritta.pudding.transaction {
            GuildProfiles.update({ filter and (GuildProfiles.userId eq userToBeEdited.id.toLong()) }) { it[xp] = 0  }
            GuildProfiles.update({ filter and (GuildProfiles.userId eq targetToBeEdited.id.toLong() )}) { it[xp] = targetXp }
        }

        if (args[options.mode] == "SET") {
            context.sendMessage {
                styled(
                    context.i18nContext.get(
                        XpCommand.XP_TRANSFER_I18N_PREFIX.TranferedXp(
                            userToBeEdited.mention,
                            userProfile.get(GuildProfiles.xp), targetToBeEdited.mention, (targetProfile?.get(GuildProfiles.xp) ?: 0)
                        )
                    ),
                    Emotes.LoriShining
                )
            }
        } else {
            context.sendMessage {
                styled(
                    context.i18nContext.get(
                        XpCommand.XP_TRANSFER_I18N_PREFIX.TransferedXpWithAddition(
                            userToBeEdited.mention, userProfile.get(GuildProfiles.xp), targetToBeEdited.mention
                        )
                    ),
                    Emotes.LoriShining
                )
                styled(
                    context.i18nContext.get(
                        XpCommand.XP_TRANSFER_I18N_PREFIX.AdditionInfo(
                            targetToBeEdited.mention, (targetProfile?.get(GuildProfiles.xp)?: 0), targetXp
                        )
                    ),
                    Emotes.LoriSunglasses
                )
            }
        }
    }
}
