package net.perfectdreams.loritta.legacy.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.LorittaUtils
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class McUUIDCommand : AbstractCommand("mcuuid", category = CommandCategory.MINECRAFT) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.mcuuid.description")
    override fun getExamplesKey() = LocaleKeyData("commands.category.minecraft.playerNameExamples")

    // TODO: Fix Usage

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "minecraft player onlineuuid")

        if (context.args.size > 0) {
            val player = context.args[0]

	        val data = HttpRequest.get("https://api.mojang.com/users/profiles/minecraft/$player").body()

	        try {
                val json = JsonParser.parseString(data).asJsonObject

	            context.sendMessage(context.getAsMention(true) + context.locale["commands.command.mcuuid.result", player, LorittaUtils.getUUID(json["id"].string)])
            } catch (e: IllegalStateException) {
                context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.command.mcuuid.invalid", player])
            }
        } else {
            this.explain(context)
        }
    }
}