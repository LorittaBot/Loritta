package net.perfectdreams.loritta.legacy.commands.vanilla.minecraft

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import org.apache.commons.codec.Charsets
import java.util.*

class OfflineUUIDCommand : AbstractCommand("mcofflineuuid", listOf("offlineuuid"), CommandCategory.MINECRAFT) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.mcofflineuuid.description")
    override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.playerNameExamples")

    // TODO: Fix Usage

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "minecraft player offlineuuid")

        if (context.args.size == 1) {
            val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + context.args[0]).toByteArray(Charsets.UTF_8))

            context.sendMessage(context.getAsMention(true) + locale["commands.command.mcofflineuuid.result", context.args[0], uuid.toString()])
        } else {
            context.explain()
        }
    }
}