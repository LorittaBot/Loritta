package com.mrpowergamerbr.loritta.website

enum class LoriWebCode(val errorId: Int, val fancyName: String) {
	// 6xx - SUCCESS / INFO
	// 7xx - USER ERROR
	// 8xx - LORI ERROR
	UNAUTHORIZED(700, "Unauthorized")
}