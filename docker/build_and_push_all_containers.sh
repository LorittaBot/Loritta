# Login into Docker Registry
echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin

sh ${0%/*}/build_and_push_container.sh cinnamon-cli ${0%/*}/cli