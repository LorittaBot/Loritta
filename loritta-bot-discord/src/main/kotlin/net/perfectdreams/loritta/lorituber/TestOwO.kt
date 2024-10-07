package net.perfectdreams.loritta.lorituber

import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes
import java.util.*

fun bitSetToLong(bitSet: BitSet): Long {
    var value: Long = 0
    for (i in 0 until Long.SIZE_BITS) {
        if (bitSet.get(i)) {
            value = value or (1L shl i)
        }
    }
    return value
}

fun longToBitSet(value: Long): BitSet {
    val bitSet = BitSet()
    for (i in 0 until Long.SIZE_BITS) {
        if ((value shr i) and 1L == 1L) {
            bitSet.set(i)
        }
    }
    return bitSet
}

fun main() {
    val bitsets = longToBitSet(43)
    val vibes = mutableListOf<LoriTuberVideoContentVibes>()
    for (idx in 0 until bitsets.length()) {
        val set = bitsets.get(idx)
        if (set) {
            vibes.add(LoriTuberVideoContentVibes.entries[idx])
        }
    }
    println(vibes)
}