package com.mrpowergamerbr.loritta.commands;

import lombok.Getter;

@Getter
public enum CommandCategory {
	FUN("Diversão"),
	MINECRAFT("Minecraft"),
	UNDERTALE("Undertale"),
	DISCORD("Discord"),
	MISC("Miscelânea"),
	ADMIN("Administração"),
	UTILS("Utilitários"),
	MAGIC("Mágica"); // Esta categoria é usada para comandos APENAS para o dono do bot (no caso, MrPowerGamerBR#4185)
	
	String fancyTitle;
	
	CommandCategory(String fancyTitle) {
		this.fancyTitle = fancyTitle;
	}
}
