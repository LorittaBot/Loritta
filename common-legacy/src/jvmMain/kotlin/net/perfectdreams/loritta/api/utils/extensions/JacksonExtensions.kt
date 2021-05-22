package net.perfectdreams.loritta.utils.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.math.BigInteger

operator fun ObjectNode.set(field: String, value: String) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Int) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Short) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Long) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Float) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Double) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Boolean) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: ByteArray) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: BigInteger) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: BigDecimal) = this.put(field, value)
operator fun ObjectNode.set(field: String, value: Any) = this.putPOJO(field, value)

private fun _objectNode(values: Iterator<Pair<String, *>>): ObjectNode {
    val obj = JsonNodeFactory.instance.objectNode()
    for ((key, value) in values) {
        when (value) {
            is String ->  obj.put(key, value)
            is Int ->  obj.put(key, value)
            is Long ->  obj.put(key, value)
            is Double ->  obj.put(key, value)
            is Boolean ->  obj.put(key, value)
            is Short ->  obj.put(key, value)
            is Float ->  obj.put(key, value)
            is ByteArray ->  obj.put(key, value)
            is JsonNode ->  obj.set(key, value)
        }

    }
    return obj
}

fun objectNode(vararg values: Pair<String, *>) = _objectNode(values.iterator())