FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}","-Dserver.port=${SERVER_PORT:8080}","-jar","/app/app.jar"]