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

    override fun getExamples(locale: BaseLocale): List<String> {
        return listOf("351473717194522647 #000FFF", "cargolegal #000000")
    }

    override val botPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)
    override val discordPermissions: List<Permission> = listOf(Permission.MANAGE_ROLES)
    override val canUseInPrivateChannel: Boolean = false

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (2 > context.args.size) {
            context.explain()
            return
        }
        val hexStr = context.args.drop(1).joinToString(" ")
        val hexColor = ColorUtils.getColorFromString(hexStr)
        val role = context.getRoleAt(0)

        if (role == null) {
            context.reply(locale["commands.moderation.rolecolor.rolenotfound"])
            return
        }
        if (hexColor == null) {
            context.reply(locale["commands.moderation.rolecolor."])
            return
        }
        // E finalmente vamos mudar a cor do cargo
        try {
            val manager = role.manager
            manager.setColor(hexColor).queue()
        } catch(e: Exception) {
            context.reply(locale["commands.moderation.rolecolor.cantChange"], Constants.ERROR)
        }
    }
}