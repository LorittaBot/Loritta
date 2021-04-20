package net.perfectdreams.loritta.commands.minecraft.declarations

import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object MinecraftPlayerCommand : CommandDeclaration {
    override fun declaration() = command(listOf("mcplayer"), CommandCategory.MINECRAFT) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("skin")) {
            description = LocaleKeyData("commands.command.mcskin.description")
            executor = McSkinExecutor
        }

        subcommand(listOf("avatar")) {
            description = LocaleKeyData("commands.command.mcavatar.description")
            executor = McAvatarExecutor
        }

        subcommand(listOf("head")) {
            description = LocaleKeyData("commands.command.mchead.description")
            executor = McHeadExecutor
        }

        subcommand(listOf("body")) {
            description = LocaleKeyData("commands.command.mcbody.description")
            executor = McBodyExecutor
        }
    }
}