package net.perfectdreams.loritta.dashboard.frontend.shimeji.render

import net.perfectdreams.loritta.dashboard.frontend.shimeji.GameState
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.LorittaPlayer
import net.perfectdreams.loritta.dashboard.frontend.shimeji.entities.PlayerMovementState
import org.w3c.dom.ROUND
import web.canvas.CanvasLineJoin
import web.canvas.CanvasRenderingContext2D
import web.canvas.round

class RenderedLorittaPlayer(
    m: GameState,
    private val animOffset: Int,
    entity: LorittaPlayer
) : RenderedEntity<LorittaPlayer>(m, entity) {
    override val shouldBeRemoved: Boolean
        get() = entity.dead

    val text = entity.playerType.longName // Text to be displayed
    private val textFont = "24px OpenSansPX" // Font for the text
    private val textures = when (entity.playerType) {
        LorittaPlayer.PlayerType.LORITTA -> LorittaPlayerTextures.LorittaTextures()
        LorittaPlayer.PlayerType.PANTUFA -> LorittaPlayerTextures.PantufaTextures()
        LorittaPlayer.PlayerType.GABRIELA -> LorittaPlayerTextures.GabrielaTextures()
    }

    override val zIndex
        get() = entity.y

    override fun render(ctx: CanvasRenderingContext2D, isGameLogicUpdate: Boolean, deltaMS: Long, d: Double) {
        if (isGameLogicUpdate) {
            beginningX = targetX
            beginningY = targetY
            targetX = entity.x.toDouble()
            targetY = entity.y.toDouble()
        }

        val interpolationPercentage = (deltaMS.toDouble() * d)
        // "The player x, y coordinates are the bottom center of the sprite"
        // -LorittaPlayer
        val spriteXAtTheMiddle = lerp(beginningX, targetX, interpolationPercentage)
        val spriteX = spriteXAtTheMiddle - ((128 * m.horizontalScale) / 2)
        val spriteY = lerp(beginningY, targetY, interpolationPercentage) - (128 * m.verticalScale)

        ctx.font = textFont
        val textToBeDrawn = this.text
        val textWidth = ctx.measureText(textToBeDrawn).width
        val textX = spriteXAtTheMiddle - (textWidth / 2) // Calculate x position for centering text
        val textY = spriteY // Calculate y position for centering text

        ctx.strokeStyle = "black"
        ctx.lineWidth = 3.0
        ctx.lineJoin = CanvasLineJoin.round
        ctx.strokeText(textToBeDrawn, textX, textY) // Stroke text

        ctx.fillStyle = "white"
        ctx.fillText(textToBeDrawn, textX, textY) // Draw text

        val spriteToBeUsed = when (entity.movementState) {
            is PlayerMovementState.FallingState -> textures.hurtTexture
            is PlayerMovementState.IdleState -> textures.idleTexture
            is PlayerMovementState.JumpingState -> textures.jumpingTexture
            is PlayerMovementState.RunningState -> {
                val frameIndex = (((entity.m.elapsedTicks / 10) + animOffset) % 6)

                textures.runningTextures[frameIndex]
            }
            is PlayerMovementState.HurtState -> textures.hurtTexture
        }

        var facingRight = true
        if (entity.speed > 0) {
            facingRight = true
        } else if (0 > entity.speed) {
            facingRight = false
        }

        if (facingRight) {
            ctx.drawImage(spriteToBeUsed, spriteX, spriteY, 128.0, 128.0)
        } else {
            ctx.save()
            ctx.scale(-1.0, 1.0)
            ctx.drawImage(spriteToBeUsed, spriteX * -1, spriteY, 128.0 * -1, 128.0)
            ctx.restore()
        }
    }

    override fun destroy() {}
}