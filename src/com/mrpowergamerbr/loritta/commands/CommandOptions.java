package com.mrpowergamerbr.loritta.commands;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class CommandOptions {
	// Todas as próximas opções são "command overrides", isto permite fazer overrides nas opções globais da Loritta
	public boolean override; // Os comandos a seguir só serão ativados CASO override esteja ativo!
	public boolean explainOnCommandRun = true; // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
	public boolean explainInPrivate = false; // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
	public boolean commandOutputInPrivate = false; // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
	public boolean mentionOnCommandOutput = true; // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
	public boolean deleteMessageAfterCommand = false; // Deletar mensagem do comando após executar ele?
	// Comandos podem extender a classe CommandOptions para colocar novas opções

	// TODO: Remover
	@Deprecated
	public boolean getAsBoolean(String key) {
		return false;
	}
}
