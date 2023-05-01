package net.perfectdreams.loritta.facingworlds.backend

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedFirstSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.facingworlds.backend.plugins.configureRouting
import net.perfectdreams.loritta.facingworlds.backend.routes.GetFacingWorldsRoute
import net.perfectdreams.loritta.facingworlds.backend.routes.api.v1.PostRpcRoute
import net.perfectdreams.loritta.facingworlds.backend.rpc.processors.Processors
import org.jetbrains.exposed.sql.SchemaUtils

class FacingWorldsBackend(val pudding: Pudding) {
    val processors = Processors(this)
    val routes = listOf(
        GetFacingWorldsRoute(),
        PostRpcRoute(this)
    )

    fun start() {
        runBlocking {
            pudding.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog,
                    PowerStreamClaimedFirstSonhosRewardSonhosTransactionsLog
                )
            }
        }

        embeddedServer(CIO, port = 4569) {
            // Enables gzip and deflate compression
            install(Compression)

            install(IgnoreTrailingSlash)

            configureRouting(this@FacingWorldsBackend, routes)
        }.start(wait = true)
    }
}