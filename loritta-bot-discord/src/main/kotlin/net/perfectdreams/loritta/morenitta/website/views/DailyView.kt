package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.DailyTaxThresholds
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.sweetmorenitta.utils.adWrapper
import net.perfectdreams.loritta.morenitta.sweetmorenitta.utils.generateAdOrSponsor
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils

class DailyView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String
) : NavbarView(
    loritta,
    i18nContext,
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

            div(classes = "media") {
                div(classes = "media-body") {
                    div(classes = "daily-attention") {
                        style = "color: #f04747;"

                        p {
                            div(classes = "daily-attention-title") {
                                +i18nContext.get(I18nKeysData.Daily.Attention.Title)
                            }
                            div {
                                +i18nContext.get(I18nKeysData.Daily.Attention.BuyingAndSellingIsWrong)
                            }
                            div {
                                i {
                                    +i18nContext.get(I18nKeysData.Daily.Attention.IllegalStuff)
                                }
                            }
                        }

                        p {
                            +i18nContext.get(I18nKeysData.Daily.Attention.DontUseVpnsAndProxies)
                        }

                        p {
                            WebsiteUtils.buildAsHtml(
                                i18nContext.language.textBundle.strings[I18nKeys.Daily.Attention.ReadOurRules.key]!!,
                                {
                                    a(href = "/guidelines") {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.Rules)
                                    }
                                },
                                {
                                    +it
                                }
                            )
                        }

                        p {
                            WebsiteUtils.buildAsHtml(
                                i18nContext.language.textBundle.strings[I18nKeys.Daily.Attention.BuySonhos.key]!!,
                                {
                                    a(href = "/dashboard/sonhos-shop") {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.ClickHere)
                                    }
                                },
                                {
                                    +it
                                }
                            )
                        }
                    }
                }
            }

            adWrapper {
                generateAdOrSponsor(
                    loritta,
                    0,
                    "2235080437",
                    adsenseAdClass = "responsive-adsense-ad"
                )
            }

            div(classes = "media") {
                div(classes = "media-body") {
                    h2 {
                        style = "color: red;"

                        +i18nContext.get(I18nKeysData.Daily.Attention.ScamWarningTitle)
                    }

                    div {
                        style = "text-align: center;"

                        img(src = "https://stuff.loritta.website/loritta-fake-scam.png")
                    }

                    p {
                        WebsiteUtils.buildAsHtml(
                            i18nContext.language.textBundle.strings[I18nKeys.Daily.Attention.ScamWarningText.key]!!,
                            {
                                if (it == "yourSonhos") {
                                    b {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.YourSonhos)
                                    }
                                } else if (it == "yourDiscordAccount") {
                                    b {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.YourDiscordAccount)
                                    }
                                }
                            },
                            {
                                +it
                            }
                        )
                    }

                    p {
                        WebsiteUtils.buildAsHtml(
                            i18nContext.language.textBundle.strings[I18nKeys.Daily.Attention.ScamWarningDontTrust.key]!!,
                            {
                                if (it == "dontTrust") {
                                    b {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.DontTrust)
                                    }
                                } else if (it == "fake") {
                                    b {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.Fake)
                                    }
                                } else if (it == "followExactlyWhatItSays") {
                                    b {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.FollowExactlyWhatItSays)
                                    }
                                }
                            },
                            {
                                +it
                            }
                        )
                    }

                    p {
                        WebsiteUtils.buildAsHtml(
                            i18nContext.language.textBundle.strings[I18nKeys.Daily.Attention.ScamWarningVerify.key]!!,
                            {
                                if (it == "ourSupportServer") {
                                    a(href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/support") {
                                        +i18nContext.get(I18nKeysData.Daily.Attention.OurSupportServer)
                                    }
                                }
                            },
                            {
                                +it
                            }
                        )
                    }
                }
            }

            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        id = "daily-compose-wrapper"
                    }
                }
            }

            adWrapper {
                generateAdOrSponsor(
                    loritta,
                    1,
                    "3808844539",
                    adsenseAdClass = "responsive-adsense-ad"
                )
            }

            // generateNitroPayVideoAd("daily-bottom3-video")

            div(classes = "media") {
                div(classes = "media-body") {
                    style = "text-align: left;"

                    div(classes = "title-with-emoji") {
                        img(src = "https://stuff.loritta.website/emotes/lori-zap.png", classes = "emoji-title")

                        h2 {
                            + "Perguntas Frequentes"
                        }
                    }

                    div {
                        style = "display: flex; gap: 1em; flex-direction: column; padding-bottom: 2em;"

                        fun DIV.fancyDetails(title: String, builder: DIV.() -> Unit) {
                            details(classes = "fancy-details") {
                                style = "line-height: 1.2; position: relative;"

                                summary {
                                    div {
                                        style = "display: flex;align-items: center;"
                                        div {
                                            style = "flex-grow: 1; align-items: center;"

                                            +title
                                        }
                                    }

                                    div(classes = "chevron-icon") {
                                        unsafe {
                                            raw("""<svg viewBox="0 0 448 512"><path d="M207.029 381.476L12.686 187.132c-9.373-9.373-9.373-24.569 0-33.941l22.667-22.667c9.357-9.357 24.522-9.375 33.901-.04L224 284.505l154.745-154.021c9.379-9.335 24.544-9.317 33.901.04l22.667 22.667c9.373 9.373 9.373 24.569 0 33.941L240.971 381.476c-9.373 9.372-24.569 9.372-33.942 0z" fill="currentColor"></path></svg>""")
                                        }
                                    }
                                }

                                div(classes = "details-content") {
                                    builder.invoke(this)
                                }
                            }
                        }

                        fancyDetails("Como consigo sonhos?") {
                            ul {
                                li { +"Pegando o Daily, nessa página" }
                                li { +"Ligando para o Bom Dia & Cia" }
                                li { +"Recebendo +pay de outros usuários" }
                                li { +"Ganhando rifas, no +lorifa" }
                                li { +"Apostando no +raspadinhas" }
                                li { +"Trocando por Garticos, moeda do GarticBot" }
                                li { +"Transferindo do SparklyPower (Servidor de Minecraft da DreamLand)" }
                                li { +"Votando na Loritta no top.gg (+dbl)" }
                                li { +"Ganhando apostas de +emojifight, +coinflip bet ou +guessnumber" }
                                li { +"Lucrando em ações no +corretora" }
                                li { +"E muito mais!" }
                            }
                        }

                        fancyDetails("Como gasto sonhos?") {
                            ul {
                                li { +"Você pode se casar com o +marry" }
                                li { +"Fazer ligações no Bom Dia & Cia" }
                                li { +"Apostando na rifa da Loritta, +lorifa" }
                                li { +"Fazendo doações com o +pay" }
                                li { +"Comprando layouts e backgrounds na loja diária" }
                                li { +"Mudando a % do seu ship com alguém" }
                                li { +"Transferindo para o SparklyPower (Servidor de Minecraft da DreamLand)" }
                                li { +"Perdendo qualquer aposta em +emojifight, +coinflip bet e etc." }
                            }
                        }

                        fancyDetails("Taxas de Inatividade") {
                            ul {
                                for (threshold in DailyTaxThresholds.THRESHOLDS) {
                                    li {
                                        + "Se você possui mais de ${threshold.minimumSonhosForTrigger} sonhos, você irá perder ${threshold.tax * 100}% dos seus sonhos após ficar ${threshold.maxDayThreshold} dias sem pegar a recompensa diária"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
