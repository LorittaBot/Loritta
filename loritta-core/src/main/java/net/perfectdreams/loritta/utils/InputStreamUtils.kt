package net.perfectdreams.loritta.utils

import java.io.IOException
import java.io.InputStream
import java.util.*

private val MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8

@Throws(IOException::class)
fun InputStream.readAllBytes(limit: Int): ByteArray {
	var buf = ByteArray(DEFAULT_BUFFER_SIZE)
	var capacity = buf.size
	var nread = 0
	var n: Int
	while (true) {
		// read to EOF which may read more or less than initial buffer size
		while (read(buf, nread, capacity - nread).let { n = it; n > 0 })
			nread += n;

		// if the last call to read returned -1, then we're done
		if (n < 0)
			break

		// need to allocate a larger buffer
		if (capacity <= MAX_BUFFER_SIZE - capacity) {
			val newCapacity = capacity shl 1
			if (newCapacity >= limit)
				throw StreamExceedsLimitException()
			capacity = capacity shl 1
		} else {
			if (capacity == MAX_BUFFER_SIZE)
				throw OutOfMemoryError("Required array size too large")
			capacity = MAX_BUFFER_SIZE
		}
		buf = Arrays.copyOf(buf, capacity)
	}
	return if (capacity == nread) buf else Arrays.copyOf(buf, nread)
}

class StreamExceedsLimitException : RuntimeException()