package net.perfectdreams.dora.utils

import java.math.BigInteger

object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private const val BASE = ALPHABET.length

    fun encode(input: ByteArray): String {
        var num = input.fold(0.toBigInteger()) { acc, byte ->
            acc * 256.toBigInteger() + (byte.toInt() and 0xFF).toBigInteger()
        }

        val encoded = StringBuilder()
        while (num > BigInteger.ZERO) {
            val remainder = (num % BASE.toBigInteger()).toInt()
            num /= BASE.toBigInteger()
            encoded.append(ALPHABET[remainder])
        }

        // Handle leading zeros
        input.takeWhile { it.toInt() == 0 }.forEach { encoded.append(ALPHABET[0]) }

        return encoded.reverse().toString()
    }

    fun decode(input: String): ByteArray {
        var num = BigInteger.ZERO
        for (char in input) {
            num = num * BASE.toBigInteger() + ALPHABET.indexOf(char).toBigInteger()
        }

        // Convert BigInteger to ByteArray
        val byteArray = num.toByteArray()

        // Handle leading zeros
        val leadingZeros = input.takeWhile { it == ALPHABET[0] }.count()
        return ByteArray(leadingZeros) + byteArray.dropWhile { it == 0.toByte() }.toByteArray()
    }
}