# build
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl http -am clean package -DskipTests

# run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/http/target/http.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
