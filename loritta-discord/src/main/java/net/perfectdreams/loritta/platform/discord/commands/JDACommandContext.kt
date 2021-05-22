package net.perfectdreams.loritta.platform.discord.commands

import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class JDACommandContext(
    override val loritta: LorittaDiscord,
    locale: BaseLocale,
    user: User,
    channel: MessageChannel
) : CommandContext(
    loritta,
    locale,
    user,
    channel
)