package net.perfectdreams.loritta.commands.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.utils.ColorUtils

class RoleColorCommand : LorittaDiscordCommand(arrayOf("rolecolor", "colorrole", "changerolecolor"), CommandCategory.ADMIN) {

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.moderation.rolecolor.description"]
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.ROLE) {}
            argument(ArgumentType.COLOR) {}
        }
    }

    override fun getExamples(locale: BaseLocale): List<String> = locale.getWithType("commands.moderation.rolecolor.examples")

    override val botPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)
    override val discordPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)
    override val canUseInPrivateChannel: Boolean = false

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (2 > context.args.size) {
            context.explain()
            return
        }
        val colorStr = context.args.drop(1).joinToString(" ")
        val color = ColorUtils.getColorFromString(colorStr)
        val role = context.getRoleAt(0)

        if (role == null) {
            context.reply(locale["commands.roleNotFound"])
            return
        }
        if (color == null) {
            context.reply(locale["commands.moderation.rolecolor.invalidColor"])
            return
        }
        // E finalmente vamos mudar a cor do cargo
        try {
            val manager = role.manager
            manager.setColor(color).queue()
            context.reply(locale["commands.moderation.rolecolor.success"], "\uD83C\uDFA8")
        } catch(e: Exception) {
            context.reply(locale["commands.moderation.rolecolor.cantChange"], Constants.ERROR)
        }
    }
}