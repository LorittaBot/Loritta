# Loritta's Dashboard Proxy

Each Loritta cluster hosts its own dashboard instance, because each Loritta cluster knows about specific guilds due to sharding.

Back in the day, we redirected each user to the correct dashboard instance (example: `loritta-cluster2.loritta.website`), which looked *very* ugly.

To workaround that issue, we use a custom reverse proxy that connects to the correct dashboard backend depending on the guild's IDs! Making the dashboard be seamless, even when configuring guilds in completely different clusters.