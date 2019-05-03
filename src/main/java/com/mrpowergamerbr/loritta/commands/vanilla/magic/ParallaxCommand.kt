package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory

class ParallaxCommand : AbstractCommand("parallax", category = CommandCategory.MAGIC) {

    override fun canHandle(context: CommandContext): Boolean {
        return context.userHandle.id in Loritta.config.loritta.subOwnerIds || Loritta.config.isOwner(context.userHandle.id)
    }

    override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
        val code = context.args.joinToString(" ")

        val command = NashornCommand("teste", code)
        command.useNewAPI = true

        command.run(context, locale)
    }
}