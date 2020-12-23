FROM kuops/library/java:${JAVA_BASEIMAGE_TAG}

WORKDIR /app

COPY *.jar app.jar

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["sh", "-c", "java \${JAVA_OPTS} -Dspring.profiles.active=\${SPRING_PROFILES_ACTIVE} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]