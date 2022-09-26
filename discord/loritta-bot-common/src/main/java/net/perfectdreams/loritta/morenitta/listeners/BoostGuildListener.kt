package net.perfectdreams.loritta.morenitta.listeners

import net.perfectdreams.loritta.morenitta.utils.NitroBoostUtils
import net.perfectdreams.loritta.morenitta.utils.config.DonatorsOstentationConfig
import net.perfectdreams.loritta.morenitta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class BoostGuildListener(val config: DonatorsOstentationConfig) : ListenerAdapter() {
    override fun onGuildMemberUpdateBoostTime(event: GuildMemberUpdateBoostTimeEvent) {
        val boostAsDonationGuilds = config.boostEnabledGuilds.map { it.id }
        if (event.guild.idLong !in boostAsDonationGuilds)
            return

        if (event.oldTimeBoosted == null && event.newTimeBoosted != null) {
            // Ativou Boost, mas vamos verificar se tem menos boosts que o requisito máximo!
            if (event.guild.boostCount > config.boostMax)
                return

            GlobalScope.launch(loritta.coroutineDispatcher) {
                NitroBoostUtils.onBoostActivate(event.member)
            }
            return
        }

        if (event.newTimeBoosted != null && event.oldTimeBoosted != null) {
            // Desativou Boost
            GlobalScope.launch(loritta.coroutineDispatcher) {
                NitroBoostUtils.onBoostDeactivate(event.member)
            }
            return
        }
    }
}