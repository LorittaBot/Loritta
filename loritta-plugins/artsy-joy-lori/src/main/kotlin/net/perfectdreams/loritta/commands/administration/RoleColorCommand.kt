package net.perfectdreams.loritta.commands.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import java.awt.Color

class RoleColorCommand: LorittaCommand(arrayOf("rolecolor", "colorrole", "changerolecolor"), CommandCategory.ADMIN) {
    companion object {
        val HEX_PATTERN = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toRegex()
    }

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.moderation.rolecolor.description"]
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.ROLE) {}
            argument(ArgumentType.TEXT) {}
        }
    }

    override val botPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)
    override val discordPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)


    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (context.args.isEmpty() || context.args.size < 2) {
            context.explain()
            return
        }
        val hexStr = context.args.first()
        val hexMatcher = RoleColorCommand.HEX_PATTERN.find(hexStr)
        var hexColor: Color?
        val roleStr = context.args[1]
        // sim e muit grand
        val role = if (context.discordMessage.mentionedRoles.firstOrNull() != null) {
            context.discordMessage.mentionedRoles.firstOrNull()
        } else if (context.args.isNotEmpty() && roleStr.isValidSnowflake() && context.event.guild!!.getRoleById(roleStr) != null) {
            context.event.guild!!.getRoleById(roleStr)
        } else if (context.event.guild!!.getRolesByName(roleStr, true).isNotEmpty()) {
            context.event.guild!!.getRolesByName(roleStr, true)[0]
        } else if (context.event.guild!!.roles.filter { it.name.contains(roleStr, true) }.isNotEmpty()) {
            context.event.guild!!.roles.filter { it.name.contains(roleStr, true) }.first()
        } else {
            context.reply(locale["commands.discord.rolecolor.roleNotFound"], Constants.ERROR)
            return
        }

        if (hexMatcher != null) {
            hexColor = Color.decode('#' + hexMatcher.groupValues.first())
        } else {
            context.reply(locale["commands.moderation.rolecolor.invalidHex"])
            return
        }

        // E finalmente vamos mudar a cor do cargo
        try {
            val manager = role!!.manager
            manager.setColor(hexColor).queue()
        } catch(e: Exception) {
            context.reply(locale["commands.moderation.rolecolor.cantChange"], Constants.ERROR)
        }
    }
}