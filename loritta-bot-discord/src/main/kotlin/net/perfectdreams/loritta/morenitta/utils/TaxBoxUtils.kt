package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.TaxBoxes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxBoxConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

object TaxBoxUtils {
    const val SERVER_TAX_CUT = 0.5

    fun processServerTaxIfNeeded(tax: Long?, guildId: Long?) {
        // This NEEDS to be within a transaction!
        if (tax == null || guildId == null)
            return

        val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
        val serverTaxCut = (tax * SERVER_TAX_CUT).toLong()

        // While it is in a server and it was taxed, the server tax cut is too smol...
        if (serverTaxCut != 0L) {
            // This has been taxed and we do have a positive cut! Let's add it to the server's tax box...
            TaxBoxConfigs.selectAll()
                .where {
                    TaxBoxConfigs.enabled eq true and (TaxBoxConfigs.id eq guildId)
                }
                .firstOrNull() ?: return // Tax Box isn't enabled! So bail out now...

            // Okay, so the tax box is enabled! But can we actually use the tax box?
            val donationKeys = DonationKeys.selectAll()
                .where {
                    DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq now.toInstant().toEpochMilli())
                }
                .toList()

            val value = donationKeys.sumOf { it[DonationKeys.value] }

            val plan = ServerPremiumPlan.getPlanFromValue(value)

            if (plan.taxBox) {
                // We can, woohoo!
                TaxBoxes.upsert(TaxBoxes.id, onUpdate = {
                    it[TaxBoxes.sonhos] = TaxBoxes.sonhos + serverTaxCut
                }) {
                    it[TaxBoxes.id] = guildId
                    it[TaxBoxes.sonhos] = serverTaxCut
                }
            }
        }
    }
}