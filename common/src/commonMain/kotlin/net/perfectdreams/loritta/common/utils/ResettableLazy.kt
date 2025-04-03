package net.perfectdreams.loritta.common.utils

import kotlin.concurrent.Volatile
import kotlin.reflect.KProperty

// https://stackoverflow.com/questions/35752575/kotlin-lazy-properties-and-values-reset-a-resettable-lazy-delegate
class ResettableLazyManager {
    val managedDelegates = mutableListOf<Resettable>()

    fun register(managed: Resettable) {
        managedDelegates.add(managed)
    }

    fun reset() {
        managedDelegates.forEach { it.reset() }
        managedDelegates.clear()
    }
}

interface Resettable {
    fun reset()
}

class ResettableLazy<PROPTYPE>(val manager: ResettableLazyManager, val init: ()->PROPTYPE): Resettable {
    @Volatile
    var lazyHolder = makeInitBlock()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): PROPTYPE {
        return lazyHolder.value
    }

    override fun reset() {
        lazyHolder = makeInitBlock()
    }

    fun makeInitBlock(): Lazy<PROPTYPE> {
        return lazy {
            manager.register(this)
            init()
        }
    }
}

fun <PROPTYPE> resettableLazy(manager: ResettableLazyManager, init: ()->PROPTYPE): ResettableLazy<PROPTYPE> {
    return ResettableLazy(manager, init)
}

fun resettableManager(): ResettableLazyManager = ResettableLazyManager()