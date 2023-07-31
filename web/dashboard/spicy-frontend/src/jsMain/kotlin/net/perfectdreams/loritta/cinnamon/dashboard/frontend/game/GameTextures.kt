package net.perfectdreams.loritta.cinnamon.dashboard.frontend.game

import kotlinx.browser.window
import net.perfectdreams.loritta.cinnamon.dashboard.utils.pixi.Texture

class GameTextures {
    val lorittaHurtTexture = Texture.from("${window.location.origin}/assets/img/lori-sprites/dano.png")
    val lorittaIdleTexture = Texture.from("${window.location.origin}/assets/img/lori-sprites/repouso.png")
    val lorittaJumpingTexture = Texture.from("${window.location.origin}/assets/img/lori-sprites/pulo2.png")
    val lorittaRunningTextures = listOf(
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida1.png"),
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida2.png"),
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida3.png"),
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida4.png"),
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida5.png"),
        Texture.from("${window.location.origin}/assets/img/lori-sprites/corrida6.png")
    )

    val pantufaHurtTexture = Texture.from("${window.location.origin}/assets/img/pantufa-sprites/dano.png")
    val pantufaIdleTexture = Texture.from("${window.location.origin}/assets/img/pantufa-sprites/repouso.png")
    val pantufaJumpingTexture = Texture.from("${window.location.origin}/assets/img/pantufa-sprites/pulo2.png")
    val pantufaRunningTextures = listOf(
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida1.png"),
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida2.png"),
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida3.png"),
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida4.png"),
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida5.png"),
        Texture.from("${window.location.origin}/assets/img/pantufa-sprites/corrida6.png")
    )

    val gabrielaHurtTexture = Texture.from("${window.location.origin}/assets/img/gabriela-sprites/dano.png")
    val gabrielaIdleTexture = Texture.from("${window.location.origin}/assets/img/gabriela-sprites/repouso.png")
    val gabrielaJumpingTexture = Texture.from("${window.location.origin}/assets/img/gabriela-sprites/pulo2.png")
    val gabrielaRunningTextures = listOf(
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida1.png"),
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida2.png"),
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida3.png"),
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida4.png"),
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida5.png"),
        Texture.from("${window.location.origin}/assets/img/gabriela-sprites/corrida6.png")
    )
}