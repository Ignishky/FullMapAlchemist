FROM adoptopenjdk:11.0.9.1_1-jre-hotspot

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-jar", "/target/preparator.jar"]

ADD target/preparator-*-spring-boot.jar /target/preparator.jar
