FROM openjdk:14-alpine

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-jar", "/target/generator.jar"]

ADD target/generator-*-spring-boot.jar /target/generator.jar
