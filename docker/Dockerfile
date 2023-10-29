FROM eclipse-temurin:21-jammy
ARG NODE_MAJOR=18

# Install required dependencies
RUN apt-get update && apt-get install -y ca-certificates curl gnupg
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list
RUN apt-get update && apt-get install nodejs -y
RUN curl -fsSL https://deb.nodesource.com/node_18.x | bash -
RUN apt-get install -y nodejs
RUN npm install clean-css-cli -g

RUN apt-get update && apt-get install -y ffmpeg gifsicle && apt-get clean && rm -rf /var/lib/apt/lists/*