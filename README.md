## Description
Vaadin 14, Java 11.
Simple private web file server. 
Share files with your homies, buddies and grannies.
Uploads video with encoding to h.264 mp4 container with the same resolution as in source file, 30fps, stereo audio in 128kbs (encoding settings will be added in the future).
Other files uploaded unchanged.
Supports preview for text, images, video an audio files.

## Build
mvn vaadin:build-frontend; mvn -e clean package -Pproduction;

## Run 
java -jar ./target/brownie-video-server-<VERSION>.jar
Go to localhost:8080. 
Try to login with any credential.
Create new administrator user.