package com.mrpowergamerbr.loritta.website.views.subviews.api

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import org.apache.commons.lang3.time.DateUtils
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import java.text.SimpleDateFormat
import java.util.*

class APIGetRssFeedTitleView : NoVarsView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/get_feed_title"))
	}

	override fun render(req: Request, res: Response, path: String): String {
		res.type(MediaType.json)
		val json = JsonObject()

		if (!req.param("feedLink").isSet) {
			json["error"] = "Missing channelLink param"
			return json.toString()
		}

		val channelLink = req.param("feedLink").value()

		val httpRequest = HttpRequest.get(channelLink)
				.header("Cookie", "YSC=g_0DTrOsgy8; PREF=f1=50000000&f6=7; VISITOR_INFO1_LIVE=r8qTZn_IpAs")
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")

		if (httpRequest.code() == 404) {
			json["error"] = "Unknown channel"
			return json.toString()
		}

		val jsoup = Jsoup.parse(httpRequest.body(), "", Parser.xmlParser())

		var title: String? = null
		var link: String? = null
		var entryItem: Element? = null
		var dateRss: String? = null
		var description: String? = null;
		var rssCalendar: Calendar? = null

		if (jsoup.select("feed").attr("xmlns") == "http://www.w3.org/2005/Atom") {
			// Atom Feed
			title = jsoup.select("feed entry title").first().text()
			link = jsoup.select("feed entry link").first().attr("href")
			entryItem = jsoup.select("feed entry").first()
			if (jsoup.select("feed entry published").isNotEmpty()) {
				dateRss = jsoup.select("feed entry published").first().text();
			} else if (jsoup.select("feed entry updated").isNotEmpty()) {
				dateRss = jsoup.select("feed entry updated").first().text();
			}
			rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateRss);
			// Enquanto a maioria das feeds RSS colocam title e link... a maioria não coloca a descrição corretamente
			// Então vamos verificar de duas maneiras
			if (jsoup.select("feed entry description").isNotEmpty()) {
				description = jsoup.select("feed entry description").first().text()
			} else if (jsoup.select("feed entry content").isNotEmpty()) {
				description = jsoup.select("feed entry content").first().text()
			}
		} else if (jsoup.select("rdf|RDF").attr("xmlns") == "http://purl.org/rss/1.0/") {
			// RDF Feed (usada pela Steam)
			title = jsoup.select("item title").first().text()
			link = jsoup.select("item link").first().text()
			entryItem = jsoup.select("item").first()
			dateRss = jsoup.select("item pubDate").first().text();
			val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
			val date = sdf.parse(dateRss)
			rssCalendar = DateUtils.toCalendar(date)
			if (!jsoup.select("item description").isEmpty()) {
				description = jsoup.select("item description").first().text()
			}
		} else if (jsoup.select("channel").isNotEmpty()) {
			// Provavelemente é uma feed RSS então :)
			title = jsoup.select("channel item title").first().text()
			link = jsoup.select("channel item link").first().text()
			entryItem = jsoup.select("channel item").first()
			dateRss = jsoup.select("channel item pubDate").first().text();
			val sdf = if (!dateRss.matches(Regex("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+"))) {
				if (dateRss[3] == ',') {
					SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
				} else {
					SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
				}
			} else { // Algumas rss feeds usam este estilo de data (bug?)
				SimpleDateFormat("dd/mm/yyyy HH:mm:ss", Locale.ENGLISH)
			}
			val date = sdf.parse(dateRss)
			rssCalendar = DateUtils.toCalendar(date)
			if (!jsoup.select("channel item description").isEmpty()) {
				description = jsoup.select("channel item description").first().text()
			}
		} else {
			// Faço a mínima ideia do que seja isto.
			json["error"] = "Invalid feed"
			return json.toString();
		}

		if (dateRss == null) {
			json["error"] = "Invalid feed"
			return json.toString();
		}

		if (description != null) {
			description = Jsoup.clean(description, "", Whitelist.simpleText(), Document.OutputSettings().escapeMode(Entities.EscapeMode.xhtml))
		}
		try {
			val pageTitle = jsoup.title()
			val entryTitle = title;
			json["title"] = pageTitle
			json["entryTitle"] = entryTitle
			return json.toString()
		} catch (e: Exception) {
			json["error"] = e.cause.toString()
			return json.toString()
		}
	}
}