package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.MessageChannel

abstract class ParallaxPromise<T> {
    companion object {
        val DEFAULT_CHANNEL_FAILURE_CALLBACK: (MessageChannel, Throwable) -> (Unit) = { channel, t ->
            GlobalScope.launch(loritta.coroutineDispatcher) {
                ParallaxUtils.sendThrowableToChannel(
                        t,
                        channel
                )
            }
        }
    }

    fun queue() {
        queue(null)
    }

    fun queue(success: java.util.function.Function<T, Any?>?) {
        queue(success, null)
    }

    abstract fun queue(success: java.util.function.Function<T, Any?>?, failure: java.util.function.Function<Any?, Any?>?)
}