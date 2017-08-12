package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.util.concurrent.TimeUnit

class RepCommand : CommandBase() {
    override fun getLabel():String {
        return "rep";
    }

    override fun getAliases(): List<String> {
        return listOf("reputation", "reputação", "reputacao");
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.REP_DESCRIPTON;
    }

    override fun getCategory(): CommandCategory {
         return CommandCategory.SOCIAL;
    }

    override fun getExample(): List<String> {
        return listOf("@Loritta", "@MrPowerGamerBR")
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun run(context: CommandContext) {
        var profile = context.lorittaUser.profile;

        if (context.message.mentionedUsers.isNotEmpty()) {
            var user = context.message.mentionedUsers[0];

            if (user == context.userHandle) {
                context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.REP_SELF);
                return;
            }

            var diff = System.currentTimeMillis() - profile.lastReputationGiven;

            if (3.6e+6 > diff) {
                var fancy = String.format(context.locale.MINUTES_AND_SECONDS,
                        60 - (TimeUnit.MILLISECONDS.toMinutes(diff)),
                        60 - (TimeUnit.MILLISECONDS.toSeconds(diff) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)))
                );
                context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.REP_WAIT.msgFormat(fancy));
                return;
            }

            var givenProfile = LorittaLauncher.loritta.getLorittaProfileForUser(user.id);

            // Agora nós iremos dar reputação para este usuário
            givenProfile.receivedReputations.add(context.userHandle.id);

            // E vamos salvar a última vez que o usuário deu reputação para o usuário
            profile.lastReputationGiven = System.currentTimeMillis();

            context.sendMessage("☝ **|** " + context.getAsMention(true) + context.locale.REP_SUCCESS.msgFormat(user.asMention));

			// E vamos salvar as configurações
			LorittaLauncher.loritta.ds.save(givenProfile);
			LorittaLauncher.loritta.ds.save(profile);
        } else {
			this.explain(context);
		}
    }
}