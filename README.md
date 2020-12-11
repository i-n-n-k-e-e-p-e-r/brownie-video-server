## Description
Simple private web file server (Vaadin 14, Java 11).    
Share files with your homies, buddies and grannies.  
Uploads video with encoding to h.264 mp4 container with the same resolution as in source file, 30fps, stereo audio in 128kbs acc (encoding settings will be added in the future).
Other files uploaded unchanged.  
Supports preview for text, images, video an audio files.  
Desktop and mobile layout.

## Build
mvn vaadin:build-frontend; mvn -e clean package -Pproduction;

## Run
<ol>
<li>java -jar ./target/brownie-video-server-VERSION.jar</li> 
<li>Go to localhost:8080</li> 
<li>Try to login with any credential.</li>
<li>Create new administrator user.</li>
</ol>