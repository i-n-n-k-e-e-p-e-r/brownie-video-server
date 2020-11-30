package org.brownie.server.providers;

import com.vaadin.flow.component.notification.Notification;

import java.io.File;
import java.nio.file.Paths;

public class MediaDirectories {

    public static File mediaDirectory;
    public static File uploadsDirectory;

    public static void initDirectories() {
        mediaDirectory = Paths.get("media_files").toFile();
        uploadsDirectory = Paths.get("uploads").toFile();

        if (mediaDirectory.exists() && uploadsDirectory.exists()) {
            return;
        }

        boolean mediaCreated = false;
        boolean uploadCreated = false;

        if (!mediaDirectory.exists()) {
            mediaCreated = mediaDirectory.mkdirs();
        }
        if (!uploadsDirectory.exists()) {
            uploadCreated = uploadsDirectory.mkdirs();
        }

        if (!mediaCreated || mediaDirectory == null || !mediaDirectory.exists() || !mediaDirectory.isDirectory()) {
            String msg = "Can not locate or create media files directory";
            if (mediaDirectory != null)
                Notification.show(msg + " '" + mediaDirectory.getAbsolutePath() + "'");
            else
                Notification.show(msg);
        }
        if (!uploadCreated || uploadsDirectory == null || !uploadsDirectory.exists() || !uploadsDirectory.isDirectory()) {
            String msg = "Can not locate or create uploads directory";
            if (uploadsDirectory != null)
                Notification.show(msg + " '" + uploadsDirectory.getAbsolutePath() + "'");
            else
                Notification.show(msg);
        }
    }
}
