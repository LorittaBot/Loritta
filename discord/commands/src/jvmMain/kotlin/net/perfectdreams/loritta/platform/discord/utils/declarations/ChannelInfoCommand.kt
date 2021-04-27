package net.perfectdreams.loritta.platform.discord.utils.declarations

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.platform.discord.utils.ChannelInfoExecutor

object ChannelInfoCommand: CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.channelinfo"

    override fun declaration(): CommandDeclarationBuilder = command(listOf("channelinfo"), CommandCategory.UTILS) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = ChannelInfoExecutor
        allowedInPrivateChannel = false
    }
}
