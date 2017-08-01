package com.mrpowergamerbr.loritta.frontend;

import com.google.common.cache.CacheBuilder;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;
import lombok.Getter;
import org.jooby.Jooby;

import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class LorittaWebsite extends Jooby {
    @Getter
    private static String frontendFolder;
    @Getter
    private static String websiteUrl;
    @Getter
    public static PebbleEngine engine;

    @Getter
    private static final ConcurrentMap<Object, Object> oAuth2 = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).maximumSize(1000).build().asMap(); // 14 dias = refresh tokens são invalidados após 14 dias

    {
        port(4568);
        assets("/**", Paths.get(frontendFolder + "static/"));
        get("/**", (req, res) -> GlobalHandler.render(req, res));
        post("/**", (req, res) -> GlobalHandler.render(req, res));
    }

    public static void init(String websiteUrl, String frontendFolder) {
        LorittaWebsite.websiteUrl = websiteUrl;
        LorittaWebsite.frontendFolder = frontendFolder;
        FileLoader fl = new FileLoader();
        fl.setPrefix(frontendFolder);
        engine = new PebbleEngine.Builder().cacheActive(false).strictVariables(true).loader(fl).build();

        run(LorittaWebsite::new, new String[]{});
    }

    public static boolean canManageGuild(TemmieGuild g) {
        return g.isOwner() || (g.getPermissions() >> 5 & 1) == 1;
    }
}