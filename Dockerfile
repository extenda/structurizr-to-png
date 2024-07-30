FROM eclipse-temurin:21

RUN apt update \
    && apt -y install graphviz \
    && apt clean

COPY target/lib /app/lib
COPY target/*.jar /app

EXPOSE 3000

WORKDIR /docs

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "/app/structurizr-to-png.jar"]
