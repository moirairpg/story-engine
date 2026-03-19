FROM eclipse-temurin:25-jdk-ubi10-minimal AS builder

WORKDIR /opt/moirai

COPY ./ ./

RUN microdnf install -y yum && \
    microdnf clean all && \
    yum update -y && \
    yum upgrade -y && \
    yum install -y maven && \
    mvn clean install -DskipTests

FROM eclipse-temurin:25-jre-ubi10-minimal AS runner

ENV DISCORD_BOT_CLIENT_ID=
ENV DISCORD_BOT_CLIENT_SECRET=
ENV DISCORD_BOT_REDIRECT_URL=
ENV DISCORD_BOT_API_TOKEN=
ENV OPENAI_API_TOKEN=
ENV SPRING_APPLICATION_PROFILES=
ENV POSTGRES_HOST=
ENV POSTGRES_DB=
ENV POSTGRES_USER=
ENV POSTGRES_PASSWORD=
ENV SUCCESS_REDIRECT_URL=
ENV FAIL_REDIRECT_URL=
ENV LOGOUT_REDIRECT_URL=

WORKDIR /opt/moirai

COPY --from=builder /opt/moirai/target/storyengine-3.0.0-SNAPSHOT.jar storyengine-3.0.0-SNAPSHOT.jar

EXPOSE 8080
EXPOSE 8000

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000", "-jar", "/opt/moirai/storyengine-3.0.0-SNAPSHOT.jar"]
