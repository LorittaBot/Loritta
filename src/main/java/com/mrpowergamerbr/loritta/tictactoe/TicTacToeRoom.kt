package com.mrpowergamerbr.loritta.tictactoe

class TicTacToeRoom {
	val initializedAt = System.currentTimeMillis()
	var player1: String? = null
	var player2: String? = null
	var currentPlayer: String? = null
	var playfield = intArrayOf(
			0, 0, 0,
			0, 0, 0,
			0, 0, 0
	)

	fun join(userId: String) {
		if (player1 != null) {
			player1 = userId
		} else {
			player2 = userId
		}
	}

	fun makeMove(position: Int, player: String): MovementStatus {
		if (currentPlayer != player) {
			// Movimento inválido (player errado)
			return MovementStatus.WRONG_PLAYER
		}

		if (position !in 0..8) {
			// Movimento inválido (fora do tabuleiro)
			return MovementStatus.OUTSIDE_OF_PLAYFIELD
		}

		val cell = playfield[position]

		if (cell != 0) {
			// Movimento inválido (cédula já ocupada)
			return MovementStatus.ALREADY_OCCUPIED_CELL
		}

		val option = if (currentPlayer == player1) {
			1
		} else {
			2
		}

		playfield[position] = option
		return MovementStatus.SUCCESS
	}

	enum class MovementStatus {
		WRONG_PLAYER,
		OUTSIDE_OF_PLAYFIELD,
		ALREADY_OCCUPIED_CELL,
		SUCCESS
	}

	enum class GameStatus {
		// CONTINUE,
		// CURRENT_PLAYER_WON,

	}
}