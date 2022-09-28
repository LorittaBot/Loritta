FROM eclipse-temurin:18-focal

# Install required dependencies
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
RUN apt-get install -y nodejs
RUN npm install clean-css-cli -g

RUN apt-get update && apt-get install -y ffmpeg gifsicle && apt-get clean && rm -rf /var/lib/apt/lists/*