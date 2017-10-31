package com.mrpowergamerbr.loritta.userdata;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@Setter
@Accessors(fluent = true)
@Entity(value = "servers", noClassnameStored = true)
@ToString
public class ServerConfig {
    @Id
    @Indexed(options = @IndexOptions(unique = true))
    public String guildId; // Guild ID
    public String commandPrefix = "+"; // Command Prefix (example: +help or .help or etc)
    public ArrayList<String> disabledCommands = new ArrayList<String>(); // Comandos desativados
    public DebugOptions debugOptions = new DebugOptions();
    public boolean deleteMessageAfterCommand; // Deletar mensagem do comando após executar ele?
    public String localeId = "default";

    public HashMap<String, CommandOptions> commandOptions = new HashMap<String, CommandOptions>(); // Command Options
    // Os command options são salvos assim:
    // CommandBase.getClass().getSimpleName() - CommandOptions

    // boolean warnOnFail; // Avisar ao usuário quando escrever o comando errado?
    public boolean explainOnCommandRun = true; // Explicar quando rodar *comando*? (Ou quando usar *comando* :shrug:)
    public boolean explainInPrivate = false; // Caso explainOnCommandRun estiver ativado, é para explicar APENAS no privado ou mandar no global?
    public boolean commandOutputInPrivate = false; // É para mandar o output (ou seja, tudo do comando) no privado em vez de mandar no global?
    public boolean warnOnMissingPermission = false; // Avisar quando a Loritta não tem permissão para falar em um canal específico
    public boolean mentionOnCommandOutput = true; // Caso esteja ativado, a Loritta irá marcar quem executou na mensagem resposta
    public boolean warnOnUnknownCommand = false;
    public ArrayList<String> blacklistedChannels = new ArrayList<String>(); // Canais em que os comandos são bloqueados
    public boolean warnIfBlacklisted = false;
    public String blacklistWarning = "{@user} Você não pode usar comandos no {@channel}, bobinho(a)! <:blobBlush:357977010771066890>";
    public ArrayList<NashornCommand> nashornCommands = new ArrayList<NashornCommand>(); // Comandos customizados

    public ArrayList<NashornEventHandler> nashornEventHandlers = new ArrayList<>();

    public JoinLeaveConfig joinLeaveConfig = new JoinLeaveConfig();
    public MusicConfig musicConfig = new MusicConfig();
    public AminoConfig aminoConfig = new AminoConfig();
    public YouTubeConfig youTubeConfig = new YouTubeConfig();
    public StarboardConfig starboardConfig = new StarboardConfig();
    public RssFeedConfig rssFeedConfig = new RssFeedConfig();
    public EventLogConfig eventLogConfig = new EventLogConfig();
    public AutoroleConfig autoroleConfig = new AutoroleConfig();
    public InviteBlockerConfig inviteBlockerConfig = new InviteBlockerConfig();
    public PermissionsConfig permissionsConfig = new PermissionsConfig();
    public HashMap<String, Integer> slowModeChannels = new HashMap<>(); // Canais com SlowMode ativado
    public HashMap<String, String> starboardEmbeds = new HashMap<String, String>(); // Quais mensagens correspondem a mensagens no starboard

    public HashMap<String, LorittaServerUserData> userData = new HashMap<String, LorittaServerUserData>();

    public CommandOptions getCommandOptionsFor(CommandBase cmd) {
        if (cmd instanceof NashornCommand) { // Se é um comando feito em Nashorn...
            // Vamos retornar uma configuração padrão!
            return new CommandOptions();
        }
        if (commandOptions.containsKey(cmd.getClass().getSimpleName())) {
            return commandOptions.get(cmd.getClass().getSimpleName());
        }
        try {
            return (CommandOptions) LorittaLauncher.getInstance().getCommandManager().getDefaultCmdOptions().get(cmd.getClass().getSimpleName()).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Getter
    @Setter
    public static class DebugOptions extends CommandOptions {
        public boolean enableAllModules; // Caso ativado, TODAS as modules estarão ativadas
    }
}
