package com.mrpowergamerbr.loritta.userdata

import java.util.*

class StoreItem(
		val name: String,
		val price: Double,
		val code: String
) {
	// Ao criar, o UUID será gerado pelo próprio servidor
	val uniqueId = UUID.randomUUID()
}