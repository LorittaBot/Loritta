package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup

class APIGetCommunityInfoView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/get_community_info"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val json = JsonObject()

		if (!req.param("aminoInviteLink").isSet) {
			json["error"] = "Missing aminoInviteLink param"
			return json.toString()
		}

		var aminoInviteLink = req.param("aminoInviteLink").value()

		// Amino agora apenas aceita https
		aminoInviteLink = aminoInviteLink.replace("http://", "https://")

		if (!aminoInviteLink.endsWith("/home/")) {
			aminoInviteLink = aminoInviteLink.replace("/home", "")
			aminoInviteLink = aminoInviteLink + "/home/"
		}

		val httpRequest = HttpRequest.get(aminoInviteLink)
				.followRedirects(true)

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown Amino community"
			return json.toString()
		}

		println("headers...")
		httpRequest.headers().forEach { t, u ->
			println("$t - $u")
		}

		val body = httpRequest.body()

		println(body)

		val document = Jsoup.parse(body)

		val title = document.getElementsByAttribute("property").first { it.attr("property") == "og:site_name" }
		val description = document.getElementsByAttribute("property").first { it.attr("property") == "og:description" }
		val iconUrl = document.getElementsByAttribute("property").first { it.attr("property") == "og:image" }

		json["title"] = title.attr("content").replace(" | aminoapps.com", "")
		json["description"] = description.attr("content")
		json["iconUrl"] = iconUrl.attr("content")

		val pattern = "window\\.ServerData\\.deeplink = \"narviiapp:\\/\\/(x[0-9]+)\\/.+\";".toPattern().matcher(body).apply { find() }
		json["communityId"] = pattern.group(1)
		return json.toString()
	}
}