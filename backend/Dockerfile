FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests && cp target/*.jar app.jar

RUN jlink \
      --add-modules java.base,java.logging,java.xml,java.naming,java.sql,java.management,java.instrument,java.desktop,java.security.jgss,java.security.sasl,java.net.http,java.prefs,java.rmi,jdk.unsupported,jdk.crypto.ec,jdk.management,jdk.naming.dns,jdk.zipfs,jdk.localedata \
      --strip-debug --no-header-files --no-man-pages --compress=zip-9 \
      --output /opt/java-minimal

FROM alpine:3.20

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=builder /opt/java-minimal /opt/java-minimal
COPY --from=builder --chown=app:app /app/app.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]