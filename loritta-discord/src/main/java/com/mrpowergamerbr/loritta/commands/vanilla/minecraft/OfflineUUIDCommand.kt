package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.apache.commons.codec.Charsets
import java.util.*

class OfflineUUIDCommand : AbstractCommand("mcofflineuuid", listOf("offlineuuid"), CommandCategory.MINECRAFT) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale.toNewLocale()["commands.minecraft.mcofflineuuid.description"]
    }

    override fun getUsage(): String {
        return "nickname"
    }

    override fun getExamples(): List<String> {
        return Arrays.asList("Monerk")
    }

    override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
        if (context.args.size == 1) {
            val uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + context.args[0]).toByteArray(Charsets.UTF_8))

            context.sendMessage(context.getAsMention(true) + locale.toNewLocale()["commands.minecraft.mcofflineuuid.result", context.args[0], uuid.toString()])
        } else {
            context.explain()
        }
    }
}