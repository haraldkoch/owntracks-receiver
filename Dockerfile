FROM java:8
MAINTAINER Harald Koch <harald.koch@gmail.com>
ADD target/owntracks-receiver.jar /srv/my-app.jar
EXPOSE 3000
CMD ["java", "-jar", "/srv/my-app.jar"]
