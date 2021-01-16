FROM adoptopenjdk:11.0.9.1_1-jre-hotspot

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-jar", "/target/generator.jar"]

ADD target/generator-*-spring-boot.jar /target/generator.jar
