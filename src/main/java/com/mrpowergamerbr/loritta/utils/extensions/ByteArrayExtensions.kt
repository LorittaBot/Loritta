package com.mrpowergamerbr.loritta.utils.extensions

fun ByteArray.bytesToHex(): String {
	val hexString = StringBuffer()
	for (i in this.indices) {
		val hex = Integer.toHexString(0xff and this[i].toInt())
		if (hex.length == 1) {
			hexString.append('0')
		}
		hexString.append(hex)
	}
	return hexString.toString()
}