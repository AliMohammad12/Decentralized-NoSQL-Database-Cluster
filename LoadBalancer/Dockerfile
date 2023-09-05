FROM openjdk:17
WORKDIR /app
COPY target/LoadBalancer-1.0-SNAPSHOT.jar app.jar
EXPOSE 9000
CMD ["java", "-jar", "app.jar"]