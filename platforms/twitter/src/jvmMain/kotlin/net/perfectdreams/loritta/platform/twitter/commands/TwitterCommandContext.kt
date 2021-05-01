package net.perfectdreams.loritta.platform.twitter.commands

import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.twitter.LorittaTwitter

class TwitterCommandContext(
    override val loritta: LorittaTwitter,
    locale: BaseLocale,
    user: User,
    channel: MessageChannel
) : CommandContext(loritta, locale, user, channel)