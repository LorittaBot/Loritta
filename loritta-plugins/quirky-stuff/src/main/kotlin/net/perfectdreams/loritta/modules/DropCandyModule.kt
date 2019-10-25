package net.perfectdreams.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.chance
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.tables.Halloween2019Players
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DropCandyModule(val config: QuirkyConfig) : MessageReceivedModule {
    override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
        return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
        if (lorittaProfile.userId == 123170274651668480L)
            println("${event.message.contentStripped.hashCode()} -> ${lorittaProfile.lastMessageSentHash}")

        if (chance(3.0) && event.message.contentStripped.hashCode() == lorittaProfile.lastMessageSentHash) {
            val isParticipating = transaction(Databases.loritta) {
                Halloween2019Players.select {
                    Halloween2019Players.user eq lorittaProfile.id
                }.count() != 0
            }

            val getTheCandy = isParticipating && Calendar.getInstance()[Calendar.MONTH] == 9

            if (getTheCandy)
                event.message.addReaction("\uD83C\uDF6C").queue()
        }

        return false
    }
}