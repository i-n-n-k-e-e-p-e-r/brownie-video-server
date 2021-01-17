package org.brownie.server.providers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.brownie.server.Application;
import org.brownie.server.events.EventsManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediaDirectories {

    public static File mediaDirectory;
    public static File uploadsDirectory;

    public static synchronized void initDirectories(UI ui) {
        mediaDirectory = Paths.get(Application.BASE_PATH + File.separator + "media_files").toFile();
        uploadsDirectory = Paths.get(Application.BASE_PATH + File.separator + "uploads").toFile();

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
            if (ui != null)
                if (mediaDirectory != null) {
                    Notification.show(msg + " '" + mediaDirectory.getAbsolutePath() + "'");
                } else {
                    Notification.show(msg);
                }
            Application.LOGGER.log(System.Logger.Level.ERROR, msg);
        }
        if (!uploadCreated || uploadsDirectory == null || !uploadsDirectory.exists() || !uploadsDirectory.isDirectory()) {
            String msg = "Can't locate or create uploads directory";
            if (ui != null)
                if (uploadsDirectory != null) {
                    Notification.show(msg + " '" + uploadsDirectory.getAbsolutePath() + "'");
                } else {
                    Notification.show(msg);
                }
            Application.LOGGER.log(System.Logger.Level.ERROR, msg);
        }
    }

    public static synchronized Path createSubFolder(File root, String subFolder) {
        if (!root.isDirectory()) return null;
        if (!root.exists()) {
            if (root.mkdirs()) {
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "Root for sub folder created '" + root.getAbsolutePath() + "'");
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Can't create root folder for sub folder '" + root.getAbsolutePath() + "'");
            }
        }
        if (subFolder == null) subFolder = "";
        if (subFolder.trim().length() == 0) subFolder = "";
        if (subFolder.length() == 0) return Paths.get(root.getAbsolutePath());

        Path pathWithSubfolder = Paths.get(root.getAbsolutePath(), subFolder);

        if (!pathWithSubfolder.toFile().exists()) {
            if (pathWithSubfolder.toFile().mkdir()) {
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "Sub folder created '" + pathWithSubfolder.toFile() + "'");
                EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_CREATED,
                        pathWithSubfolder.toFile());
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Can't create sub folder '" + pathWithSubfolder.toFile() + "'");
            }
        }

        return pathWithSubfolder;
    }

    public static synchronized void clearUploadsSubFolder(String folderName) {
        File[] uploadedFiles = Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().listFiles();
        if (Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().exists() &&
                uploadedFiles != null && uploadedFiles.length == 0) {
            if (Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().delete()) {
                Application.LOGGER.log(System.Logger.Level.INFO,
                        "Folder deleted '" +
                                Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().getAbsolutePath() + "'");
            } else {
                Application.LOGGER.log(System.Logger.Level.ERROR,
                        "Can't delete folder '" +
                                Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().getAbsolutePath() + "'");
            }
        }
    }

    public static List<String> getFoldersInMedia() {
        return Arrays.stream(Objects.requireNonNull(MediaDirectories.mediaDirectory.listFiles()))
                .filter(File::isDirectory)
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
