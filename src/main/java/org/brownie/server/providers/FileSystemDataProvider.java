package org.brownie.server.providers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.SortOrderProvider;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.brownie.server.Application;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;
import org.brownie.server.recoder.VideoDecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemDataProvider
		extends AbstractBackEndHierarchicalDataProvider<File, FilenameFilter>
		implements IEventListener, SortOrderProvider {

	private static final long serialVersionUID = -421399331824343179L;

	private final File root;
	private final TreeGrid<File> grid;

	private static final Comparator<File> fileComparator = (fileA, fileB) -> {
		if ((!fileA.isDirectory() && !fileB.isDirectory()) || (fileA.isDirectory() && fileB.isDirectory())) {
			return String.CASE_INSENSITIVE_ORDER.compare(fileA.getName(), fileB.getName());
		} else {
			if (fileA.isDirectory() && !fileB.isDirectory()) {
				return -1;
			} else {
				return 1;
			}
		}
	};
	
	public FileSystemDataProvider(TreeGrid<File> grid, File root) {
		this.grid = grid;
		this.root = root;
	}
	
	@Override
	public int getChildCount(HierarchicalQuery<File, FilenameFilter> query) {
		return (int) fetchChildren(query).count();
	}
	
	@Override
	protected Stream<File> fetchChildrenFromBackEnd(HierarchicalQuery<File, FilenameFilter> query) {
		final File parent = query.getParentOptional().orElse(root);
		if ((parent.listFiles() == null || Objects.requireNonNull(parent.listFiles()).length == 0)
				|| ((query.getFilter().isPresent() && parent.listFiles(query.getFilter().get()) == null)
				|| (query.getFilter().isPresent() && Objects.requireNonNull(parent.listFiles(query.getFilter().get())).length == 0))) {
			return sortFileStream(new ArrayList<File>().stream(), query.getSortOrders());
		}
		Stream<File> filteredFiles = query.getFilter()
				.map(filter -> Stream.of(Objects.requireNonNull(parent.listFiles(filter))))
				.orElse(Stream.of(Objects.requireNonNull(parent.listFiles())))
				.skip(query.getOffset()).limit(query.getLimit());
	
		return sortFileStream(filteredFiles, query.getSortOrders());
	}
	
	@Override
	public boolean hasChildren(File item) {
		return item.list() != null && Objects.requireNonNull(item.list()).length > 0;
	}
	
	private Stream<File> sortFileStream(Stream<File> fileStream, List<QuerySortOrder> sortOrders) {
		if (sortOrders.isEmpty()) {
		    return fileStream;
		}

		List<Comparator<File>> comparators = sortOrders.stream()
				.map(sortOrder -> {
		            Comparator<File> comparator = null;
		            if (sortOrder.getSorted().equals("file-name")) {
		                comparator = fileComparator;
		            }
		            if (comparator != null && sortOrder.getDirection() == SortDirection.DESCENDING) {
		                comparator = comparator.reversed();
		            }
		            return comparator;
		        }).filter(Objects::nonNull).collect(Collectors.toList());
		
		if (comparators.isEmpty()) {
		    return fileStream;
		}
		
		Comparator<File> first = comparators.remove(0);
		Comparator<File> combinedComparators = comparators.stream()
		        .reduce(first, Comparator::thenComparing);
		return fileStream.sorted(combinedComparators);
	}

	public File getRoot() {
		return this.root;
	}

	@Override
	public void update(EventsManager.EVENT_TYPE eventType, Object... params) {
		processEvent(grid.getUI().isPresent() ? grid.getUI().get() : null,
				eventType,
				getAllGridItems(),
				params);
	}

	private LinkedList<File> getAllGridItems() {
		LinkedList<File> gridItems = new LinkedList<>();
		List<File> roots = null;
		List<File[]> leaves = null;
		try {
			roots = grid.getDataCommunicator()
					.fetchFromProvider(0, grid.getDataCommunicator().getDataProviderSize())
					.collect(Collectors.toList());
			gridItems.addAll(roots);

			leaves = roots.stream().filter(File::isDirectory).map(File::listFiles).collect(Collectors.toList());
			leaves.forEach(f -> gridItems.addAll(Arrays.stream(f).collect(Collectors.toList())));
		} finally {
			if (roots != null) roots.clear();
			if (leaves != null) leaves.clear();
		}

		return gridItems;
	}

	private void processEvent(UI ui, EventsManager.EVENT_TYPE eventType, List<File> gridItems, Object... params) {
		Application.LOGGER.log(System.Logger.Level.DEBUG, "Updating FileSystemDataProvider " + this);

		switch(eventType) {
			case ENCODING_STARTED:
			case ENCODING_FINISHED:
			case FILE_RENAMED: {
				Set<?> forUpdate = Arrays.stream(params).collect(Collectors.toSet());
				for (var f : gridItems) {
					for (var o : forUpdate) {
						if (((File)o).getAbsolutePath().equals(f.getAbsolutePath())) {
							if (ui != null) ui.access(() -> refreshItem(f));
							break;
						}
					}
				}

				break;
			}
			case FILE_CREATED:
			case FILE_DELETED:
			case FILE_MOVED: {
				if (ui != null) ui.access(this::refreshAll);
				break;
			}

			default : break;
		}
	}

	@Override
	public List<EventsManager.EVENT_TYPE> getEventTypes() {
		ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

		types.add(EventsManager.EVENT_TYPE.FILE_MOVED);
		types.add(EventsManager.EVENT_TYPE.FILE_RENAMED);
		types.add(EventsManager.EVENT_TYPE.FILE_DELETED);
		types.add(EventsManager.EVENT_TYPE.FILE_CREATED);
		types.add(EventsManager.EVENT_TYPE.ENCODING_STARTED);
		types.add(EventsManager.EVENT_TYPE.ENCODING_FINISHED);

		return types;
	}

	public static File getUniqueFileName(File file) {
		if (!file.exists()) {
			Application.LOGGER.log(System.Logger.Level.DEBUG,
					"Generated unique file name '" + file.getAbsolutePath() + "'");
			return file;
		}

		int i = file.getName().lastIndexOf('.');
		String fileName;
		if (i != -1) {
			String extension = file.getName().substring(i + 1);
			fileName = file.getName().substring(0, i) + " copy" + "." + extension;
		} else {
			fileName = file.getName() + " copy";
		}

		String dir = Paths.get(file.getAbsolutePath()).getParent().toFile().getAbsolutePath();
		Path newPath = Paths.get(dir, fileName);

		return getUniqueFileName(newPath.toFile());
	}

	@Override
	public Stream<QuerySortOrder> apply(SortDirection sortDirection) {
		String fieldId = grid.getColumns().get(0).getId().isPresent() ? grid.getColumns().get(0).getId().get() : "";

		QuerySortOrder order = new QuerySortOrder(fieldId, sortDirection);
		List<QuerySortOrder> result = new ArrayList<>();
		result.add(order);

		return result.stream();
	}

	public static String getMIMETypeFromURLConnections(File file) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();

		String mimeType = null;
		try {
			mimeType = fileNameMap.getContentTypeFor(file.getName());
		} catch (Exception ex) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while getting MIME type in getMIMETypeFromURLConnections", ex);
		} finally {
			if (mimeType == null || mimeType.length() == 0) mimeType = "";
		}

		return mimeType;
	}

	public static String getMIMETypeFromFiles(File file) {
		String result = null;
		try {
			result = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while getting MIME type in getMIMETypeFromFiles", e);
		}
		if (result == null) {
			return "";
		}
		return result;
	}

	public static boolean isVideo(File file) {
		return getMIMETypeFromFiles(file).contains("video");
	}

	public static boolean isImage(File file) {
		return getMIMETypeFromFiles(file).contains("image");
	}

	public static boolean isAudio(File file) {
		return getMIMETypeFromFiles(file).contains("audio");
	}

	public static boolean isText(File file) {
		return getMIMETypeFromFiles(file).contains("text");
	}

	public static boolean isDataFile(File file) {
		return (!isVideo(file) && !isImage(file) && !isAudio(file) && !isText(file));
	}

	public static void copyUploadedFile(String folderName, File original) {
		if (original == null || folderName == null) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Can't copy file. Sub directory or original file is null.");
			if (original != null && original.delete()) {
				Application.LOGGER.log(System.Logger.Level.ERROR,
						"Original file deleted '" + original.getAbsolutePath() + "'");
			}
			return;
		}

		Path subDirectory = MediaDirectories.createSubFolder(MediaDirectories.mediaDirectory, folderName.trim());
		if (subDirectory == null) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Can't copy file. Root directory is null.");
			return;
		}

		File uniqueFileName = FileSystemDataProvider.getUniqueFileName(
				Paths.get(subDirectory.toFile().getAbsolutePath(), original.getName()).toFile());

		Path copied = Paths.get(subDirectory.toFile().getAbsolutePath(), uniqueFileName.getName());

		Path originalPath = original.toPath();
		try {
			Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while coping file from '" + original.getAbsolutePath() + "' to '" + copied.toFile().getAbsolutePath() + "'", e);
			e.printStackTrace();
		} finally {
			if (original.exists()) {
				if (original.delete()) {
					Application.LOGGER.log(System.Logger.Level.INFO,
							"Original file deleted '" + original.getAbsolutePath() + "'");
				} else {
					Application.LOGGER.log(System.Logger.Level.ERROR,
							"Can't delete file '" +
									Paths.get(MediaDirectories.uploadsDirectory.getAbsolutePath(), folderName).toFile().getAbsolutePath() + "'");
				}
			}

			if (folderName.trim().length() > 0) {
				MediaDirectories.clearUploadsSubFolder(folderName.trim());
			}

			if (Objects.requireNonNull(subDirectory.toFile().listFiles()).length == 0) {
				EventsManager.getManager().notifyAllListeners(EventsManager.EVENT_TYPE.FILE_CREATED);
			}
		}
	}

	public static Map.Entry<Integer, Integer> getImageSize(File file) {
		if (file != null && file.exists()) {
			BufferedImage bufferedImage = null;
			try {
				bufferedImage = ImageIO.read(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (bufferedImage != null) {
				int width = bufferedImage.getWidth();
				int height = bufferedImage.getHeight();
				return new AbstractMap.SimpleEntry<>(width, height);
			}
		}

		return null;
	}

	public static void deleteFileOrDirectory(File fileForDelete) {
		if (!fileForDelete.exists()) { return; }

		if (fileForDelete.isDirectory()) {
			List.of(Objects.requireNonNull(fileForDelete.listFiles())).forEach(childFile -> {
				if (childFile.exists() && !VideoDecoder.getDecoder().isEncoding(childFile)) {
					if (childFile.delete()) {
						Application.LOGGER.log(System.Logger.Level.INFO,
								"Deleted '" + childFile.getAbsolutePath() + "'");
					} else {
						Application.LOGGER.log(System.Logger.Level.ERROR,
								"Can't delete '" + childFile.getAbsolutePath() + "'");
					}
				}
			});

			if (fileForDelete.listFiles() == null ||
					(fileForDelete.listFiles() != null
							&& Objects.requireNonNull(fileForDelete.listFiles()).length == 0)) {
				if (fileForDelete.delete()) {
					Application.LOGGER.log(System.Logger.Level.INFO,
							"Deleted directory '" + fileForDelete.getAbsolutePath() + "'");
				} else {
					Application.LOGGER.log(System.Logger.Level.ERROR,
							"Can't delete directory'" + fileForDelete.getAbsolutePath() + "'");
				}
			}
		} else {
			if (fileForDelete.delete()) {
				Application.LOGGER.log(System.Logger.Level.INFO,
						"Deleted '" + fileForDelete.getAbsolutePath() + "'");
			} else {
				Application.LOGGER.log(System.Logger.Level.ERROR,
						"Can't delete '" + fileForDelete.getAbsolutePath() + "'");
			}
		}
	}
}