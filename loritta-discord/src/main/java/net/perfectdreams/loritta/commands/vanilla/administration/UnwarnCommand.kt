package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class UnwarnCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("unwarn", "desavisar"), CommandCategory.MODERATION) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.unwarn"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")
        localizedExamples("$LOCALE_PREFIX.examples")
        usage {
            argument(ArgumentType.USER) {
                optional = false
            }
            argument(ArgumentType.NUMBER) {
                optional = false
            }
        }
        canUseInPrivateChannel = false
        userRequiredPermissions = listOf(
            Permission.KICK_MEMBERS
        )
        botRequiredPermissions = listOf(
            Permission.BAN_MEMBERS,
            Permission.KICK_MEMBERS
        )
        executesDiscord {
            if (args.isEmpty())
                return@executesDiscord explain()

            val user = AdminUtils.checkUser(this) ?:
                return@executesDiscord
            val member = guild.retrieveMemberOrNull(user.handle)

            if (member != null) {
                if (!AdminUtils.checkPermissions(this, member))
                    return@executesDiscord
            }

            val warns = loritta.newSuspendedTransaction {
                Warn.find { (Warns.guildId eq guild.idLong) and (Warns.userId eq user.id) }.toList()
            }

            if (warns.isEmpty()) {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.noWarnsFound", "`${serverConfig.commandPrefix}warnlist`"],
                        Constants.ERROR
                    )
                )
                return@executesDiscord
            }

            if (args.getOrNull(1) == "all") {
                loritta.newSuspendedTransaction {
                    Warns.deleteWhere { (Warns.guildId eq guild.idLong) and (Warns.userId eq user.id) }
                }

                if (warns.size == 1) {
                    reply(
                        LorittaReply(
                            locale["$LOCALE_PREFIX.warnRemoved"],
                            Emotes.LORI_HMPF
                        )
                    )
                    return@executesDiscord
                }

                reply(
                    locale["$LOCALE_PREFIX.warnsRemoved", warns.size],
                    Emotes.LORI_HMPF
                )
                return@executesDiscord
            }

            var warnIndex = 0

            if (args.size >= 2) {
                if (args[1].toIntOrNull() == null && args.getOrNull(1) == "all") {
                    reply(
                        LorittaReply(
                            locale["commands.invalidNumber", args[1]],
                            Constants.ERROR
                        )
                    )
                    return@executesDiscord
                }

                warnIndex = args[1].toInt()
            } else warnIndex = warns.size

            if (warnIndex > warns.size) {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.notEnoughWarns", warnIndex, "`${serverConfig.commandPrefix}warnlist`"],
                        Constants.ERROR
                    )
                )
                return@executesDiscord
            }

            val selectedWarn = warns[warnIndex - 1]

            loritta.newSuspendedTransaction {
                selectedWarn.delete()
            }

            reply(
                LorittaReply(
                    locale["${LOCALE_PREFIX}.warnRemoved"] + " ${Emotes.LORI_HMPF}",
                    "\uD83C\uDF89"
                )
            )
        }
    }
}