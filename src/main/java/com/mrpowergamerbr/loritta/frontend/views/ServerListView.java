package com.mrpowergamerbr.loritta.frontend.views;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import net.dv8tion.jda.core.entities.Guild;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

public class ServerListView {

    public static Object render(RenderContext context) {
        try {
            List<Guild> guilds = LorittaLauncher.getInstance().getLorittaShards().getGuilds();

            guilds.sort(new Comparator<Guild>() {

                @Override
                public int compare(Guild o1, Guild o2) {
                    return o1.getSelfMember().getJoinDate().compareTo(o2.getSelfMember().getJoinDate());
                }
            });
            context.contextVars().put("guilds", guilds);

            PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("server_list.html");

            return template;
        } catch (PebbleException e) {
            // TODO Auto-generated catch block
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return e.toString();
        }
    }
}