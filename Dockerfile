FROM adoptopenjdk:11
LABEL maintainer="https://github.com/i-n-n-k-e-e-p-e-r"
WORKDIR /
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get install -y maven git nodejs
RUN git clone https://github.com/i-n-n-k-e-e-p-e-r/brownie-video-server
WORKDIR /brownie-video-server
RUN mvn clean install
RUN mvn vaadin:build-frontend
RUN mvn -e clean package -Pproduction
WORKDIR /
RUN mkdir /server
RUN cp /brownie-video-server/target/brownie-video-server-1.0.0.jar /server/server.jar
RUN useradd -m brownie
RUN chown -R brownie /server
USER brownie
WORKDIR /server
EXPOSE 8080
CMD java -jar -Dspring.profiles.active=prod server.jar
