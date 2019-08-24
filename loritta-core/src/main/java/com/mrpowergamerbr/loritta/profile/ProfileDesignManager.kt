package com.mrpowergamerbr.loritta.profile

class ProfileDesignManager {
	val designs = mutableListOf<ProfileDesign>()
	val publicDesigns: List<ProfileDesign>
		get() = designs.filter { it.public }

	fun registerDesign(design: ProfileDesign) {
		designs.removeIf { it.internalType == design.internalType }
		designs.add(design)
	}

	fun unregisterDesign(design: ProfileDesign) {
		designs.removeIf { it.internalType == design.internalType }
	}

	init {
		registerDesign(
				ProfileDesign(true, NostalgiaProfileCreator::class.java, "default", 0.0, listOf())
		)
		registerDesign(
				ProfileDesign(true, DefaultProfileCreator::class.java, "modern", 2000.0, listOf())
		)
		registerDesign(
				ProfileDesign(true, MSNProfileCreator::class.java, "msn", 7500.0, listOf())
		)
		registerDesign(
				ProfileDesign(true, OrkutProfileCreator::class.java, "orkut", 7500.0, listOf())
		)
	}
}