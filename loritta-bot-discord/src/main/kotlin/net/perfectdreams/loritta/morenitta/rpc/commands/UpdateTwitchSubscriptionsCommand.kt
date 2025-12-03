package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.UpdateTwitchSubscriptionsResponse

class UpdateTwitchSubscriptionsCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.UpdateTwitchSubscriptions) {
    override suspend fun onRequest(call: ApplicationCall) {
        if (loritta.isMainInstance) {
            GlobalScope.launch {
                loritta.twitchSubscriptionsHandler.requestSubscriptionCreation("Update Twitch Subscriptions Request")
            }
        }
        call.respondRPCResponse<UpdateTwitchSubscriptionsResponse>(UpdateTwitchSubscriptionsResponse.Success)
    }
}