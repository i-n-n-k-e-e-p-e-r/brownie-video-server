## Description
Simple private web file server (Vaadin 14, Java 11).    
Share files with your homies, buddies and grannies.  
Uploads video with encoding to h.264 mp4 container with the same resolution as in source file, 30fps, stereo audio in 128kbs acc (encoding settings will be added in the future).
Other files uploaded unchanged.  
Supports preview for text, images, video an audio files.  
Desktop and mobile layout.

## Build 
##### Maven
1. Clone this repository
2. mvn clean install; mvn vaadin:build-frontend; mvn -e clean package -Pproduction;

##### Docker
1. Clone this repository
2. docker build -t brownie-video-server PATH_TO_THIS_REPO_DIRECTORY


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
