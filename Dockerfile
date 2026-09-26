FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw --batch-mode dependency:go-offline

COPY src/ src/

RUN ./mvnw --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system nexos \
    && useradd --system --gid nexos --create-home nexos

COPY --from=build /workspace/target/*.jar app.jar

USER nexos

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
