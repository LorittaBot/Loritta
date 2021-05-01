# We use Amazon Corretto because OpenJDK only provides Early Access builds
FROM amazoncorretto:15-alpine-jdk

# Install required dependencies
RUN apk add git

# Copy all required files
COPY files /home/cinnamon/

# Clone the locales repository
RUN git clone https://github.com/LorittaBot/LorittaLocales /home/cinnamon/locales

# And that's it!
ENTRYPOINT ["/home/cinnamon/start.sh"]