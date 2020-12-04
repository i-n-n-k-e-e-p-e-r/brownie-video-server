package org.brownie.server.providers;

import com.vaadin.flow.component.notification.Notification;
import org.brownie.server.Application;

import java.io.File;
import java.nio.file.Paths;

public class MediaDirectories {

    public static File mediaDirectory;
    public static File uploadsDirectory;

    public static void initDirectories() {
        mediaDirectory = Paths.get("media_files").toFile();
        uploadsDirectory = Paths.get("uploads").toFile();

        if (mediaDirectory.exists() && uploadsDirectory.exists()) {
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Using media directory '" + mediaDirectory.getAbsolutePath() + "'");
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Using uploads directory '" + uploadsDirectory.getAbsolutePath() + "'");
            return;
        }

        boolean mediaCreated = false;
        boolean uploadCreated = false;

        if (!mediaDirectory.exists()) {
            mediaCreated = mediaDirectory.mkdirs();
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Created media directory '" + mediaDirectory.getAbsolutePath() + "'");
        }
        if (!uploadsDirectory.exists()) {
            uploadCreated = uploadsDirectory.mkdirs();
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "Created uploads directory '" + uploadsDirectory.getAbsolutePath() + "'");
        }

        if (!mediaCreated || mediaDirectory == null || !mediaDirectory.exists() || !mediaDirectory.isDirectory()) {
            String msg = "Can't locate or create media files directory";
            if (mediaDirectory != null)
                Notification.show(msg + " '" + mediaDirectory.getAbsolutePath() + "'");
            else
                Notification.show(msg);
            Application.LOGGER.log(System.Logger.Level.ERROR, msg);
        }
        if (!uploadCreated || uploadsDirectory == null || !uploadsDirectory.exists() || !uploadsDirectory.isDirectory()) {
            String msg = "Can't locate or create uploads directory";
            if (uploadsDirectory != null)
                Notification.show(msg + " '" + uploadsDirectory.getAbsolutePath() + "'");
            else
                Notification.show(msg);
            Application.LOGGER.log(System.Logger.Level.ERROR, msg);
        }
    }
}
