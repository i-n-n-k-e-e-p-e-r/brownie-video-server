FROM adoptopenjdk:11
LABEL maintainer="https://github.com/i-n-n-k-e-e-p-e-r"
WORKDIR /
RUN apt-get update \
	&& apt-get install -y wget \
	&& rm -rf /var/lib/apt/lists/*
RUN mkdir /server
WORKDIR /server
RUN wget "https://github.com/i-n-n-k-e-e-p-e-r/brownie-video-server/releases/latest/download/brownie-video-server.jar"
RUN useradd -m brownie
RUN chown -R brownie /server
USER brownie
EXPOSE 8080
CMD java -jar -Dspring.profiles.active=prod brownie-video-server.jar