package net.perfectdreams.loritta.utils.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class FixedMapDeserializer : JsonDeserializer<Map<*, *>>() {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext): Map<*, *> {
        val any = p0.readValueAsTree<TreeNode>()
        if (any.isArray) {
            val values = mutableMapOf<Any?, Any?>()
            repeat(any.size()) {
                values[it] = any.get(it)
            }
            return values
        } else {
            val values = mutableMapOf<Any?, Any?>()
            any.fieldNames().forEach {
                values[it] = any.get(it)
            }
            return values
        }
    }
}