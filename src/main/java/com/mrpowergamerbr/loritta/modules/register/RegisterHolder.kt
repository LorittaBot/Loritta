package com.mrpowergamerbr.loritta.modules.register

class RegisterHolder(val step: List<RegisterStep>) {
	class RegisterStep(
			val title: String,
			val description: String,
			val thumbnail: String?,
			val maxAnswers: Int,
			val options: List<RegisterOption>
	)

	class RegisterOption(
			val emote: String,
			val roleId: String
	)
}