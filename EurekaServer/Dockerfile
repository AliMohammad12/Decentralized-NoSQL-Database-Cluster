FROM openjdk:17
WORKDIR /app
COPY target/EurekaServer-1.0-SNAPSHOT.jar app.jar
EXPOSE 8761
CMD ["java", "-jar", "app.jar"]