package org.brownie.server.recoder;


import org.brownie.server.Application;
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

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoDecoder {
    public static final String OUTPUT_VIDEO_FORMAT = "mp4";

    private static VideoDecoder decoder = null;
    private final Set<String> queue = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executor;

    private VideoDecoder() {
        int cores = Runtime.getRuntime().availableProcessors();
        int threadsCount = cores > 3 ? cores - 2 : 1;
        executor = Executors.newFixedThreadPool(threadsCount);
        Application.LOGGER.log(System.Logger.Level.INFO,
                "Video decoder executor initialized with " + threadsCount + " threads");
    }

    public static VideoDecoder getDecoder() {
        synchronized (VideoDecoder.class) {
            if (decoder == null) {
                decoder = new VideoDecoder();
            }
        }

        return decoder;
    }

    public synchronized void addFileToQueue(String folderName, File source) {
        Path subDirectory = MediaDirectories.createSubFolder(MediaDirectories.mediaDirectory, folderName);

        if (subDirectory == null) {
            if (source.delete()) {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Root folder is null. File deleted '" + source.getAbsolutePath() + "'");
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Root folder is null. Can't delete file '" + source.getAbsolutePath() + "'");
            }

            if (folderName.trim().length() > 0) {
                MediaDirectories.clearUploadsSubFolder(folderName.trim());
            }

            return;
        }
        final File targetFile = FileSystemDataProvider.getUniqueFileName(
                Paths.get(subDirectory.toFile().getAbsolutePath(),
                        changeExtension(source, OUTPUT_VIDEO_FORMAT).getName()).toFile());

        executor.submit(new DecodingTask(folderName, source, targetFile));
    }

    public synchronized boolean isEncoding(File file) {
        return queue.contains(file.getAbsolutePath());
    }

    public static File encodeFile(@NotNull File source, @NotNull File target, boolean changeExtension) throws IOException, EncoderException {
        if (changeExtension) target = changeExtension(target, OUTPUT_VIDEO_FORMAT);
        if (!target.exists()) {
            if (target.createNewFile()) {
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "File created '" + source.getAbsolutePath() + "'");
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Can't create file '" + source.getAbsolutePath());
            }
            EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_CREATED, target);
        }

        Encoder encoder = new Encoder();

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");

        audio.setBitRate(256000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");
        video.setX264Profile(X264_PROFILE.BASELINE);

        MultimediaObject sourceMediaObject = new MultimediaObject(source);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat(OUTPUT_VIDEO_FORMAT);
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);

        encoder.encode(sourceMediaObject, target, attrs);

        return target;
    }

    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        if (i == -1) return new File(f.getAbsolutePath() + "." + newExtension);

        String name = f.getName().substring(0, i);

        File result = new File(f.getParent(), name + "." + newExtension);
        Application.LOGGER.log(System.Logger.Level.DEBUG,
                "File extension changed from '" + f.getAbsolutePath() + "' to '" + result.getAbsolutePath() + "'");
        return result;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    class DecodingTask extends Thread {
        private final String folderName;
        private final File source;
        private final File targetFile;

        public DecodingTask(String folderName, File source, File targetFile) {
            this.folderName = folderName;
            this.source = source;
            this.targetFile = targetFile;
        }

        @Override
        public void run() {
            try {
                queue.add(targetFile.getAbsolutePath());
                EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.ENCODING_STARTED, targetFile);

                Application.LOGGER.log(System.Logger.Level.INFO,
                        "Files in queue " + queue.size() + ". Added '" + targetFile.getAbsolutePath() + "'");
                String startMsg = "Encoding STARTED to file " + targetFile.getAbsolutePath();
                Application.LOGGER.log(System.Logger.Level.INFO, startMsg);

                encodeFile(source, targetFile, false);
            } catch (IOException | EncoderException e) {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Error while encoding file '" + targetFile.getAbsolutePath() + "'", e);
                e.printStackTrace();
            } finally {
                queue.remove(targetFile.getAbsolutePath());

                EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.ENCODING_FINISHED, targetFile);

                String stopMsg = "Encoding STOPPED. Result file '" + targetFile.getAbsolutePath() + "'";
                Application.LOGGER.log(System.Logger.Level.INFO, stopMsg);
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "Files in queue " + queue.size() + ". Removed '" + targetFile.getAbsolutePath() + "'");

                if (source.exists()) {
                    if (source.delete()) {
                        Application.LOGGER.log(System.Logger.Level.INFO,
                                "Uploaded file '" + source.getAbsolutePath() + "' deleted.");
                    } else {
                        Application.LOGGER.log(System.Logger.Level.ERROR,
                                "Can't delete uploaded file '" + source.getAbsolutePath());
                    }
                }

                if (folderName.trim().length() > 0) {
                    MediaDirectories.clearUploadsSubFolder(folderName.trim());
                }
            }
        }
    }
}
