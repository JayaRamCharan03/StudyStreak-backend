FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV DB_URL=jdbc:postgresql://dpg-db5kdk59fdbs73d6fnng-a:5432/studystreak_yhri
ENV DB_USER=studystreak_yhri_user
ENV DB_PASSWORD=xNvwaTOXQeo1VnewAOXOy4lk9pt5J7Ed
EXPOSE 9090
CMD ["java","-jar","app.jar"]
