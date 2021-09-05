package net.perfectdreams.loritta.commands.minecraft.declarations

import net.perfectdreams.loritta.commands.minecraft.McAvatarExecutor
import net.perfectdreams.loritta.commands.minecraft.McBodyExecutor
import net.perfectdreams.loritta.commands.minecraft.McHeadExecutor
import net.perfectdreams.loritta.commands.minecraft.McOfflineUUIDExecutor
import net.perfectdreams.loritta.commands.minecraft.McSkinExecutor
import net.perfectdreams.loritta.commands.minecraft.McUUIDExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object MinecraftCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Minecraft
    val I18N_CATEGORY_PREFIX = I18nKeysData.Commands.Category.Minecraft

    override fun declaration() = command(listOf("minecraft"), CommandCategory.MINECRAFT, I18N_CATEGORY_PREFIX.Name /* TODO: Use the category description */) {
        subcommandGroup(listOf("player"), I18N_PREFIX.Player.Description) {
            subcommand(listOf("skin"), I18N_PREFIX.Player.Skin.Description) {
                executor = McSkinExecutor
            }

            subcommand(listOf("avatar"), I18N_PREFIX.Player.Avatar.Description) {
                executor = McAvatarExecutor
            }

            subcommand(listOf("head"), I18N_PREFIX.Player.Head.Description) {
                executor = McHeadExecutor
            }

            subcommand(listOf("body"), I18N_PREFIX.Player.Body.Description) {
                executor = McBodyExecutor
            }

            subcommand(listOf("onlineuuid"), I18N_PREFIX.Player.Onlineuuid.Description) {
                executor = McUUIDExecutor
            }

            subcommand(listOf("offlineuuid"), I18N_PREFIX.Player.Offlineuuid.Description) {
                executor = McOfflineUUIDExecutor
            }
        }
    }
}