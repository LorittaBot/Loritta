package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.utils.giveaway.payments.PaymentGateway
import net.perfectdreams.loritta.utils.giveaway.payments.PaymentReason
import org.jetbrains.exposed.sql.transactions.transaction

class MigrationTool(val config: LorittaConfig) {
    fun migrateDonators() {
        val loritta = Loritta(config)
        loritta.initMongo()
        loritta.initPostgreSql()

        println("Migrando doadores...")

        transaction(Databases.loritta) {
            val profiles = Profile.find {
                Profiles.isDonator eq true
            }

            profiles.forEach {
                if (it.isActiveDonator()) {
                    Payment.new {
                        this.money = (it.money + 10).toBigDecimal()
                        this.createdAt = it.donatedAt
                        this.paidAt = it.donatedAt
                        this.gateway = PaymentGateway.OTHER
                        this.userId = it.userId
                        this.reason = PaymentReason.DONATION
                    }

                    if (it.money + 10 >= 19.99) {
                        DonationKey.new {
                            this.userId = it.userId
                            this.value = (it.money + 10)
                            this.expiresAt = it.donatedAt + 2_764_800_000
                        }
                    }
                }
            }
        }


        println("Doadores migrados com sucesso!")
    }

}