package org.brownie.server.recoder;

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
import java.util.Arrays;

public class VideoDecoder {

    public static final String OUTPUT_VIDEO_FORMAT = "mp4";

    public static void encodeFile(@NotNull File source, @NotNull File target) throws IOException {
        if (target.exists()) target.delete();
        target = changeExtension(target, OUTPUT_VIDEO_FORMAT);
        target.createNewFile();

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

        try {
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (source.exists()) source.delete();
        }
    }

    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0,i);
        return new File(f.getParent(), name + newExtension);
    }
}
