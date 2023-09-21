FROM openjdk:17
WORKDIR /app
COPY target/ClusterWebClient-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9011
CMD ["java", "-jar", "app.jar"]