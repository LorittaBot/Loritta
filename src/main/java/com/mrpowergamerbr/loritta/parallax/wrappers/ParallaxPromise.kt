package com.mrpowergamerbr.loritta.parallax.wrappers

abstract class ParallaxPromise<T> {
    abstract fun queue(success: java.util.function.Function<T, Any?>)

    abstract fun queue(success: java.util.function.Function<T, Any?>, failure: java.util.function.Function<T, Any?>)
}