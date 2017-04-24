package com.mrpowergamerbr.loritta;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.mrpowergamerbr.loritta.utils.LorittaConfig;

import lombok.Getter;

@Getter
public class LorittaLauncher {
	public static Loritta loritta;

	public static void main(String[] args) {
		Gson gson = new Gson();
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
				FileUtils.writeStringToFile(file, gson.toJson(new LorittaConfig()), "UTF-8");
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
