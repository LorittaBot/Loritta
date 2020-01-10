import LoriDashboard.loadingScreen
import utils.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Json
import kotlin.math.floor

object LoriServerList {
	fun start() {
		println("LoriServerList - owo")

		var backgroundY = 0

		window.setInterval({
			jq(".serverListHeader").css(
					"background-position-y",
					backgroundY
			)
			backgroundY -= 2
		}, 75)

		val selfProfileDiv = document.getElementById("self-profile-json")!!.innerHTML
		println(selfProfileDiv)
		val serverSamplesDiv = document.getElementById("server-samples-json")!!.innerHTML
		println(serverSamplesDiv)

		val data = JSON.parse<Json>(selfProfileDiv)

		if (data.toJson().get("api:code") != LoriWebCodes.UNAUTHORIZED)
			selfProfile = data.toJson<LorittaProfile>()

		val pathName = window.location.pathname
		val args = pathName.split("/")
		// arg0 = empty
		// arg1 = servers
		// arg2 = list
		// arg3 = page
		val arg0 = args.getOrNull(0)
		val arg1 = args.getOrNull(1)
		val arg2 = args.getOrNull(2)
		val arg3 = args.getOrNull(3)
		val arg4 = args.getOrNull(4)

		if (arg3 == "page" && arg4 != null) {
			var skip = (arg4.toIntOrNull() ?: 1) - 1
			skip *= 50
			showTopRankServers(50, skip)
		} else if (arg3 == "bumped" && arg4 != null) {
			var skip = (arg4.toIntOrNull() ?: 1) - 1
			skip *= 50
			showRecentlyBumpedRankServers(50, skip)
		} else {
			println("owo payload: " + serverSamplesDiv)
			val payload = JSON.parse<LorittaSamplePayload>(serverSamplesDiv)

			println("Sponsored Count: " + payload.sponsoredCount)
			println("Partners Count: " + payload.partnersCount)
			println("Total Count: " + payload.totalCount)

			val sponsorSampleDiv = jq(".sponsored-servers-sample")
			val partnerSampleDiv = jq(".partners-servers-sample")
			val allServersSampleDiv = jq(".all-servers-sample")
			val randomServersSampleDiv = jq(".random-servers-sample")
			val recentlyBumpedServersSampleDiv = jq(".recently-bumped-servers-sample")

			for (serverSample in payload.sponsored) {
				addServerSample(serverSample, sponsorSampleDiv, "pure-u-1 pure-u-md-1-2")
			}

			for (serverSample in payload.partners) {
				addServerSample(serverSample, partnerSampleDiv, "pure-u-1 pure-u-md-1-2")
			}

			for ((index, serverSample) in payload.top.withIndex()) {
				if (index % 5 == 0) {
					addAdvertisement(allServersSampleDiv)
					injectAdvertisements(allServersSampleDiv)
				}
				addServerSample(serverSample, allServersSampleDiv)
			}

			for ((index, serverSample) in payload.recentlyBumped.withIndex()) {
				if (index % 5 == 0) {
					addAdvertisement(recentlyBumpedServersSampleDiv)
					injectAdvertisements(recentlyBumpedServersSampleDiv)
				}
				addServerSample(serverSample, recentlyBumpedServersSampleDiv)
			}

			for (serverSample in payload.random) {
				addServerSample(serverSample, randomServersSampleDiv, "pure-u-1 pure-u-md-1-2")
			}

			jq(".view-more-servers").click {
				showTopRankServers(50, 0)
			}

			jq(".view-more-recently-bumped-servers").click {
				showRecentlyBumpedRankServers(50, 0)
			}
		}
	}

	fun showTopRankServers(size: Int, skip: Int) {
		jq("#rank-list-sample-buttons").remove()
		jq("#listWrapper").empty()
		jq("#rank-list-sample-buttons").empty()
		jq("#listWrapper").html(
				"""
					<div class="oddWrapper">
<div class="contentWrapper" style="text-align: center; width: 80%;">
<div class="sectionHeader">
<i class="far fa-heart"></i> owo whats this???
</div>
<div class="rank-list-sample pure-g"></div>
</div>
</div>"""
		)
		showLoadingBar("Carregando...")
		var page = floor((skip / 50).toDouble()) + 1
		window.history.pushState(null, "owo whats this", "/servers/page/$page")

		jQuery.post("${loriUrl}api/v1/server-list/get-servers?size=$size&skip=$skip&serverType=top", { data, b, c ->
			println("Received data: ${data.stringify()}")
			val payload = data.toJson<LorittaServerQueryPayload>()
			for (serverSample in payload.result) {
				addServerSample(serverSample, jq(".rank-list-sample"), "pure-u-1 pure-u-md-1-2")
			}
			jq(".rank-list-sample").after(jq("<div>")
					.attr("id", "rank-list-sample-buttons")
					.css("text-align", "center")
			)
			if (page != 1.0) {
				println("diff page!")
				jq("#rank-list-sample-buttons").append(
						jq("<button>")
								.attr("class", "button-discord button-discord-info pure-button")
								.html("<i class=\"fas fa-arrow-left\"></i>")
				).click {
					showTopRankServers(size, skip - 50)
				}
			}
			if (payload.totalCount.unsafeCast<Number>().toInt() > (skip + size)) {
				jq("#rank-list-sample-buttons").append(
						jq("<button>")
								.attr("class", "button-discord button-discord-info pure-button")
								.html("<i class=\"fas fa-arrow-right\"></i>")
				).click {
					showTopRankServers(size, skip + 50)
				}
			}
			window.scroll(0.0, 0.0)
			hideLoadingBar()
		})
	}

