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
RUN apt-get update && apt-get install -y ffmpeg gifsicle unzip && apt-get clean && rm -rf /var/lib/apt/lists/*

# MANUAL INSTALLATION OF PLAYWRIGHT'S DEPENDENCIES because I don't want to install all browsers (which is what Playwright does by default)
# THE APP MUST RUN WITH "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true"
# The Chromium version must match what Playwright uses
# A path can be provided via the "setExecutablePath" option
# See: https://github.com/microsoft/playwright/blob/main/packages/playwright-core/browsers.json
RUN mkdir /playwright/ && cd /playwright/ && wget "https://playwright.azureedge.net/builds/chromium/1124/chromium-win64.zip" && unzip chromium-win64.zip