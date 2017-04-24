package com.mrpowergamerbr.loritta;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrpowergamerbr.loritta.utils.LorittaConfig;

import lombok.Getter;

@Getter
public class LorittaLauncher {
	public static Loritta loritta;

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File file = new File("./config.json");
		LorittaConfig config = null;
		
		if (file.exists()) {
			String json;
			try {
				json = FileUtils.readFileToString(file, "UTF-8");
				config = gson.fromJson(json, LorittaConfig.class);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1); // Sair caso der erro
				return;
			}
		} else {
			System.out.println("Bem-Vindo(a) a Loritta!");
			System.out.println("Como é a sua primeira vez executando ela, nós iremos criar um arquivo chamado \"config.json\", que você deverá configurar a Loritta antes de usar ela!");
			System.out.println("");
			System.out.println("Após configurar a Loritta, inicie ela novamente!");
			try {
				FileUtils.writeStringToFile(file, gson.toJson(new LorittaConfig() // Colocar valores padrões na LorittaConfig, já que se não o Gson só irá gerar "{}"
						.setClientId("Client ID do Bot")
						.setClientSecret("Client Secret do Bot")
						.setClientToken("Client Token do Bot")
						.setFrontendFolder("Pasta do frontend da Loritta, coloque uma / no final!")
						.setMercadoPagoClientId("Client ID do MercadoPago, usado na página de doação")
						.setMercadoPagoClientToken("Client Token do MercadoPago, usado na página de doação")
						.setOwnerId("ID do dono do bot, usado para alguns comandos \"especiais\"")
						.setWebsiteUrl("URL do website da Loritta, coloque uma / no final!")
						.setYoutubeKey("Key da API do YouTube, usado no comando \"+youtube\"")
						.setCurrentlyPlaying("loritta.website | Shantae: Half-Genie Hero")), "UTF-8");
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(1);
			return;
		}

		loritta = new Loritta(config);
		loritta.start();
	}

	// STATIC MAGIC(tm)
	public static Loritta getInstance() {
		return loritta;
	}
}
