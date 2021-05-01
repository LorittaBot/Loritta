# Login into Docker Registry
echo "$GITHUB_TOKEN" | docker login ghcr.io -u $GITHUB_ACTOR --password-stdin

sh ${0%/*}/build_and_push_container.sh cinnamon-cli ${0%/*}/cli
sh ${0%/*}/build_and_push_container.sh cinnamon-discord-interactions ${0%/*}/discord-interactions
sh ${0%/*}/build_and_push_container.sh cinnamon-twitter ${0%/*}/twitter