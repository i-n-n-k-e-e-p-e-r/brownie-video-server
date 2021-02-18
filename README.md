## Description
Simple private web file server (Vaadin 14, Java 11).    
Share files with your homies, buddies and grannies.
First of all it was created for uploading videos in any format through web gui and then watch it in browser on tablet or anyware.
Server encodes uploaded video to h.264 mp4 container with the same resolution and fps as in source file and stereo audio in 256kbs acc (encoding settings will be added in the future).
Other files are uploaded unchanged.
Supports preview and play for video, audio, text and images.
Supports Safary, Firefox, Chrome and Edge.
Desktop and mobile layout.

## Build 
##### Maven
1. Clone this repository
2. mvn clean install; mvn vaadin:build-frontend; mvn -e clean package -Pproduction;

##### Docker
1. Clone this repository
2. docker build --no-cache --rm -t brownie-video-server PATH_TO_DOCKER_FILE_OF_THIS_REPO


## Run 
##### Maven
1. java -jar ./target/brownie-video-server.jar  
2. Go to localhost:8080  
3. Try to login with any credential  
4. Create new administrator user  
5. Login with new administrator user credential  

##### Docker
1. Run docker container (two options)
    - With console output:
        * docker run -ti -p 7920:8080 brownie-video-server  
    - In the background:
        * docker run -d --restart=always --privileged -p 7920:8080 brownie-video-server
2. Go to http://localhost:7920  
3. Try to login with any credential
4. Create new administrator user
5. Login with new administrator user credential
