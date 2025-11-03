package net.perfectdreams.spicymorenitta.game.render

import org.w3c.dom.Image

interface LorittaPlayerTextures {
    val hurtTexture: Image
    val idleTexture: Image
    val jumpingTexture: Image
    val runningTextures: List<Image>

    class LorittaTextures : LorittaPlayerTextures {
        override val hurtTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/dano.png"
        }

        override val idleTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/repouso.png"
        }

        override val jumpingTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/pulo2.png"
        }

        override val runningTextures = listOf(
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida1.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida2.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida3.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida4.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida5.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/lori-sprites/corrida6.png"
            }
        )
    }

    class PantufaTextures : LorittaPlayerTextures {
        override val hurtTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/dano.png"
        }

        override val idleTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/repouso.png"
        }

        override val jumpingTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/pulo2.png"
        }

        override val runningTextures = listOf(
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida1.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida2.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida3.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida4.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida5.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/pantufa-sprites/corrida6.png"
            }
        )
    }

    class GabrielaTextures : LorittaPlayerTextures {
        override val hurtTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/dano.png"
        }

        override val idleTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/repouso.png"
        }

        override val jumpingTexture = Image().apply {
            src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/pulo2.png"
        }

        override val runningTextures = listOf(
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida1.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida2.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida3.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida4.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida5.png"
            },
            Image().apply {
                src = "https://stuff.loritta.website/pocket-loritta/gabriela-sprites/corrida6.png"
            }
        )
    }
}