	fun showRecentlyBumpedRankServers(size: Int, skip: Int) {
		jq("#listWrapper").empty()
		jq("#listWrapper").html(
				"""
					<div class="oddWrapper">
<div class="contentWrapper" style="text-align: center; width: 80%;">
<div class="sectionHeader">
<i class="far fa-heart"></i> owo whats this???
</div>
<div class="rank-list-sample pure-g"></div>
</div>
</div>"""
		)
		showLoadingBar("Carregando...")
		var page = floor((skip / 50).toDouble()) + 1
		window.history.pushState(null, "owo whats this", "/servers/bumped/$page")

		jQuery.post("${loriUrl}api/v1/server-list/get-servers?size=$size&skip=$skip&serverType=recentlyBumped", { data, b, c ->
			val payload = data.toJson<LorittaServerQueryPayload>()
			for (serverSample in payload.result) {
				addServerSample(serverSample, jq(".rank-list-sample"), "pure-u-1 pure-u-md-1-2")
			}
			window.scroll(0.0, 0.0)
			hideLoadingBar()
		})
	}

	fun addServerSample(serverSample: LorittaServerSample, div: JQuery, clazz: String? = null) {
		val serverSampleTemplate = if (clazz != null) {
			jq("#server-sample-template").clone().attr("id", null)
					.attr("class", clazz)
		} else {
			jq("#server-sample-template").children()
		}

		val template = serverSampleTemplate.clone()

		template.find(".server-tagline").text(serverSample.serverListConfig.tagline ?: ":shrug:")

		var tagline = template.find(".server-tagline").text()
		serverSample.serverEmotes.forEach {
			tagline = tagline.replace(":${it.name}:", "<img class=\"discord-emote\" src=\"${it.imageUrl}\">")
		}

		// template.addClass(clazz)
		val type = serverSample.getType()
		template.addClass(when (type) {
			LorittaPartner.Type.SPONSOR -> "server-sponsor"
			LorittaPartner.Type.PARTNER -> "server-partner"
			LorittaPartner.Type.NORMAL -> "server-normal"
		})

		template.find(".server-sample-icon").attr("src", (serverSample.iconUrl
				?: "${loriUrl}assets/img/unknown.png").replace("jpg", "png") ?: "aaa")
		template.find(".server-name").text(serverSample.name)
		template.find(".server-author").text(serverSample.ownerName + "#" + serverSample.ownerDiscriminator)
		template.find(".server-tagline").html(tagline ?: ":shrug:")
		template.find(".server-upvotes").html(serverSample.validVoteCount.toString())
		val keywords = serverSample.serverListConfig.keywords.joinToString(separator = " ", transform = { "<span class=\"sample-keyword\">${legacyLocale["KEYWORD_" + it]}</span>" })
		template.find(".server-keywords").html(keywords)

		val serverIcon = serverSample.iconUrl?.replace("jpg", "png") ?: "${loriUrl}assets/img/unknown.png"

		val partnerInformation = PartnerView.PartnerInformation(
				serverSample.id,
				serverIcon,
				serverSample.invite,
				serverSample.name,
				serverSample.serverListConfig.tagline ?: ":shrug:",
				serverSample.serverListConfig.description ?: ":shrug:",
				serverSample.serverListConfig.keywords,
				serverSample.ownerId,
				serverSample.ownerName,
				serverSample.ownerDiscriminator,
				"???",
				serverSample.memberCount,
				serverSample.onlineCount,
				serverSample.serverEmotes,
				serverSample.canVote,
				serverSample.cantVoteReason,
				serverSample.canVoteNext,
				serverSample.joinedServer
		)

		template.find(".server-button").click {
			PartnerView.openServerModal(serverSample, false, legacyLocale)
		}

		div.append(template)
	}

	fun addAdvertisement(div: JQuery) {
		val advertisement = jq("<ins>")
				.addClass("adsbygoogle")
				.attr("style", "display:block")
				.attr("data-ad-client", "ca-pub-9989170954243288")
				.attr("data-ad-slot", "3480163710")
				.attr("data-ad-format", "auto")

		div.append(advertisement)
	}

	val wrapperBlur: JQuery by lazy {
		jq("#wrapperBlur")
	}

	/* val loadingScreen: JQuery by lazy {
		jq("#loading-screen")
	} */

	fun showLoadingBar(text: String? = "Salvando...") {
		wrapperBlur.css("filter", "blur(7px)")
		if (text != null)
			loadingScreen.find("#loading-screen-text").text(text)
		loadingScreen.fadeIn(250)
	}

	fun hideLoadingBar() {
		wrapperBlur.css("filter", "")
		loadingScreen.fadeOut(250)
	}

	fun injectAdvertisements(element: JQuery) {
		element.find(".guild-advertisement").attr("class", "adsbygoogle")
		try {
			js("(adsbygoogle = window.adsbygoogle || []).push({});")
		} catch (e: dynamic) {
			println("Error while injecting: " + e.message)
		}
		println("Advertisement injected!")
	}
}