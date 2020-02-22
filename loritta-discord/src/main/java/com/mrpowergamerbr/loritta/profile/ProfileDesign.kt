package com.mrpowergamerbr.loritta.profile

data class ProfileDesign(
		val public: Boolean,
		val clazz: Class<*>,
		val internalType: String,
		val price: Long,
		val createdBy: List<Long>
)