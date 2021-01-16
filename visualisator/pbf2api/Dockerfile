FROM adoptopenjdk:11.0.9.1_1-jre-hotspot

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-jar", "/target/pbf2api.jar"]

EXPOSE 9090

ADD target/pbf2api-*-spring-boot.jar /target/pbf2api.jar
