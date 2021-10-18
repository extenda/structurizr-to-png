FROM eclipse-temurin:17-centos7

RUN yum -y install graphviz

COPY target/lib /app/lib
COPY target/*.jar /app

EXPOSE 3000

WORKDIR /docs

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "/app/structurizr-to-png.jar"]
