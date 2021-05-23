package net.perfectdreams.loritta.api.utils.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.type.MapType

class FixedMapDeserializer(val mapType: MapType) : JsonDeserializer<Map<*, *>>() {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext): Map<*, *> {
        val any = p0.readValueAsTree<TreeNode>()

        if (any.isArray) {
            val values = mutableMapOf<Any?, Any?>()
            repeat(any.size()) {
                values[it] = convertFromJacksonToJava(mapType.contentType, any.get(it))
            }
            return values
        } else {
            val values = mutableMapOf<Any?, Any?>()
            any.fieldNames().forEach {
                val key = when {
                    mapType.keyType.isTypeOrSubTypeOf(Integer::class.java) -> it.toInt()
                    mapType.keyType.isTypeOrSubTypeOf(Long::class.java) -> it.toLong()
                    mapType.keyType.isTypeOrSubTypeOf(Float::class.java) -> it.toFloat()
                    mapType.keyType.isTypeOrSubTypeOf(Double::class.java) -> it.toDouble()
                    else -> it
                }
                values[key] = convertFromJacksonToJava(mapType.contentType, any.get(it))
            }
            return values
        }
    }

    fun convertFromJacksonToJava(type: JavaType, node: TreeNode): Any {
        return when {
            type.isTypeOrSubTypeOf(String::class.java) -> (node as TextNode).textValue()
            type.isTypeOrSubTypeOf(Integer::class.java) -> (node as IntNode).intValue()
            type.isTypeOrSubTypeOf(Long::class.java) -> (node as TextNode).longValue()
            type.isTypeOrSubTypeOf(Float::class.java) -> (node as TextNode).floatValue()
            type.isTypeOrSubTypeOf(Double::class.java) -> (node as TextNode).doubleValue()
            else -> (node as TextNode).textValue()
        }
    }
}