package net.perfectdreams.loritta.morenitta.messageverify.png

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

object PNGChunkUtils {
    fun addChunkToPNG(pngBytes: ByteArray, newChunk: ByteArray): ByteArray {
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

    fun readChunksFromPNG(pngBytes: ByteArray): List<PNGChunk> {
        val chunks = mutableListOf<PNGChunk>()

        // Skip the 8-byte PNG signature
        var pos = 8

        while (pos < pngBytes.size) {
            // Limit how many PNG chunks are read to avoid DoS issues when reading a PNG file
            // (This probably will never happen... but who knows right)
            if (chunks.size > 1024)
                error("Too many parsed PNG chunks! ${chunks.size}")

            val length = ByteBuffer.wrap(pngBytes, pos, 4).order(ByteOrder.BIG_ENDIAN).int
            val type = String(pngBytes, pos + 4, 4, Charsets.US_ASCII)
            val data = pngBytes.sliceArray(pos + 8 until pos + 8 + length)
            val crc = ByteBuffer.wrap(pngBytes, pos + 8 + length, 4).order(ByteOrder.BIG_ENDIAN).int
            pos += 12 + length

            chunks.add(PNGChunk(length, type, data, crc))
        }

        return chunks
    }

    fun createTextPNGChunk(text: String) = createPNGChunk("tEXt", text.toByteArray(Charsets.US_ASCII))

    fun createPNGChunk(type: String, data: ByteArray): ByteArray {
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
}