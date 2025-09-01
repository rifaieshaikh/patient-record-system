# build
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl worker -am clean package -DskipTests

# run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/worker/target/worker.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
