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

ARG APP_VERSION

WORKDIR /opt/moirai

COPY --from=builder /opt/moirai/target/storyengine-${APP_VERSION}.jar storyengine.jar

EXPOSE 8080

CMD ["java", "-jar", "/opt/moirai/storyengine.jar"]
