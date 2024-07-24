package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

fun createChunk(type: String, data: ByteArray): ByteArray {
    val typeBytes = type.toByteArray(Charsets.US_ASCII)
    val length = data.size
    val buffer = ByteBuffer.allocate(4 + 4 + length + 4)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(length)
    buffer.put(typeBytes)
    buffer.put(data)

    val crcBuffer = ByteBuffer.allocate(typeBytes.size + length)
    crcBuffer.put(typeBytes)
    crcBuffer.put(data)
    val crc = CRC32()
    crc.update(crcBuffer.array())
    buffer.putInt(crc.value.toInt())

    return buffer.array()
}

fun addChunkToPng(file: File, newChunk: ByteArray): File {
    val pngBytes = file.readBytes()

    val signature = pngBytes.take(8).toByteArray()
    val chunks = mutableListOf<ByteArray>()
    var pos = 8

    while (pos < pngBytes.size) {
        val length = ByteBuffer.wrap(pngBytes, pos, 4).order(ByteOrder.BIG_ENDIAN).int
        val chunk = pngBytes.sliceArray(pos until pos + 12 + length)
        pos += 12 + length
        chunks.add(chunk)
    }

    val newFile = File(file.parent, "modified_${file.name}")
    newFile.outputStream().use { output ->
        output.write(signature)
        for (chunk in chunks) {
            val type = String(chunk.sliceArray(4 until 8))
            if (type == "IEND") {
                output.write(newChunk)
            }
            output.write(chunk)
        }
    }

    return newFile
}

fun addChunkToPng(pngBytes: ByteArray, newChunk: ByteArray): ByteArray {
    val signature = pngBytes.take(8).toByteArray()
    val chunks = mutableListOf<ByteArray>()
    var pos = 8

    while (pos < pngBytes.size) {
        val length = ByteBuffer.wrap(pngBytes, pos, 4).order(ByteOrder.BIG_ENDIAN).int
        val chunk = pngBytes.sliceArray(pos until pos + 12 + length)
        pos += 12 + length
        chunks.add(chunk)
    }

    val baos = ByteArrayOutputStream()
    baos.use { output ->
        output.write(signature)
        for (chunk in chunks) {
            val type = String(chunk.sliceArray(4 until 8))
            if (type == "IEND") {
                output.write(newChunk)
            }
            output.write(chunk)
        }
    }

    return baos.toByteArray()
}

fun main() {
    val file = File("D:\\SparklyPowerAssets\\LorittaStuffScratchPad\\1013117678287855647.png")
    // PNG chunks are expected to use Latin-1
    val newChunk = createChunk("tEXt", "A Loritta é muito fofa!".toByteArray(Charsets.US_ASCII))
    val newFile = addChunkToPng(file, newChunk)
    println("Modified PNG saved to: ${newFile.path}")

    val chunks = readChunksFromPng(File("D:\\SparklyPowerAssets\\LorittaStuffScratchPad\\modified_1013117678287855647.png"))

    for (chunk in chunks) {
        println("Chunk Type: ${chunk.type}")
        println("Chunk Length: ${chunk.length}")
        println("Chunk CRC: ${chunk.crc}")
        if (chunk.type == "tEXt") {
            val text = String(chunk.data, Charsets.US_ASCII)
            println("Chunk Data (Text): $text")
        }
        println()
    }
}

data class Chunk(val length: Int, val type: String, val data: ByteArray, val crc: Int)

fun readChunksFromPng(pngBytes: ByteArray): List<Chunk> {
    val chunks = mutableListOf<Chunk>()

    // Skip the 8-byte PNG signature
    var pos = 8

    while (pos < pngBytes.size) {
        val length = ByteBuffer.wrap(pngBytes, pos, 4).order(ByteOrder.BIG_ENDIAN).int
        val type = String(pngBytes, pos + 4, 4, Charsets.US_ASCII)
        val data = pngBytes.sliceArray(pos + 8 until pos + 8 + length)
        val crc = ByteBuffer.wrap(pngBytes, pos + 8 + length, 4).order(ByteOrder.BIG_ENDIAN).int
        pos += 12 + length

        chunks.add(Chunk(length, type, data, crc))
    }

    return chunks
}

fun readChunksFromPng(file: File): List<Chunk> {
    val pngBytes = file.readBytes()
    val chunks = mutableListOf<Chunk>()

    // Skip the 8-byte PNG signature
    var pos = 8

    while (pos < pngBytes.size) {
        val length = ByteBuffer.wrap(pngBytes, pos, 4).order(ByteOrder.BIG_ENDIAN).int
        val type = String(pngBytes, pos + 4, 4, Charsets.US_ASCII)
        val data = pngBytes.sliceArray(pos + 8 until pos + 8 + length)
        val crc = ByteBuffer.wrap(pngBytes, pos + 8 + length, 4).order(ByteOrder.BIG_ENDIAN).int
        pos += 12 + length

        chunks.add(Chunk(length, type, data, crc))
    }

    return chunks
}