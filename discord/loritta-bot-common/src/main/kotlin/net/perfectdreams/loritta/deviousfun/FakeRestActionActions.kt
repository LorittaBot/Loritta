package net.perfectdreams.loritta.deviousfun

/**
 * Fake queue call that mimicks JDA's .queue() (sort of, it doesn't handle exception shallowing)
 *
 * In the future, it would be good to replace this with Kotlin's [runCatching]
 */
fun <T> T.queue(): T = this

/**
 * Fake queue call that mimicks JDA's .queue()
 *
 * In the future, it would be good to replace this with Kotlin's [runCatching]
 */
inline fun <T> T.queue(action: (T) -> (Unit)) {
    action.invoke(this)
}

/**
 * Fake queue call that mimicks JDA's .queue()
 *
 * In the future, it would be good to replace this with Kotlin's [runCatching]
 */
inline fun <T> T.queue(success: (T) -> (Unit), failure: (T) -> (Unit)) {
    try {
        success.invoke(this)
    } catch (e: Throwable) {
        failure.invoke(this)
    }
}

/**
 * Fake await call that mimicks Loritta's .await()
 */
fun <T> T.await(): T = this

/**
 * Fake complete call that mimicks JDA's .complete()
 */
fun <T> T.complete(): T = this