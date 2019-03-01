FROM openjdk:8-jre-alpine

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-jar", "/target/preparator.jar"]

ADD target/preparator-*-spring-boot.jar /target/preparator.jar
