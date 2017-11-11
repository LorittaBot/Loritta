package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.google.gson.JsonParser
import com.google.gson.Gson
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import java.awt.Color
import java.util.*

class TwitchCommand : CommandBase() {
	
	override fun getLabel(): String {
		return "twitch"
	}
	
	override fun getDescription(locale: BaseLocale): String {
		return "Pesquisa um canal na twitch"
	}
	
	override fun getExample(): List<String> {
		return Arrays.asList("yoda", "MrPowerGamerBr", "baleia_roxa")
	}
	
	override fun getUsage(): String {
        return "Nome do canal"
	}
	
	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			
				val embed = EmbedBuilder()
				embed.setColor(Color(75,54,124))

				val query = context.args.joinToString(" ");
				
				val channel = HttpRequest.get("https://api.twitch.tv/kraken/channels/$query?client_id=whhhh3xldqve4xsjj6lklhznwltbi2")
					.body()
					
				val user = HttpRequest.get("https://api.twitch.tv/kraken/users/$query?client_id=whhhh3xldqve4xsjj6lklhznwltbi2")
					.body()
					
				val json = jsonParser.parse(channel).obj
				val bio = jsonParser.parse(user).obj
				
				if (json.has("name")) {
					
				var name = json["display_name"].string
				var data = json["created_at"].string
				var url = json["url"].string
				var fallowers = json["followers"].string
				var views = json["views"].string
				var lang = json["language"].string	
	
				var foto: String? = null;
				
				if(json["logo"].isJsonNull) {
					foto = "https://static-cdn.jtvnw.net/jtv_user_pictures/xarth/404_user_70x70.png"
				} else {
					foto = json["logo"].string
				}
				
				val dia = data.split("T");
				val split = dia[0].split("-")
				val momento = split[2] + "/" + split[1] + "/" + split[0]
				
				embed.setTitle(name, url)
				if(!bio["bio"].isJsonNull) embed.setDescription(bio["bio"].string)
				embed.setThumbnail(foto)
				embed.addField("Data de crianção", momento, true)
				embed.addField("Linguagem do canal", lang, true)
				embed.addField("Seguidores", fallowers, true)
				embed.addField("Total de visualizações", views, true)
				embed.addField("Link", url, true)
				if(!json["video_banner"].isJsonNull) embed.setImage(json["video_banner"].string)
				
				context.sendMessage(embed.build())
				} else {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Não encontrei nenhum canal com esse nome")			
				}	
			} else {
				context.explain()
		}
	}	
}
