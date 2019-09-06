FROM java:8
MAINTAINER: liuzhiqiang/2425358736@qq.com
VOLUME ["/tmp"]
EXPOSE 6533
ADD webrtc_live-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
