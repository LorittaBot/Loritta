package net.perfectdreams.loritta.platform.discord.commands

import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.JDAMessageChannel
import net.perfectdreams.loritta.platform.discord.entities.JDAUser

class JDACommandContext(
    override val loritta: LorittaDiscord,
    locale: BaseLocale,
    user: JDAUser,
    channel: JDAMessageChannel,
    val lorittaUser: LorittaUser
) : CommandContext(
    loritta,
    locale,
    user,
    channel
) {
    val jdaUser = user.user
    val jdaMessageChannel = channel.channel
}