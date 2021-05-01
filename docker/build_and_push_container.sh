# $1 = Image Name
# $2 = Folder
IMAGE_NAME = $1

# Login to GitHub's Container Repository
echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin

# Build cli
docker build $2 --file Dockerfile --tag cinnamon-cli --label "runnumber=${GITHUB_RUN_ID}"

IMAGE_ID=ghcr.io/${{ github.repository_owner }}/$IMAGE_NAME

# Change all uppercase to lowercase
IMAGE_ID=$(echo $IMAGE_ID | tr '[A-Z]' '[a-z]')

# Strip git ref prefix from version
VERSION=$(echo "${{ github.ref }}" | sed -e 's,.*/\(.*\),\1,')

# Strip "v" prefix from tag name
[[ "${{ github.ref }}" == "refs/tags/"* ]] && VERSION=$(echo $VERSION | sed -e 's/^v//')

# Use Docker `latest` tag convention
[ "$VERSION" == "master" ] && VERSION=latest
[ "$VERSION" == "main" ] && VERSION=latest

echo IMAGE_ID=$IMAGE_ID
echo VERSION=$VERSION

docker tag $IMAGE_NAME $IMAGE_ID:$VERSION
docker push $IMAGE_ID:$VERSION