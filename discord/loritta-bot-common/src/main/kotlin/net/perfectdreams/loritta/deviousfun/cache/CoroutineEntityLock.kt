package net.perfectdreams.loritta.deviousfun.cache

import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineEntityLock(
    val mutex: Mutex
) : AbstractCoroutineContextElement(CoroutineEntityLock) {
    companion object Key : CoroutineContext.Key<CoroutineEntityLock>
}