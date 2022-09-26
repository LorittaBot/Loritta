/* owo e uwu */
package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.localized
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class RoleInfoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("roleinfo", "taginfo"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
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
            OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "server role info")

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