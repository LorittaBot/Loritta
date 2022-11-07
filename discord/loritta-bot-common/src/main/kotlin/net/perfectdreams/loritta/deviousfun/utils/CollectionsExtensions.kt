package net.perfectdreams.loritta.deviousfun.utils

// https://stackoverflow.com/a/58310635/7271796
fun <T> Collection<T>.containsSameElements(other: Collection<T>): Boolean {
    // check collections aren't same
    if (this !== other) {
        // fast check of sizes
        if (this.size != other.size) return false

        // check other contains next element from this
        // all "it" must be in "other", if there isn't, then it should return false
        // (Kotlin fast fails in the "all" check!)
        return this.all { it in other }
    }
    // collections are same or they contain the same elements
    return true
}

inline fun <T> runIfDifferentAndNotNull(source: Collection<T>?, target: Collection<T>?, action: (Collection<T>) -> (Unit)) {
    if ((source == null && target != null) || (source != null && target != null && !source.containsSameElements(target))) {
        action.invoke(target)
    }
}