echo "Setting up, using folder $1"

# Make it executable
chmod +x $1/files/start.sh

# Copy the JAR
cp platforms/discord/interactions/build/libs/interactions-*-all.jar $1/files/cinnamon-interactions.jar