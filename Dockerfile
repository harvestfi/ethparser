FROM openjdk:11
COPY ./dist /dist
CMD ["java","-XX:+UseContainerSupport","-Dserver.port=8080","-Dmanagement.server.port=9090","-jar","dist/ethparser.jar"]
