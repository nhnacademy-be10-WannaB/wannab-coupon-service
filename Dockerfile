FROM eclipse-temurin:21
ARG JAR_FILE=./target/wannab-coupon-service.jar
COPY ${JAR_FILE} wannab-coupon-service.jar

ENTRYPOINT ["java","-jar", "/wannab-coupon-service.jar"]