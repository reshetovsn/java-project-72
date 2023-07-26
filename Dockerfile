FROM gradle:8.2.1-eclipse-temurin:20-jdk

WORKDIR /app

COPY /app .

RUN gradle installDist

CMD ./build/install/app/bin/app

