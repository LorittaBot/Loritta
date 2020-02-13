package com.mrpowergamerbr.loritta.profile

data class ProfileDesign(
		val public: Boolean,
		val clazz: Class<*>,
		val internalType: String,
		val price: Double,
		val createdBy: List<Long>
)