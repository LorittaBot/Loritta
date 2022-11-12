package net.perfectdreams.loritta.morenitta.utils

import dev.kord.rest.request.Request
import dev.kord.rest.request.RequestHandler
import dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.KordMetrics

public class MetricsKtorRequestHandler(private val delegate: RequestHandler) :
    RequestHandler by delegate {
    override suspend fun <B : Any, R> handle(request: Request<B, R>): R {
        KordMetrics.requests
            .labels(request.route.path, request.route.method.value)
            .inc()

        return delegate.handle(request)
    }
}