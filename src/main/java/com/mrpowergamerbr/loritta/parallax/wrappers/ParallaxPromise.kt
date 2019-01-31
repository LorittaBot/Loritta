package com.mrpowergamerbr.loritta.parallax.wrappers

import java.util.function.Function

abstract class ParallaxPromise<T> {
    fun queue() {
        queue(Function {  })
    }

    abstract fun queue(success: java.util.function.Function<T, Any?>)

    abstract fun queue(success: java.util.function.Function<T, Any?>, failure: java.util.function.Function<T, Any?>)
}