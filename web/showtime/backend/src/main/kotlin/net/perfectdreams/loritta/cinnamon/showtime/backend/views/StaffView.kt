package net.perfectdreams.loritta.cinnamon.showtime.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.innerContent
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData

class StaffView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val discordAvatars: Map<Long, String>,
    val aboutMe: Map<Long, String?>
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    companion object {
        private val DiscordEmote = Regex("<(a)?:([a-zA-Z0-9_]+):([0-9]+)>")

        /**
         * List of staff groups and staff members that should be shown in the list
         */
        val staffList = staffList {
            group(I18nKeysData.Website.Staff.LorittaCreator.Title, null) {
                user("MrPowerGamerBR") {
                    socialNetworks.add(DiscordSocialNetwork(123170274651668480))
                    socialNetworks.add(TwitterSocialNetwork("MrPowerGamerBR"))
                    socialNetworks.add(LastFmSocialNetwork("MrPowerGamerBR"))
                    socialNetworks.add(GitHubSocialNetwork("MrPowerGamerBR"))
                    socialNetworks.add(WebsiteSocialNetwork("https://mrpowergamerbr.com/"))
                    socialNetworks.add(YouTubeSocialNetwork("UC-eeXSRZ8cO-i2NZYrWGDnQ"))
                    socialNetworks.add(RedditSocialNetwork("MrPowerGamerBR"))
                }
            }

            group(I18nKeysData.Website.Staff.LorittaBodyguard.Title, I18nKeysData.Website.Staff.LorittaBodyguard.Description) {
                user("Arth") {
                    socialNetworks.add(DiscordSocialNetwork(351760430991147010))
                    socialNetworks.add(TwitterSocialNetwork("souarth"))
                    socialNetworks.add(LastFmSocialNetwork("souarth"))
                }
 
                user("Stéphany") {
                    socialNetworks.add(DiscordSocialNetwork(400683515873591296))
                    socialNetworks.add(TwitterSocialNetwork("dittom_"))
                    socialNetworks.add(GitHubSocialNetwork("dittom20"))
                }
                
                user("Srta. Paum") {
                    socialNetworks.add(DiscordSocialNetwork(197501878399926272))
                    socialNetworks.add(TwitterSocialNetwork("srtabread"))
                }

                user("DanielaGC_") {
                    socialNetworks.add(DiscordSocialNetwork(395788326835322882))
                    socialNetworks.add(TwitterSocialNetwork("DanielaGC_0"))
                    socialNetworks.add(LastFmSocialNetwork("DanielaGC_0"))
                    socialNetworks.add(GitHubSocialNetwork("DanielaGC"))
                }

                user("PeterStark000") {
                    socialNetworks.add(DiscordSocialNetwork(361977144445763585))
                    socialNetworks.add(GitHubSocialNetwork("PeterStark000"))
                    socialNetworks.add(TwitterSocialNetwork("PeterStark000"))
                    socialNetworks.add(YouTubeSocialNetwork("UCcTTEVAyQ_xnfopzewr-5MA"))
                    socialNetworks.add(RedditSocialNetwork("PeterStark000"))
                    socialNetworks.add(LastFmSocialNetwork("PeterStark000"))
                }

                user("hechfx") {
                    socialNetworks.add(DiscordSocialNetwork(236167700777271297L))
                    socialNetworks.add(TwitterSocialNetwork("brviictoor"))
                    socialNetworks.add(GitHubSocialNetwork("hechfx"))
                    socialNetworks.add(LastFmSocialNetwork("brviictoor"))
                }

                user("nathaan") {
                    socialNetworks.add(DiscordSocialNetwork(437731723350900739))
                    socialNetworks.add(TwitterSocialNetwork("oRafa_e"))
                }
                
                user("JvGm45") {
                    socialNetworks.add(DiscordSocialNetwork(197308318119755776))
                    socialNetworks.add(TwitterSocialNetwork("JvGm45"))
                    socialNetworks.add(GitHubSocialNetwork("JvGm45"))
                }

                user("José") {
                    socialNetworks.add(DiscordSocialNetwork(472085605623529496L))
                }
            }

            group(I18nKeysData.Website.Staff.LorittaSupport.Title, I18nKeysData.Website.Staff.LorittaSupport.Description) {
                // No one here... yet :(
            }
        }

        sealed class StaffSocialNetwork
        class DiscordSocialNetwork(
            val userId: Long
        ) : StaffSocialNetwork()
        class TwitterSocialNetwork(
            val handle: String
        ) : StaffSocialNetwork()
        class LastFmSocialNetwork(
            val handle: String
        ) : StaffSocialNetwork()
        class GitHubSocialNetwork(
            val handle: String
        ) : StaffSocialNetwork()
        class WebsiteSocialNetwork(
            val url: String
        ) : StaffSocialNetwork()
        class YouTubeSocialNetwork(
            val handle: String
        ) : StaffSocialNetwork()
        class RedditSocialNetwork(
            val handle: String
        ) : StaffSocialNetwork()

        private val socialNetworkSortOrder = listOf(
            DiscordSocialNetwork::class,
            WebsiteSocialNetwork::class,
            TwitterSocialNetwork::class,
            RedditSocialNetwork::class,
            YouTubeSocialNetwork::class,
            GitHubSocialNetwork::class,
            LastFmSocialNetwork::class,
        )

        private fun staffList(group: StaffListBuilder.() -> (Unit)): StaffListBuilder {
            return StaffListBuilder().apply(group)
        }

        class StaffListBuilder {
            val groups = mutableListOf<GroupBuilder>()

            fun group(name: StringI18nData, description: ListI18nData?, builder: GroupBuilder.() -> (Unit)) {
                groups.add(GroupBuilder(name, description).apply(builder))
            }
        }

        class GroupBuilder(val name: StringI18nData, val description: ListI18nData?) {
            val users = mutableListOf<StaffUserBuilder>()

            fun user(name: String, builder: StaffUserBuilder.() -> (Unit)) {
                users.add(StaffUserBuilder(name).apply(builder))
            }
        }

        class StaffUserBuilder(val name: String) {
            var socialNetworks = mutableListOf<StaffSocialNetwork>()
        }
    }

    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Staff.Title)

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-body") {
                        div {
                            style = "text-align: center;"
                            h1 {
                                + i18nContext.get(I18nKeysData.Website.Staff.Title)
                            }

                            for (text in i18nContext.get(I18nKeysData.Website.Staff.Description)) {
                                p {
                                    + text
                                }
                            }
                        }
                    }
                }
            }

            var index = 0
            for (group in staffList.groups) {
                if (group.users.isNotEmpty()) {
                    div(classes = if (index % 2 == 0) "even-wrapper" else "odd-wrapper") {
                        classes = classes + "wobbly-bg"

                        div(classes = "staff-content") {
                            div {
                                style = "text-align: center;"
                                h2 {
                                    +i18nContext.get(group.name)
                                }

                                if (group.description != null) {
                                    for (text in i18nContext.get(group.description)) {
                                        p {
                                            +text
                                        }
                                    }
                                }
                            }

                            div(classes = "staff-section") {
                                // Sort staff members by their name
                                for (user in group.users.sortedBy { it.name }) {
                                    val discordSocialNetwork = user.socialNetworks.filterIsInstance<DiscordSocialNetwork>()
                                        .firstOrNull()
                                    val discordUserId = discordSocialNetwork?.userId

                                    val discordAvatarUrl = if (discordUserId != null) discordAvatars[discordUserId] else null

                                    val avatarUrl = discordAvatarUrl ?: "/v3/assets/img/emotes/lori-lick.gif"

                                    div(classes = "staff-info") {
                                        img(
                                            src = avatarUrl,
                                            classes = "staff-avatar"
                                        )

                                        div(classes = "staff-title") {
                                            +user.name
                                        }

                                        if (user.socialNetworks.isNotEmpty()) {
                                            div(classes = "staff-social-networks") {
                                                for (socialNetwork in user.socialNetworks.sortedBy { socialNetworkSortOrder.indexOf(it::class) }) {
                                                    when (socialNetwork) {
                                                        is DiscordSocialNetwork -> {
                                                            a(href = "discord://-/users/${socialNetwork.userId}") {
                                                                title = "Discord"

                                                                iconManager.discord.apply(this)
                                                            }
                                                        }
                                                        is WebsiteSocialNetwork -> {
                                                            a(href = socialNetwork.url, target = "_blank") {
                                                                title = i18nContext.get(I18nKeysData.Website.Staff.SocialNetworks.PersonalWebsite)

                                                                iconManager.globe.apply(this)
                                                            }
                                                        }
                                                        is YouTubeSocialNetwork -> {
                                                            a(href = "https://youtube.com/user/${socialNetwork.handle}", target = "_blank") {
                                                                title = "YouTube"

                                                                iconManager.youtube.apply(this)
                                                            }
                                                        }
                                                        is TwitterSocialNetwork -> {
                                                            a(href = "https://twitter.com/${socialNetwork.handle}", target = "_blank") {
                                                                title = "Twitter"

                                                                iconManager.twitter.apply(this)
                                                            }
                                                        }
                                                        is LastFmSocialNetwork -> {
                                                            a(href = "https://last.fm/user/${socialNetwork.handle}", target = "_blank") {
                                                                title = "last.fm"

                                                                iconManager.lastfm.apply(this)
                                                            }
                                                        }
                                                        is GitHubSocialNetwork -> {
                                                            a(href = "https://github.com/${socialNetwork.handle}", target = "_blank") {
                                                                title = "GitHub"

                                                                iconManager.github.apply(this)
                                                            }
                                                        }
                                                        is RedditSocialNetwork -> {
                                                            a(href = "https://www.reddit.com/u/${socialNetwork.handle}", target = "_blank") {
                                                                title = "Reddit"

                                                                iconManager.reddit.apply(this)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        val lastFmSocialNetwork = user.socialNetworks.filterIsInstance<LastFmSocialNetwork>()
                                            .firstOrNull()

                                        if (lastFmSocialNetwork != null) {
                                            val lastFmInfo = showtimeBackend.lastFmStaffData?.get(lastFmSocialNetwork.handle)
                                            val topSongInTheLast7Days = lastFmInfo?.topSongInTheLast7Days
                                            val nowListening = lastFmInfo?.nowListening

                                            if (nowListening != null) {
                                                div(classes = "staff-song") {
                                                    title = i18nContext.get(I18nKeysData.Website.Staff.SocialNetworks.CurrentlyListening)

                                                    iconManager.headphones.apply(this)

                                                    + " ${nowListening.artist} - ${nowListening.name}"
                                                }
                                            }

                                            if (topSongInTheLast7Days != null) {
                                                div(classes = "staff-song") {
                                                    title = i18nContext.get(I18nKeysData.Website.Staff.SocialNetworks.TopSongInTheLast7Days)

                                                    iconManager.music.apply(this)

                                                    + " ${topSongInTheLast7Days.artist} - ${topSongInTheLast7Days.name}"
                                                }
                                            }
                                        }

                                        if (discordSocialNetwork != null) {
                                            val rawAboutMe = aboutMe[discordSocialNetwork.userId]

                                            if (rawAboutMe != null) {
                                                // It says that it is unneeded but everytime that I remove it, it complains, smh
                                                val splitAboutMe = rawAboutMe.split("\n")

                                                div(classes = "staff-description") {
                                                    for (aboutMe in splitAboutMe) {
                                                        // We will split the string into two sections:
                                                        // - String
                                                        // - Discord Emote
                                                        val sections = mutableListOf<AboutMeSection>()
                                                        val matches = DiscordEmote.findAll(aboutMe).toList()
                                                        var firstMatchedCharacterIndex = 0
                                                        var lastMatchedCharacterIndex = 0

                                                        for (match in matches) {
                                                            sections.add(AboutMeText(aboutMe.substring(firstMatchedCharacterIndex until match.range.first)))
                                                            val animated = match.groupValues[1] == "a"
                                                            val emoteName = match.groupValues[2]
                                                            val emoteId = match.groupValues[3]
                                                            // TODO: Fix this! Because we migrated the emotes to Ethereal, this won't work correctly anymore
                                                            if (showtimeBackend.webEmotes.emotes.contains(emoteName)) {
                                                                sections.add(AboutMeLorittaWebsiteEmote(emoteName))
                                                            } else {
                                                                sections.add(AboutMeDiscordEmote(emoteId, animated))
                                                            }

                                                            lastMatchedCharacterIndex = match.range.last + 1
                                                            firstMatchedCharacterIndex = lastMatchedCharacterIndex
                                                        }

                                                        sections.add(
                                                            AboutMeText(
                                                                aboutMe.substring(
                                                                    lastMatchedCharacterIndex until aboutMe.length
                                                                )
                                                            )
                                                        )

                                                        for (section in sections) {
                                                            when (section) {
                                                                is AboutMeLorittaWebsiteEmote -> {
                                                                    val etherealGambiEmote = showtimeBackend.webEmotes.emotes[section.emoteFile]!!

                                                                    imgSrcSetFromEtherealGambi(
                                                                        showtimeBackend,
                                                                        etherealGambiEmote,
                                                                        "png",
                                                                        "1.5em"
                                                                    ) {
                                                                        classes = setOf("inline-emoji")
                                                                    }
                                                                }
                                                                is AboutMeDiscordEmote -> {
                                                                    img(src = "https://cdn.discordapp.com/emojis/${section.emoteId}.${if (section.animated) "gif" else "webp"}?size=48&quality=lossless", classes = "inline-emoji")
                                                                }
                                                                is AboutMeText -> +section.text
                                                            }
                                                        }

                                                        br {}
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    index++
                }
            }
        }
    }

    sealed class AboutMeSection
    data class AboutMeText(val text: String) : AboutMeSection()
    data class AboutMeLorittaWebsiteEmote(val emoteFile: String) : AboutMeSection()
    data class AboutMeDiscordEmote(val emoteId: String, val animated: Boolean) : AboutMeSection()
}
