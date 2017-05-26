package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import java.util.*

class SobreMimCommand : CommandBase() {
    override fun getLabel():String {
        return "sobremim";
    }

    override fun getAliases(): MutableList<String> {
         return Arrays.asList("aboutme");
    }

    override fun getDescription(): String {
        return "Altere o \"Sobre Mim\" no comando de perfil!";
    }

    override fun getCategory(): CommandCategory {
         return CommandCategory.SOCIAL;
    }

    override fun run(context: CommandContext) {
        var profile = context.lorittaUser.profile;
        if (context.args.size > 0) {
            profile.aboutMe = context.args.joinToString(" ");
            context.sendMessage(context.getAsMention(true) + "Sua mensagem de perfil foi alterada para " + profile.aboutMe + "!")
            LorittaLauncher.getInstance().ds.save(profile);
        } else {
            this.explain(context);
        }
    }
}