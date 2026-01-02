# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação (skip tests para build mais rápido)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Criar usuário não-root (segurança)
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar JAR do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Criar diretório de uploads
RUN mkdir -p /app/uploads && chown spring:spring /app/uploads

# Usar usuário não-root
USER spring:spring

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/candidatos/health || exit 1

# Executar aplicação
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]