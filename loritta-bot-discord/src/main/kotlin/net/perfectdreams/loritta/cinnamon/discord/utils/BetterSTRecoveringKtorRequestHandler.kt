package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.Request
import dev.kord.rest.request.RequestHandler

public class BetterSTRecoveringKtorRequestHandler(private val delegate: KtorRequestHandler) :
    RequestHandler by delegate {

    /**
     * @throws ContextException if any exception occurs (this is also the only exception which can be thrown)
     * @see KtorRequestHandler.handle
     */
    override suspend fun <B : Any, R> handle(request: Request<B, R>): R {
        val stacktrace = ContextException()

        return try {
            delegate.handle(request)
        } catch (e: Exception) {
            stacktrace.sanitizeStackTrace()
            e.initCause(stacktrace)
            throw e
        }
    }
}

/**
 * Exception used to save the current stack trace before executing a request.
 *
 * @see StackTraceRecoveringKtorRequestHandler
 */
public class ContextException internal constructor() : RuntimeException() {

    internal fun sanitizeStackTrace() {
        // Remove artifacts of stack trace capturing
        // This is the stack trace element is the creation of the ContextException
        // at dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler.handle(StackTraceRecoveringKtorRequestHandler.kt:23)
        stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
    }
}