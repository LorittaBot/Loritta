package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.common.utils.WebsiteVoteSource
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object BotVoteSonhosTransactionsLog: LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val websiteSource = postgresEnumeration<WebsiteVoteSource>("website_source")
    val sonhos = long("sonhos")
    val reason = text("reason").nullable()
}