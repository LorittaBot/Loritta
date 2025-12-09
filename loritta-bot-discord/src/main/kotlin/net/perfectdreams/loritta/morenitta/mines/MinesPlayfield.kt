package net.perfectdreams.loritta.morenitta.mines

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.security.SecureRandom

class MinesPlayfield(val random: SecureRandom, val totalMines: Int, val houseEdge: Double) {
    // A simple "Mines" implementation :3
    companion object {
        // We use 4x4 playfield instead of the common 5x5 because 5x5 does NOT fit well on my phone screen
        const val PLAYFIELD_WIDTH = 4
        const val PLAYFIELD_HEIGHT = 4
        const val TOTAL_TILES = PLAYFIELD_WIDTH * PLAYFIELD_HEIGHT

        private fun calculateMinesPayoutMultiplier(
            totalTiles: Int,
            mineCount: Int,
            picksMade: Int,
            houseEdge: Double
        ): Double {
            require(totalTiles > picksMade) { "Picks made cannot be equal or higher to the total amount of tiles!" }

            var multiplier = BigDecimal.ONE

            for (i in 0 until picksMade) {
                val remainingTiles = BigDecimal.valueOf((totalTiles - i).toLong())
                val remainingSafeTiles = BigDecimal.valueOf(((totalTiles - mineCount) - i).toLong())
                multiplier = multiplier.multiply(remainingTiles)
                    .divide(remainingSafeTiles, MathContext.DECIMAL128)
            }

            if (picksMade > 0) {
                val houseEdgeModifier = BigDecimal.ONE.subtract(houseEdge.toBigDecimal())
                multiplier = multiplier.multiply(houseEdgeModifier)
            }

            return multiplier.setScale(2, RoundingMode.HALF_DOWN).toDouble()
        }
    }

    val tiles = Array(PLAYFIELD_WIDTH) { Array(PLAYFIELD_HEIGHT) { false } }
    val pickedTiles = Array(PLAYFIELD_WIDTH) { Array(PLAYFIELD_HEIGHT) { false } }
    var gameState: GameState = GameState.Playing

    init {
        require(totalMines > 0) { "Total mines must be greater than 0!" }
        require(TOTAL_TILES > totalMines) { "Total mines must be less than $TOTAL_TILES!" }

        var spawnedMines = 0

        while (spawnedMines != this.totalMines) {
            val x = random.nextInt(PLAYFIELD_WIDTH)
            val y = random.nextInt(PLAYFIELD_HEIGHT)

            // Skip if there's already a mine where we want to spawn
            if (tiles[x][y])
                continue

            tiles[x][y] = true

            spawnedMines++
        }
    }

    fun pick(x: Int, y: Int): PickResult {
        val hasAlreadyPicked = pickedTiles[x][y]

        if (hasAlreadyPicked)
            return PickResult.AlreadyPicked

        if (tiles[x][y]) {
            this.gameState = GameState.GameOver(false, false, calculateMinesPayoutMultiplier(), Position(x, y))
            return PickResult.Mine
        }

        pickedTiles[x][y] = true

        val picked = getPickedCount()

        if (TOTAL_TILES - totalMines == picked) {
            this.gameState = GameState.GameOver(true, true, calculateMinesPayoutMultiplier(), null)
            return PickResult.Success(true, calculateMinesPayoutMultiplier())
        }

        return PickResult.Success(false, calculateMinesPayoutMultiplier())
    }

    fun getPickedCount(): Int {
        var picked = 0

        for (x in 0 until PLAYFIELD_WIDTH) {
            for (y in 0 until PLAYFIELD_HEIGHT) {
                if (pickedTiles[x][y])
                    picked++
            }
        }

        return picked
    }

    fun hasPickedAllSafeTiles() = getPickedCount() == (TOTAL_TILES - totalMines)

    /**
     * Calculates the current mines payout
     */
    fun calculateMinesPayoutMultiplier(): Double {
        val picksMade = getPickedCount()

        return calculateMinesPayoutMultiplier(TOTAL_TILES, totalMines, picksMade, houseEdge)
    }

    /**
     * Calculates the next mines payout if another tile was picked
     *
     * Returns null if the playfield is already cleared
     */
    fun calculateNextMinesPayoutMultiplier(): Double? {
        if (hasPickedAllSafeTiles())
            return null

        val picksMade = getPickedCount()

        return calculateMinesPayoutMultiplier(TOTAL_TILES, totalMines, picksMade + 1, houseEdge)
    }

    fun payout(): PayoutResult {
        val picked = getPickedCount()
        this.gameState = GameState.GameOver(hasPickedAllSafeTiles(), true, calculateMinesPayoutMultiplier(), null)

        return if (picked == 0) {
            PayoutResult.NoTilesPicked
        } else {
            PayoutResult.Success(calculateMinesPayoutMultiplier())
        }
    }

    fun canPayout(): Boolean {
        val picked = getPickedCount()
        return this.gameState is GameState.Playing && picked != 0
    }

    sealed class PickResult {
        data class Success(val clearedPlayfield: Boolean, val payoutMultiplier: Double) : PickResult()
        data object Mine : PickResult()
        data object AlreadyPicked : PickResult()
    }

    sealed class PayoutResult {
        data class Success(val payoutMultiplier: Double) : PayoutResult()
        data object NoTilesPicked : PayoutResult()
    }

    sealed class GameState {
        data object Playing : GameState()
        data class GameOver(
            val clearedPlayfield: Boolean,
            val askedForPayout: Boolean,
            val payoutMultiplier: Double,
            val explodedAt: Position?
        ) : GameState()
    }

    data class Position(
        val x: Int,
        val y: Int
    )
}

