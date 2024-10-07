package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.InteractionHook
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberRequest
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.serializable.lorituber.requests.LoriTuberRPCRequest


sealed class LoriTuberScreen(
    val command: LoriTuberCommand,
    val user: User,
    // The hook is mutable because we want to replace the current hook on the ReceivedMailScreen, when the mail is acknowledged
    var hook: InteractionHook
) {
    val loritta = command.loritta

    abstract suspend fun render()

    suspend fun <T> sendLoriTuberRPCRequest(request: LoriTuberRPCRequest) = command.sendLoriTuberRPCRequest<T>(request)

    suspend inline fun <reified T> sendLoriTuberRPCRequestNew(request: LoriTuberRequest) = command.sendLoriTuberRPCRequestNew<T>(request)
}