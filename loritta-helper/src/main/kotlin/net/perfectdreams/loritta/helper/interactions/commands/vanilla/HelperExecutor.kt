package net.perfectdreams.loritta.helper.interactions.commands.vanilla

import net.perfectdreams.loritta.helper.LorittaHelper
import net.perfectdreams.loritta.helper.utils.slash.PermissionLevel
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments

abstract class HelperExecutor(
    val helper: LorittaHelper,
    val requiredPermissionLevel: PermissionLevel
) : LorittaSlashCommandExecutor() {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val guild = context.guildOrNull

        if (guild == null) {
            context.reply(true) {
                content = "Você não pode usar comandos da Helper!"
            }
            return
        }

        val permissionRoles = helper.config.permissionRoles
        val roleIds = context.member.roles.map { it.idLong }
        val permissionLevel = when {
            permissionRoles.adminRoles.any { it in roleIds } -> PermissionLevel.ADMIN
            permissionRoles.helperRoles.any { it in roleIds } -> PermissionLevel.HELPER
            permissionRoles.fanArtsManagerRoles.any { it in roleIds } -> PermissionLevel.FAN_ARTS_MANAGER
            else -> PermissionLevel.NOTHING
        }

        if (permissionLevel.ordinal > requiredPermissionLevel.ordinal) {
            context.reply(true) {
                content = "Você não pode usar comandos da Helper!"
            }
            return
        }

        executeHelper(context, args)
    }

    abstract suspend fun executeHelper(context: ApplicationCommandContext, args: SlashCommandArguments)
}