echo "Setting up, using folder $1"

# Make it executable
chmod +x $1/files/start.sh

# Copy the JAR
cp platforms/twitter/build/libs/twitter-*-all.jar $1/files/cinnamon-twitter.jar