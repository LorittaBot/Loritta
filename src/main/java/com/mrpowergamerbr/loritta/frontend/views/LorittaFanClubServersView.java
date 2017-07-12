package com.mrpowergamerbr.loritta.frontend.views;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LorittaFanClubServersView {

    public static Object render(RenderContext context) {
        try {
            PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("loritta_fan_club_servers.html");

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