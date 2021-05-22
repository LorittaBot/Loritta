package net.perfectdreams.loritta.modules

import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.QuirkyConfig
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class ThankYouLoriModule(val config: QuirkyConfig) : MessageReceivedModule {
    override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
        return config.thankYouLori.enabled && event.channel.idLong == config.thankYouLori.channelId
    }

    override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
        if (event.message.contentRaw.length >= 8) {
            if (MiscUtils.hasInappropriateWords(event.message.contentRaw))
                return false

            event.message.addReaction(config.thankYouLori.reactions.random()).queue()

            if (config.thankYouLori.giveDonationKeyIfSentBeforeTime >= System.currentTimeMillis()) {
                val keyCount = transaction(Databases.loritta) {
                    DonationKey.find {
                        (DonationKeys.userId eq event.author.idLong) and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                    }.count()
                }

                if (keyCount == 0L) {
                    transaction(Databases.loritta) {
                        DonationKey.new {
                            this.userId = event.author.idLong
                            this.expiresAt = config.thankYouLori.expiresAt
                            this.value = config.thankYouLori.donationKeyValue
                        }
                    }

                    event.message.addReaction("\uD83D\uDD11").queue()
                }
            }
        }

        return false
    }
}