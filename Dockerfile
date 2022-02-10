FROM openjdk:11
EXPOSE 8080
ADD target/robotmq-cluster.jar robotmq-cluster.jar
ENV SERVER_SOCKET_PORT=9988
ENTRYPOINT ["java","-jar","robotmq-cluster.jar"]


