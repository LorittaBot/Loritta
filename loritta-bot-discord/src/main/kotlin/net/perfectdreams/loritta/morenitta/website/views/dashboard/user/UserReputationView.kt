package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.views.BaseView

class UserReputationView(
    val loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    val userIdentification: LorittaJsonWebSession.UserIdentification?,
    val user: User?,
    val lastReputationGiven: Reputation?,
    val reputations: List<Reputation>,
    val guildId: Long?,
    val channelId: Long?,
    val backgroundUrl: String
) : BaseView(
    i18nContext,
    locale,
    path
) {
    override fun getTitle() = "Reputações para ${user?.name}"

    override fun HTML.generateBody() {
        user!!
        body {
            div {
                id = "loading-screen"
                img(src = "https://loritta.website/assets/img/loritta_loading.png", alt = "Loading Spinner")
                div(classes = "loading-text") {
                    +"Carregando..."
                }
            }

            script {
                unsafe {
                    raw("""
    function recaptchaCallback(response) {
        this['spicy-morenitta'].net.perfectdreams.spicymorenitta.routes.ReputationRoute.Companion.recaptchaCallback(response)
    }""")
                }
            }

            style {
                unsafe {
                    raw("""
    .centralize-on-screen {
        position: fixed;
        top: 50%;
        left: 50%;
        margin-right: -50%;
        transform: translate(-50%, -50%);
    } 

    .fake-modal-box {
        background-color: #35383C;
        color: white;
        border-radius: 7px;
        box-shadow: 0px 0px 20px #00000082;
        overflow: hidden;
        max-width: 800px;
        width: 100%;
        overflow-y: auto;
        max-height: 100vh;
    }

    .fake-modal-box .modal-title {
        background-color: #7289DA;
        display: flex;
        height: 140px;
        padding: 24px;
        box-sizing: border-box;
        align-items: center;
    }

    .fake-modal-box .information-box {
        background-color: #494B4F;
        padding: 5px;
        text-align: center;
    }

    .fake-modal-box .modal-title .modal-image-wrapper {
        border-radius: 999px;
        border: 6px solid;
        border-color: #ffffff4d;
        width: 80px;
        height: 80px;
        margin-left: 16px;
    }

    .fake-modal-box .modal-title .modal-header-info {
        flex: 1;
        overflow: hidden;
    }

    .fake-modal-box .modal-title .modal-header-info .modal-primary {
        font-size: 48px;
        font-family: Lato;
        font-weight: 700;
    }

    .fake-modal-box .modal-title .modal-header-info .modal-secondary {
        font-size: 16px;
        text-transform: uppercase;
        color: hsla(0,0%,100%,.4);
        font-family: Lato;
        font-weight: 600;
    }

    .fake-modal-box .box-content {
        padding: 24px;
    }

    .fake-modal-box .box-content .reputation-wrapper {
        display: flex;
        box-sizing: border-box;
        align-items: center;
    }

    @media only screen and (max-width: 700px) {
    .reputation-wrapper {
        flex-flow: column;
    }
    }

    .fake-modal-box .rank-avatar {
        width: 48px;
        border-radius: 999px;
        
    }

    .fake-modal-box .rank-name {
        font-size: 20px;
        font-weight: 600;
        color: white;
        width: 240px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .fake-modal-box .reputations-received {
        font-weight: 600;
        color: hsla(0,0%,100%,.4);
    }

    .fake-modal-box .rank-position {
        font-size: 24px;
        padding: 6px;
    }

    .fake-modal-box .rank-title {
        font-size: 2em;
        font-weight: 600;
        text-align: center;
    }

    .fake-modal-box .box-item {
        padding: 4px;
    }


    .fake-modal-box table {
        border-collapse: separate;
        border-spacing: 0 4px;
    }

    body {
        background-image: url("$backgroundUrl");
        background-size: cover;
    }


    .rainbow {
        /* Chrome, Safari, Opera */
    -webkit-animation: rainbow 3s infinite; 
    
    /* Internet Explorer */
    -ms-animation: rainbow 3s infinite;
    
    /* Standar Syntax */
    animation: rainbow 3s infinite; 
    }
    
    /* Chrome, Safari, Opera */
    @-webkit-keyframes rainbow{
    0%{color: orange;}	
    10%{color: purple;}	
        20%{color: red;}
        40%{color: yellow;}
        60%{color: green;}
        100%{color: blue;}
        100%{color: orange;}	
    }
    /* Internet Explorer */
    @-ms-keyframes rainbow{
    0%{color: orange;}	
    10%{color: purple;}	
        20%{color: red;}
        40%{color: yellow;}
        60%{color: green;}
        100%{color: blue;}
        100%{color: orange;}	
    }
    
    /* Standar Syntax */
    @keyframes rainbow{
    0%{color: orange;}	
    10%{color: purple;}	
        20%{color: red;}
        40%{color: yellow;}
        60%{color: green;}
        100%{color: blue;}
        100%{color: orange;}
    }
                    """)
                }
            }

            val diff = System.currentTimeMillis() - (lastReputationGiven?.receivedAt ?: 0)
            val nextReputationCanBeGivenAt = (lastReputationGiven?.receivedAt ?: 0) + 3_600_000
            val canGiveReputation = diff > 3_600_000

            div(classes = "centralize-on-screen fake-modal-box dark") {
                div(classes = "modal-title") {
                    div(classes = "modal-header-info") {
                        div(classes = "modal-secondary") { +"Reputações" }
                        div(classes = "modal-primary") { +user.name }
                    }
                    div {
                        img(classes = "modal-image-wrapper", src = user.effectiveAvatarUrl)
                    }
                }

                div(classes = "box-content") {
                    div(classes = "reputation-wrapper") {
                        div(classes = "box-item") {
                            p {
                                +"Reputações servem para você agradecer outro usuário por algo que ele fez. ${user.name} te ajudou em algo? ${user.name} contou uma piada e você caiu no chão de tanto rir? Então dê uma reputação para agradecer!"
                            }
                            p {
                                +"Atualmente ${user.name} possui "
                                span(classes = "reputation-count") {
                                    +"${reputations.size}"
                                }
                                +" reputações!"
                            }
                            hr {}
                            div("flavourText") {
                                +"Por que você está dando esta reputação?"
                            }
                            textArea {
                                id = "reputation-reason"
                            }
                            div {
                                if (userIdentification == null) {
                                    var redirectUrl = loritta.config.loritta.website.url + "user/${user.id}/rep"
                                    if (channelId != null)
                                        redirectUrl += "?guild=$guildId&channel=$channelId"

                                    a(classes = "button-discord button-discord-info pure-button g-recaptcha reputation-button", href = LorittaDiscordOAuth2AuthorizeScopeURL(loritta, redirectUrl).toString()) {
                                        id = "reputation-button"
                                        attributes["data-need-login"] = "true"

                                        +"Dar reputação!"
                                    }
                                } else {
                                    if (canGiveReputation) {
                                        div {
                                            id = "reputation-captcha"
                                            style = "display: inline-block;"
                                        }

                                        button(classes = "button-discord button-discord-disabled pure-button g-recaptcha reputation-button") {
                                            id = "reputation-button"
                                            attributes["data-callback"] = "giveReputation"

                                            +"Dar reputação!"
                                        }
                                    } else {
                                        button(classes = "button-discord button-discord-disabled pure-button") {
                                            id = "reputation-button"
                                            attributes["data-can-give-at"] = nextReputationCanBeGivenAt.toString()

                                            +"Dar reputação!"
                                        }
                                    }
                                }
                            }
                        }
                        div(classes = "box-item leaderboard") {}
                    }
                }

                unsafe {
                    raw("""
    <!-- Loritta Reputation Page -->
    <ins class="adsbygoogle"
        style="display:block"
        data-ad-client="ca-pub-9989170954243288"
        data-ad-slot="6685706043"
        data-ad-format="auto"
        data-full-width-responsive="true"></ins>
    """)
                }

            }
        }
    }

    override fun HEAD.generateMeta() {
        meta("theme-color", "#00c1df")
        meta(content = locale["website.lorittaWebsite"]) { attributes["property"] = "og:site_name" }
        meta(content = "Reputações servem para você agradecer outro usuário por algo que ele fez. ${user?.name} te ajudou em algo? ${user?.name} contou uma piada e você caiu no chão de tanto rir? Então dê uma reputação para agradecer! Atualmente ${user?.name} possui ${reputations.size} reputações!") { attributes["property"] = "og:description" }
        meta(content = getTitle()) { attributes["property"] = "og:title" }
        meta(content = "600") { attributes["property"] = "og:ttl" }
        meta(content = user?.effectiveAvatarUrl) { attributes["property"] = "og:image"}
    }
}