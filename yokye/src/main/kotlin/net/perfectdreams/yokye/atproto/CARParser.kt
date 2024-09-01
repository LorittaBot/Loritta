package net.perfectdreams.yokye.atproto

import com.upokecenter.cbor.CBORObject
import java.io.InputStream

fun parseCAR(input: InputStream): MutableList<CBORObject> {
    input.use {
        val headerLength = readUnsignedLEB128VarInt(it)
        // println("version: $headerLength")

        val header = CBORObject.Read(it)
        // println("header: $header")

        val results = mutableListOf<CBORObject>()

        while (it.available() != 0) {
            val dataLength = readUnsignedLEB128VarInt(it)
            // println("data length: $dataLength")

            readCID(it)
            val body = CBORObject.Read(it)
            results.add(body)
            // println(body)
        }
        return results
    }
}

fun readCID(input: InputStream) {
    val version = readUnsignedLEB128VarInt(input)
    // println("CID version: $version")

    val codec = readUnsignedLEB128VarInt(input)
    // println("CID codec: $codec")

    val hashFunc = readUnsignedLEB128VarInt(input)
    // println("hash function: $hashFunc")

    val digestSize = readUnsignedLEB128VarInt(input)
    // println("digest size: $digestSize")

    input.readNBytes(digestSize)
}

private fun readUnsignedLEB128VarInt(input: InputStream): Int {
    var result = 0
    var shift = 0
    var byte: Int

    do {
        byte = input.read()
        if (byte == -1) throw IllegalArgumentException("Unexpected end of stream")

        val value = byte and 0x7F
        result = result or (value shl shift)
        shift += 7
    } while (byte and 0x80 != 0)

    return result
}