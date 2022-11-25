@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import SaveStuff
import jq
import kotlinx.browser.document
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.EmbedEditorStuff
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLButtonElement

class WelcomerConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/welcomer") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val textChannels: List<ServerConfig.TextChannel>,
			val welcomerConfig: ServerConfig.WelcomerConfig
	)

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "textchannels", "welcomer")
			switchContentAndFixLeftSidebarScroll(call)

			if (guild.welcomerConfig.deleteJoinMessagesAfter != null)
				jq("#deleteJoinMessagesAfter").`val`(guild.welcomerConfig.deleteJoinMessagesAfter)

			if (guild.welcomerConfig.deleteRemoveMessagesAfter != null)
				jq("#deleteLeaveMessagesAfter").`val`(guild.welcomerConfig.deleteRemoveMessagesAfter)

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1")

			LoriDashboard.configureTextChannelSelect(jq("#canalJoinId"), guild.textChannels, guild.welcomerConfig.channelJoinId)
			LoriDashboard.configureTextChannelSelect(jq("#canalLeaveId"), guild.textChannels, guild.welcomerConfig.channelRemoveId)

			LoriDashboard.configureTextArea(
					jq("#joinMessage"),
					true,
					true,
					jq("#canalJoinId"),
					true,
					EmbedEditorStuff.userInContextPlaceholders(locale),
					true,
					mapOf(
							"Padrão" to "\uD83D\uDC49 {@user} entrou no servidor!",
							"Padrão, só que melhor" to "<a:lori_happy:521721811298156558> | {@user} acabou de entrar na {guild}! Agora temos {guild-size} membros!",
							"Lista de Informações" to """{@user}, bem-vindo(a) ao {guild}! <a:lori_happy:521721811298156558>
• Leia as #regras *(psiu, troque o nome do canal aqui na mensagem!)* para você poder conviver em harmonia! <:blobheart:467447056374693889>
• Converse no canal de #bate-papo <:lori_pac:503600573741006863>
• E é claro, divirta-se! <a:emojo_feriado:393751205308006421>

Aliás, continue sendo incrível! (E eu sou muito fofa! :3)""",
							"Embed Simples" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Seja bem-vindo(a)!",
      "description":"Olá {@user}! Seja bem-vindo(a) ao {guild}!"
   }
}""",
							"Embed com Avatar" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Bem-vindo(a)!",
      "description":"Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
    "footer": {
      "text": "ID do usuário: {user-id}"
    }
   }
}""",
							"Embed com Avatar e Imagem" to """{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋Bem-vindo(a)!",
      "description":"Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
	  "image": {
	     "url": "https://media.giphy.com/media/GPQBFuG4ABACA/source.gif"
	  },
    "footer": {
      "text": "ID do usuário: {user-id}"
    }
   }
}""",
							"Embed com Informações" to """{
   "content":"{@user}",
   "embed":{
      "color":-14689638 ,
      "title":"{user}#{user-discriminator} | Bem-vindo(a)!",
      "thumbnail": {
         "url" : "{user-avatar-url}"
      },
      "description":"<:lori_hug:515328576611155968> Olá, seja bem-vindo(a) ao {guild}!",
      "fields": [
        {
            "name": "👋 Sabia que...",
            "value": "Você é o {guild-size}º membro aqui no servidor?",
            "inline": true
        },
        {
            "name": "🛡 Tag do Usuário",
            "value": "`{user}#{user-discriminator}` ({user-id})",
            "inline": true
        },
        {
            "name": "📛 Precisando de ajuda?",
            "value": "Caso você tenha alguma dúvida ou problema, chame a nossa equipe!",
            "inline": true
        },
        {
            "name": "👮 Evite punições!",
            "value": "Leia as nossas #regras para evitar ser punido no servidor!",
            "inline": true
        }
      ],
    "footer": {
      "text": "{guild} • © Todos os direitos reservados."
    }
   }
}""",
							"Kit Social Influencer™" to """{
   "content":"{@user}",
   "embed":{
      "color":-2342853,
      "title":"{user}#{user-discriminator} | Bem-vindo(a)!",
      "thumbnail": {
         "url" : "{user-avatar-url}"
      },
      "description":"Salve {@user}! Você acabou de entrar no servidor do {guild}, aqui você poderá se interagir com fãs do {guild}, conversar sobre suas coisas favoritas e muito mais!",
      "fields": [
        {
            "name": "📢 Fique atento!",
            "value": "Novos vídeos do {guild} serão anunciados no #vídeos-novos!",
            "inline": true
        },
        {
            "name": "📺 Inscreva-se no canal se você ainda não é inscrito! ",
            "value": "[Canal](https://www.youtube.com/channel/UC-eeXSRZ8cO-i2NZYrWGDnQ)",
            "inline": true
        },
        {
            "name": "🐦 Siga no Twitter! ",
            "value": "[@LorittaBot](https://twitter.com/LorittaBot)",
            "inline": true
        },
        {
            "name": "🖼 Siga no Instagram! ",
            "value": "[@lorittabot](https://instagram.com/lorittabot)",
            "inline": true
        }
      ],
    "footer": {
      "text": "Eu sou muito fofa e o {guild} também :3"
    }
   }
}"""

					)
			)

			LoriDashboard.configureTextArea(
					jq("#leaveMessage"),
					true,
					true,
					jq("#canalLeaveId"),
					true,
					EmbedEditorStuff.userInContextPlaceholders(locale),
					true,
					mapOf(
							"Padrão" to "\uD83D\uDC48 {user} saiu do servidor...",
							"Padrão, só que melhor" to "<a:bongo_lori_triste:524894216510373888> | {user} saiu do {guild}... espero que algum dia ele volte...",
							"Embed Simples" to """{
   "content":"",
   "embed":{
      "color":-6250077,
      "title":"Tchau...",
      "description":"{user} saiu do {guild}... espero que algum dia ele volte..."
   }
}""",
							"Embed com Avatar" to """{
   "content":"",
   "embed":{
      "color":-6250077,
      "title":"😭 #chateada!",
      "description":"⚰ **{user}** saiu do servidor... <:lori_triste:370344565967814659>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
      "footer": {
         "text": "ID do usuário: {user-id}"
      }
   }
}
""",
							"Embed com Avatar e Imagem" to """{
   "content":"",
   "embed":{
      "color":-6250077,
      "title":"😭 #chateada!",
      "description":"⚰ **{user}** saiu do servidor... <:lori_triste:370344565967814659>",
      "author":{
         "name":"{user}#{user-discriminator}",
         "icon_url":"{user-avatar-url}"
      },
      "thumbnail":{
         "url":"{user-avatar-url}"
      },
	  "image": {
	     "url": "https://i.imgur.com/RUIaWW3.png"
	  },
      "footer": {
         "text": "ID do usuário: {user-id}"
      }
   }
}"""
					)
			)
			LoriDashboard.configureTextArea(
					jq("#joinPrivateMessage"),
					true,
					true,
					null,
					true,
					EmbedEditorStuff.userInContextPlaceholders(locale)
			)

			LoriDashboard.configureTextArea(
					jq("#banMessage"),
					true,
					true,
					jq("#canalLeaveId"),
					true,
					EmbedEditorStuff.userInContextPlaceholders(locale)
			)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}
		}
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("welcomer", {
			val deleteJoinMessagesAfter = (jq("#deleteJoinMessagesAfter").`val`() as String).toIntOrNull()
			if (deleteJoinMessagesAfter == 0) {
				it["deleteJoinMessagesAfter"] = null
			}

			val deleteLeaveMessagesAfter = (jq("#deleteRemoveMessagesAfter").`val`() as String).toIntOrNull()
			if (deleteLeaveMessagesAfter == 0) {
				it["deleteRemoveMessagesAfter"] = null
			}
		})
	}
}