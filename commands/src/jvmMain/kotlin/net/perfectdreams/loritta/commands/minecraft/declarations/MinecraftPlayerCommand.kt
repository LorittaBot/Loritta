package net.perfectdreams.loritta.commands.minecraft.declarations

import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McOfflineUUIDExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.McUUIDExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object MinecraftPlayerCommand : CommandDeclaration {
    override fun declaration() = command(listOf("mcplayer"), CommandCategory.MINECRAFT, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("skin"), LocaleKeyData("commands.command.mcskin.description").toI18nHelper()) {
            executor = McSkinExecutor
        }

        subcommand(listOf("avatar"), LocaleKeyData("commands.command.mcavatar.description").toI18nHelper()) {
            executor = McAvatarExecutor
        }

        subcommand(listOf("head"), LocaleKeyData("commands.command.mchead.description").toI18nHelper()) {
            executor = McHeadExecutor
        }

        subcommand(listOf("body"), LocaleKeyData("commands.command.mcbody.description").toI18nHelper()) {
            executor = McBodyExecutor
        }

        subcommandGroup(listOf("uuid"), description = LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {
            subcommand(listOf("online"), LocaleKeyData("commands.command.mcuuid.description").toI18nHelper()) {
                executor = McUUIDExecutor
            }

            subcommand(listOf("offline"), LocaleKeyData("commands.command.mcofflineuuid.description").toI18nHelper()) {
                executor = McOfflineUUIDExecutor
            }
        }
    }
}