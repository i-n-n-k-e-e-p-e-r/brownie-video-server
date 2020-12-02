package org.brownie.server.recoder;

import com.vaadin.flow.component.notification.Notification;
import org.brownie.server.events.EventsManager;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.providers.MediaDirectories;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.VideoSize;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoDecoder {
    public static final String OUTPUT_VIDEO_FORMAT = "mp4";

    private static VideoDecoder decoder = null;
    private static final List<String> queue = Collections.synchronizedList(new ArrayList<>());

    private VideoDecoder() {}

    public static VideoDecoder getDecoder() {
        synchronized (VideoDecoder.class) {
            if (decoder == null) {
                decoder = new VideoDecoder();
            }
        }

        return decoder;
    }

    public synchronized void addFileToQueue(String folderName, File source) {
        Path subDirectory = Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(), folderName);
        if (!subDirectory.toFile().exists()) {
            subDirectory.toFile().mkdir();
            EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED, null);
        }

        File uniqueFileName = FileSystemDataProvider.getUniqueFileName(
                Paths.get(MediaDirectories.mediaDirectory.getAbsolutePath(), source.getName()).toFile());
        final File targetFile = changeExtension(uniqueFileName, OUTPUT_VIDEO_FORMAT);

        EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.ENCODING_STARTED, null);
        queue.add(targetFile.getAbsolutePath());

        new Thread(() -> {
            try {
                System.out.println("encoding START to " + targetFile.getAbsolutePath());
                encodeFile(source, targetFile, false);
            } catch (IOException | EncoderException e) {
                e.printStackTrace();
            } finally {
                if (source.exists()) source.delete();
                queue.remove(targetFile.getAbsolutePath());

                EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.ENCODING_FINISHED, null);
                System.out.println("encoding STOP " + targetFile.getAbsolutePath());
            }
        }).start();
    }

    public synchronized boolean isEncoding(File file) {
        return queue.contains(file.getAbsolutePath());
    }

    public static File encodeFile(@NotNull File source, @NotNull File target, boolean changeExtension) throws IOException, EncoderException {
        if (changeExtension) target = changeExtension(target, OUTPUT_VIDEO_FORMAT);
        if (!target.exists()) {
            target.createNewFile();
            EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED, null);
        }

        Encoder encoder = new Encoder();

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");

        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");

        video.setX264Profile(X264_PROFILE.BASELINE);
        video.setFrameRate(30);
        video.setSize(new VideoSize(1280, 720));

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat(OUTPUT_VIDEO_FORMAT);
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);

        encoder.encode(new MultimediaObject(source), target, attrs);

        return target;
    }

    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        if (i == -1) return new File(f.getAbsolutePath() + "." + newExtension);

        String name = f.getName().substring(0, i);
        return new File(f.getParent(), name + "." + newExtension);
    }
}
