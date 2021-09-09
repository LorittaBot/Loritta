package net.perfectdreams.loritta.cinnamon.platform.cli.commands

import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.entities.MessageChannel
import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.locale.BaseLocale
import net.perfectdreams.loritta.cinnamon.platform.cli.LorittaCLI

class CLICommandContext(
    override val loritta: LorittaCLI,
    locale: BaseLocale,
    user: User,
    channel: MessageChannel
) : CommandContext(loritta, locale, user, channel)