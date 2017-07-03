package com.mrpowergamerbr.loritta.utils

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.LorittaLauncher

class PurgeServerConfigsThread : Thread("Purge Server Configs Thread") {
	override fun run() {
		super.run()

		while (true) {
			// Vamos pegar todos os ServerConfigs...
			var documents = LorittaLauncher.loritta.mongo.getDatabase("loritta")
					.getCollection("servers").find();

			for (document in documents) {
				var id = document.getString("_id");

				var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(id);

				if (guild == null) {
					// Se a guild é nula, quer dizer que a Loritta não está mais nesta guild, então vamos deletar
					// este ServerConfig!

					LorittaLauncher.loritta.mongo.getDatabase("loritta")
							.getCollection("servers").deleteOne(Filters.eq("_id", id)); // Tchau... :(
				}
			}
			Thread.sleep(60000);
		}
	}
}