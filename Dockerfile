FROM openjdk:11
EXPOSE 8080
ADD target/robotmq-cluster.jar robotmq-cluster.jar

ENTRYPOINT ["java","-jar","robotmq-cluster.jar"]
