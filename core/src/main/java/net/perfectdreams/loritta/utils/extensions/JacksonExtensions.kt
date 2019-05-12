package net.perfectdreams.loritta.utils.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.math.BigInteger

val JsonNode.obj
    get() = this as ObjectNode

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