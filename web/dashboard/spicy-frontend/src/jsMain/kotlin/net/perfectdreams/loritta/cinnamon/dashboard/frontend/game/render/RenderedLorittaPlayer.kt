package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.render

import js.core.jso
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.GameTextures
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.LorittaPlayer
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.game.entities.PlayerMovementState
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Container
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.PixiSprite
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.PixiTexture
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Text

class RenderedLorittaPlayer(
    val textures: GameTextures,
    val animOffset: Int,
    spriteContainer: Container,
    nametagContainer: Container,
    entity: LorittaPlayer
) : RenderedEntity<LorittaPlayer>(spriteContainer, nametagContainer, entity) {
    override val shouldBeRemoved: Boolean
        get() = entity.dead

    var facingRight = true
    var text = Text(entity.owner, jso {
        fill = "white"
        fontSize = 24
        lineJoin = "round"
        strokeThickness = 3
        fontFamily = "m5x7"
    })
    override val sprite: PixiSprite = PixiSprite(textures.lorittaHurtTexture)

    init {
        spriteContainer.addChild(sprite)
        text.anchor.x = 0.5
        text.anchor.y = 0.5
        nametagContainer.addChild(text)
        text.asDynamic().texture.baseTexture.scaleMode = 0 // nearest neighbor
    }

    private val hurtTexture: PixiTexture
        get() = when (entity.playerType) {
            LorittaPlayer.PlayerType.LORITTA -> textures.lorittaHurtTexture
            LorittaPlayer.PlayerType.PANTUFA -> textures.pantufaHurtTexture
            LorittaPlayer.PlayerType.GABRIELA -> textures.gabrielaHurtTexture
        }

    private val idleTexture: PixiTexture
        get() = when (entity.playerType) {
            LorittaPlayer.PlayerType.LORITTA -> textures.lorittaIdleTexture
            LorittaPlayer.PlayerType.PANTUFA -> textures.pantufaIdleTexture
            LorittaPlayer.PlayerType.GABRIELA -> textures.gabrielaIdleTexture
        }

    private val jumpingTexture: PixiTexture
        get() = when (entity.playerType) {
            LorittaPlayer.PlayerType.LORITTA -> textures.lorittaJumpingTexture
            LorittaPlayer.PlayerType.PANTUFA -> textures.pantufaJumpingTexture
            LorittaPlayer.PlayerType.GABRIELA -> textures.gabrielaJumpingTexture
        }

    private val runningTextures: List<PixiTexture>
        get() = when (entity.playerType) {
            LorittaPlayer.PlayerType.LORITTA -> textures.lorittaRunningTextures
            LorittaPlayer.PlayerType.PANTUFA -> textures.pantufaRunningTextures
            LorittaPlayer.PlayerType.GABRIELA -> textures.gabrielaRunningTextures
        }

    override fun render(isGameLogicUpdate: Boolean, deltaMS: Long, d: Double) {
        if (isGameLogicUpdate) {
            beginningX = targetX
            beginningY = targetY
            targetX = entity.x.toDouble()
            targetY = entity.y.toDouble()
        }

        when (entity.movementState) {
            is PlayerMovementState.FallingState -> changeTexture(hurtTexture)
            is PlayerMovementState.IdleState -> changeTexture(idleTexture)
            is PlayerMovementState.JumpingState -> changeTexture(jumpingTexture)
            is PlayerMovementState.RunningState -> {
                val frameIndex = (((entity.m.elapsedTicks / 10) + animOffset) % 6)

                changeTexture(runningTextures[frameIndex])
            }
            is PlayerMovementState.HurtState -> changeTexture(hurtTexture)
        }

        if (entity.speed > 0) {
            facingRight = true
        } else if (0 > entity.speed) {
            facingRight = false
        }

        // println("Facing right? $facingRight - Current sprite width: ${sprite.width}")

        sprite.scale.x = if (facingRight) {
            0.42666666666
        } else {
            -0.42666666666
        }

        sprite.scale.y = 0.42666666666
        sprite.anchor.x = 0.5
        sprite.anchor.y = 0.5

        val interpolationPercentage = (deltaMS.toDouble() * d)
        // println("DeltaMS: $deltaMS; Interpolation percentage: $interpolationPercentage")

        sprite.x = lerp(beginningX, targetX, interpolationPercentage)
        sprite.y = lerp(beginningY, targetY, interpolationPercentage) - (128 / 2)
        text.x = sprite.x
        text.y = sprite.y - 70
    }

    override fun changeTexture(newTexture: PixiTexture) {
        sprite.texture = newTexture
    }

    override fun destroy() {
        spriteContainer.removeChild(sprite)
        nametagContainer.removeChild(text)
    }
}