package com.mrpowergamerbr.loritta.website.requests.routes

import kotlinx.html.HTML
import kotlinx.html.head
import kotlinx.html.unsafe
import org.jooby.Request
import org.jooby.Response

object Page {
	fun getHead(req: Request, res: Response, variables: Map<String, Any?>, title: String? = null, description: String? = null, imageUrl: String? = null): HTML.() -> Unit = {
		head {
			val pathNL = variables["pathNL"]
			val websiteUrl = variables["websiteUrl"]
			val cssAssetVersion = variables["cssAssetVersion"]
			unsafe {
				raw("""
						<!-- Global site tag (gtag.js) - Google Analytics -->
		<script src="https://www.googletagmanager.com/gtag/js?id=UA-53518408-9"></script>
		<script>
				window.dataLayer = window.dataLayer || [];
		function gtag(){dataLayer.push(arguments);}
		gtag('js', new Date());

		gtag('config', 'UA-53518408-9');
		</script>
		<meta charset="utf-8">
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<title>$title • Loritta</title>
		<meta name="description" content="$description">
		<script src="https://code.jquery.com/jquery-3.2.1.min.js" crossorigin="anonymous"></script>
		<script src='https://www.google.com/recaptcha/api.js?render=explicit'></script>
		<script src="${websiteUrl}assets/js/countUp.min.js"></script>
		<script src="https://cdn.rawgit.com/showdownjs/showdown/1.7.6/dist/showdown.min.js"></script>
		<script src="//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"></script>
		<script src="${websiteUrl}assets/js/tingle.min.js"></script>
		<script src="${websiteUrl}assets/js/autosize.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
		<script type="text/javascript" src="${websiteUrl}assets/js/kotlin.min.js?v=4"></script>
		<link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css" rel="stylesheet" />
		<script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>
		<script type="text/javascript" src="${websiteUrl}assets/js/kotlinx-html-js.min.js?v=4"></script>
		<script type="text/javascript" src="${websiteUrl}assets/js/moment-with-locales.min.js"></script>
		<link rel="stylesheet" href="${websiteUrl}assets/css/style.css?v=$cssAssetVersion">
		<meta name="author" content="MrPowerGamerBR">
		<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon">
		<link rel="icon" href="/favicon.ico" type="image/x-icon">

		<meta property="og:site_name" content="Website da Loritta">
		<meta property="og:description" content="$description">
		<meta property="og:title" content="$title">
		<meta property="og:image" content="${imageUrl ?: "http://loritta.website/assets/img/loritta_gabizinha_v1.png"}">
		<meta property="og:ttl" content="600">
		<meta property="og:image:width" content="320">
		<meta property="twitter:site" content="loritta">
		<meta property="twitter:card" content="summary">
		<meta property="twitter:title" content="Página inicial">
		<meta name="theme-color" content="#00c1df">

		<link rel="alternate" hreflang="en" href="https://loritta.website/us$pathNL" />
		<link rel="alternate" hreflang="pt" href="https://loritta.website/pt$pathNL" />
		<link rel="alternate" hreflang="pt-BR" href="https://loritta.website/br$pathNL" />
		<link rel="alternate" hreflang="es" href="https://loritta.website/es$pathNL" />
					""".trimIndent())
			}
		}
	}
}