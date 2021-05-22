package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.unsafe
import net.perfectdreams.loritta.sweetmorenitta.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.sweetmorenitta.utils.adWrapper
import net.perfectdreams.loritta.sweetmorenitta.utils.generateNitroPayAdOrSponsor
import net.perfectdreams.loritta.sweetmorenitta.utils.generateNitroPayVideoAd

class DailyView(
    locale: BaseLocale,
    path: String
) : NavbarView(
    locale,
    path
) {
    override fun getTitle() = "Daily"

    override fun DIV.generateContent() {
        style {
            unsafe {
                raw("""
    .scene {
    width: 100px;
    height: 100px;
    margin: 75px;
    perspective: 1000px;
    perspective-origin: 50% -400%;
    transition: 1s;
    filter: drop-shadow(12px 12px 25px rgba(0,0,0,0.5));
    margin-left: auto;
    margin-right: auto;
    }

    .scene:hover {
        transform: scale(1.1);
    transition: 1s;
    }

    .cube {
    width: 100px;
    height: 100px;
    position: relative;
    transform-style: preserve-3d;
    transform: translateZ(-100px);
    transition: transform 1s;
    animation-name: rotate-cube;
    animation-duration: 5s;
    animation-iteration-count: infinite;
    animation-timing-function: linear;
    }

    .cube img {
        max-width: 100px;
        max-height: 100px;
    }

    .cube__face {
    position: absolute;
    width: 100px;
    height: 100px;
    // border: 2px solid black;
    font-size: 40px;
    font-weight: bold;
    color: white;
    text-align: center;
    }

    .cube__face--front  { background: hsla(  0, 100%, 50%, 0.7); }
    .cube__face--right  { background: hsla( 60, 100%, 50%, 0.7); }
    .cube__face--back   { background: hsla(120, 100%, 50%, 0.7); }
    .cube__face--left   { background: hsla(180, 100%, 50%, 0.7); }
    .cube__face--top    { background: hsla(240, 100%, 50%, 0.7); }
    .cube__face--bottom { background: hsla(300, 100%, 50%, 0.7); }

    .cube__face--front  { transform: rotateY(  0deg) translateZ(50px); }
    .cube__face--right  { transform: rotateY( 90deg) translateZ(50px); }
    .cube__face--back   { transform: rotateY(180deg) translateZ(50px); }
    .cube__face--left   { transform: rotateY(-90deg) translateZ(50px); }
    .cube__face--top    { transform: rotateX( 90deg) translateZ(50px); }
    .cube__face--lace1 { transform: rotateX( 0deg) rotateY(45deg) translateY(-50px) translateZ(0px); }
    .cube__face--lace2 { transform: rotateX( 0deg) rotateY(-45deg) translateY(-50px) translateZ(0px); }
    .cube__face--bottom { transform: rotateX(-90deg) translateZ(50px); }

    label { margin-right: 10px; }

    /* The animation code */
    @keyframes rotate-cube {
    0% {transform: rotateY(0deg); }
    50% {transform: rotateY(180deg); }
    100% {transform: rotateY(360deg); }
    }
    """)
            }
        }
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            adWrapper {
                generateNitroPayAdOrSponsor(
                    0,
                    "daily-top1",
                    NitroPayAdGenerator.ALL_SIZES
                )
                generateNitroPayAdOrSponsor(
                    1,
                    "daily-top2",
                    NitroPayAdGenerator.ALL_SIZES_EXCEPT_PHONES
                )
            }

            div(classes = "media") {
                div(classes = "media-body") {
                    p {
                        style = "color: #f04747;"
                        b {
                            + "Atenção: "
                        }
                        + "Compra e venda de sonhos com terceiros não são responsabilidade da equipe da Loritta! Se você for roubado (scamming), nós não iremos recuperar seus sonhos e se você reclamar com a equipe da Loritta você será banido, pois resolver problemas de roubos fora da Loritta não é nosso trabalho. Se você comprar/vender sonhos ou outros produtos com valores monetários (por exemplo: Discord Nitro) com terceiros, você e o seu comprador/vendedor correm risco de serem banidos caso sejam denunciados para a equipe. Muitos \"vendedores\" usam cartões clonados para vender tais produtos, logo você está correndo risco de estar cometendo um ato ilegal ao comprar com terceiros. Dar sonhos como recompensa por entrarem em um servidor também é proibido e é contra os termos de uso do Discord."
                        + " "
                        + "Leia mais sobre as regras da Loritta "
                        a(href = "/guidelines") {
                            + "clicando aqui"
                        }
                        + "."
                    }

                    p {
                        style = "color: #f04747;"
                        + "Se você deseja comprar sonhos de uma forma segura que ainda por cima ajuda a Loritta ficar online, visite a nossa loja de sonhos "
                        a(href = "/user/@me/dashboard/bundles") {
                            + "clicando aqui"
                        }
                        + "!"
                    }
                }
            }

            div(classes = "media") {
                div(classes = "media-body") {
                    h1 {
                        + "Prêmio Diário"
                    }

                    div {
                        id = "daily-prewrapper"

                        div {
                            id = "daily-wrapper"

                            div(classes = "scene") {
                                div(classes = "cube") {
                                    div(classes = "cube__face cube__face--front") {
                                        style = "width: 100%; height: 100%;"

                                        img(src = "/assets/img/daily/present_side.png") {}
                                    }
                                    div(classes = "cube__face cube__face--back") {
                                        style = "width: 100%; height: 100%;"

                                        img(src = "/assets/img/daily/present_side.png") {}
                                    }
                                    div(classes = "cube__face cube__face--right") {
                                        style = "width: 100%; height: 100%;"

                                        img(src = "/assets/img/daily/present_side.png") {}
                                    }
                                    div(classes = "cube__face cube__face--left") {
                                        style = "width: 100%; height: 100%;"

                                        img(src = "/assets/img/daily/present_side.png") {}
                                    }
                                    div(classes = "cube__face cube__face--top") {
                                        style = "width: 100%; height: 100%;"

                                        img(src = "/assets/img/daily/present_top.png") {}
                                    }
                                    div(classes = "cube__face cube__face--lace1") {
                                        style = "width: 100%; height: 25%"

                                        img(src = "/assets/img/daily/present_lace.png") {}
                                    }
                                    div(classes = "cube__face cube__face--lace2") {
                                        style = "width: 100%; height: 25%"

                                        img(src = "/assets/img/daily/present_lace.png") {}
                                    }
                                }
                            }

                            p {
                                + "Pegue o seu prêmio diário para conseguir sonhos!"
                            }

                            div {
                                id = "daily-captcha"
                                style = "display: inline-block;"
                            }

                            div {
                                div(classes = "button-discord pure-button daily-reward-button button-discord-disabled") {
                                    style = "font-size: 1.25em; transition: 0.3s;"

                                    i(classes = "fas fa-gift") {}

                                    + " Pegar Prêmio"
                                }
                            }
                        }

                        div(classes = "daily-notification flavourText") {
                            style = "color: #f04747;"
                        }
                    }
                }
            }

            adWrapper {
                generateNitroPayAdOrSponsor(
                    2,
                    "daily-bottom1",
                    NitroPayAdGenerator.ALL_SIZES
                )
                generateNitroPayAdOrSponsor(
                    3,
                    "daily-bottom2",
                    NitroPayAdGenerator.ALL_SIZES_EXCEPT_PHONES
                )
            }

            generateNitroPayVideoAd("daily-bottom3-video")
        }

        div(classes = "even-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-body") {
                    h1 {
                        + "Mas... o que são sonhos?"
                    }

                    p {
                        + "Sonhos são a moeda que você pode utilizar na Loritta. Você pode usar sonhos para apostar na Lorifa, comprar novos designs para o perfil, casar, comprar raspadinhas e muito mais!"
                    }
                    p {
                        + "Para apostar na rifa, use `+rifa`!"
                    }
                    p {
                        + "Você pode casar com a pessoa que você tanto ama com `+casar`!"
                    }
                    p {
                        + "Envie sonhos para seus amigos e amigas com `+pay`!"
                    }
                    p {
                        + "Você pode comprar novos designs para o seu perfil no `+profile shop`!"
                    }
                }
                div(classes = "media-figure") {
                    img(src = "https://loritta.website/assets/img/loritta_money_discord.png") {}
                }
            }
        }

        script {
            unsafe {
                raw("""
    function recaptchaCallback(response) {
        this['spicy-morenitta'].net.perfectdreams.spicymorenitta.routes.DailyRoute.Companion.recaptchaCallback(response)
    }""")
            }
        }
    }
}