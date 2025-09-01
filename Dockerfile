FROM maven:3.9.9-amazoncorretto-23 AS builder

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn dependency:resolve
RUN mvn clean package -DskipTests

FROM amazoncorretto:23

COPY --from=builder /app/target/*.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
