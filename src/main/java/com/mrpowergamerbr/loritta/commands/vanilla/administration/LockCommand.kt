package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.Permission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class LockCommand : AbstractCommand("lock", listOf("trancar"), CommandCategory.ADMIN){
    override fun getDescription(locale: BaseLocale): String {
      return locale["MUTE_DESCRIPTION"]
    }
    override fun getUsage(locale: BaseLocale): CommandArguments {
      return arguments {
        argument(ArgumentType.USER) {
            optional = false
        }
        argument(ArgumentType.TEXT) {
            optional = true
        }
      }
    }
    override fun getExamples(): List<String> {
      return listOf("532334243424234", "324234234243")
    }
    override fun getDiscordPermissions(): List<Permission> {
      return listOf(Permission.MANAGE_SERVER)
    }
    override fun canUseInPrivateChannel(): Boolean {
      return false
    }
    override fun getBotPermissions(): List<Permission> {
      return listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)
    }
    override suspend fun run(context: CommandContext,locale: BaseLocale) {
      if (context.args.isNotEmpty()) {
        val channel = context.event.channel
        if (context.guild.selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
          val everyoneRole = contex.guild.getPublicRole
          val permissionOverride = channel.getPermissionOverride(everyoneRole)
          if (permissionOverride == null) {
            channel.createPermissionOverride(everyoneRole)
                      .setDeny(Permission.MESSAGE_WRITE)
                      .queue()
          } else {
            if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
              permissionOverride.manager
                .deny(Permission.MESSAGE_WRITE)
                .queue()
            }
          }
        } else {
          this.explain(context)
        }
      }
    }
}
