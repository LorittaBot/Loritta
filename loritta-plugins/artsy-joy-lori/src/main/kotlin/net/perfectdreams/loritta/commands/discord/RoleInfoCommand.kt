/* owo e uwu */
package net.perfectdreams.loritta.commands.discord

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.extensions.localized
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class RoleInfoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("roleinfo", "taginfo"), CommandCategory.DISCORD) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.roleinfo.description")

        canUseInPrivateChannel = false

        arguments {
            argument(ArgumentType.TEXT) {}
        }

        executesDiscord {
            val context = this

            if (args.isEmpty()) explainAndExit()

            val role = context.role(0)

            if (role != null) {
                val builder = EmbedBuilder()

                val isHoisted = if (role.isHoisted) {
                    locale["loritta.fancyBoolean.true"]
                } else {
                    locale["loritta.fancyBoolean.false"]
                }
                val isMentionable = if (role.isMentionable) {
                    locale["loritta.fancyBoolean.true"]
                } else {
                    locale["loritta.fancyBoolean.false"]
                }
                val isIntegrationBot = if (role.isManaged) {
                    locale["loritta.fancyBoolean.true"]
                } else {
                    locale["loritta.fancyBoolean.false"]
                }
                val permissions = role.permissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })

                builder.setTitle("\uD83D\uDCBC ${role.name}")
                if (role.color != null)
                    builder.setColor(role.color)
                builder.addField("\uD83D\uDC40 ${locale["$LOCALE_PREFIX.roleinfo.roleMention"]}", "`${role.asMention}`", true)
                builder.addField("\uD83D\uDCC5 ${locale["$LOCALE_PREFIX.roleinfo.roleCreated"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(role.timeCreated, locale), true)
                builder.addField("\uD83D\uDCBB ${locale["$LOCALE_PREFIX.roleinfo.roleID"]}", "`${role.id}`", true)
                builder.addField(locale["$LOCALE_PREFIX.roleinfo.roleHoisted"], isHoisted, true)
                builder.addField("<:bot:516314838541008906> ${locale["$LOCALE_PREFIX.roleinfo.roleIntegration"]}", isIntegrationBot, true)
                builder.addField("\uD83D\uDC40 ${locale["$LOCALE_PREFIX.roleinfo.roleMentionable"]}", isMentionable, true)
                builder.addField("\uD83D\uDC65 ${locale["$LOCALE_PREFIX.roleinfo.roleMembers"]}", context.guild.getMembersWithRoles(role).size.toString(),true)
                if (role.color != null)
                    builder.addField("🎨 ${locale["$LOCALE_PREFIX.roleinfo.roleColor"]}", "`#${Integer.toHexString(role.color!!.rgb).substring(2).toUpperCase()}`", true)
                builder.addField("\uD83D\uDEE1 ${locale["$LOCALE_PREFIX.roleinfo.rolePermissions"]}", permissions, false)

                context.sendMessage(context.getUserMention(true), builder.build())
            } else {
                context.reply(
                        LorittaReply(
                                locale["commands.command.roleinfo.roleNotFound"],
                                Constants.ERROR
                        )
                )
            }
        }
    }
}