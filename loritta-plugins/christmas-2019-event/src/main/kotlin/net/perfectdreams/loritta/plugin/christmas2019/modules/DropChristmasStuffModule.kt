package net.perfectdreams.loritta.plugin.christmas2019.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.chance
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019Config
import net.perfectdreams.loritta.tables.Christmas2019Players
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit

class DropChristmasStuffModule(val config: Christmas2019Config) : MessageReceivedModule {
    val lastDropsAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
            .asMap()
    val lastDropsByUserAt = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build<Long, Long>()
            .asMap()

    override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
        return event.guild?.selfMember?.hasPermission(Permission.MESSAGE_ADD_REACTION) == true && Calendar.getInstance().get(Calendar.MONTH) == 11
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
        val date = System.currentTimeMillis()

        val millis = event.member!!.timeJoined.toInstant().toEpochMilli()

        val diff = date - millis

        var chance = Math.min((diff.toDouble() * 2.0) / 1_296_000_000, 2.0)

        val id = event.channel.idLong
        val lastDrop = lastDropsAt.getOrDefault(id, 0L)

        val lastDropDiff = System.currentTimeMillis() - lastDrop

        val since = 360_000 - Math.max(360_000 - lastDropDiff, 0)

        val chanceBoost = (6.0 * since) / 360_000

        val ceil = 3.0

        chance = Math.min(chance + chanceBoost, ceil)

        if (chance(chance) && event.message.contentStripped.hashCode() == lorittaProfile.lastMessageSentHash) {
            if (5_000 >= System.currentTimeMillis() - lastDrop)
                return false

            val userDropTime = lastDropsByUserAt.getOrDefault(event.author.idLong, 0L)

            if (180_000 >= System.currentTimeMillis() - userDropTime)
                return false

            val isParticipating = transaction(Databases.loritta) {
                Christmas2019Players.select {
                    Christmas2019Players.user eq lorittaProfile.id
                }.count() != 0
            }

            val getTheCandy = isParticipating && Christmas2019.isEventActive()

            if (getTheCandy) {
                lastDropsAt[id] = System.currentTimeMillis()
                lastDropsByUserAt[event.author.idLong] = System.currentTimeMillis()
                event.message.addReaction("lori_gift:653402818199158805").queue()
            }
        }

        return false
    }
}