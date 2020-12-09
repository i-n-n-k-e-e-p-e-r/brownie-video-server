package org.brownie.server.providers;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.SortOrderProvider;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.brownie.server.Application;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileSystemDataProvider
		extends AbstractBackEndHierarchicalDataProvider<File, FilenameFilter>
		implements IEventListener, SortOrderProvider {

	private static final long serialVersionUID = -421399331824343179L;

	private final File root;
	private final TreeGrid<File> grid;

	private static final Comparator<File> nameComparator =
			(fileA, fileB) -> String.CASE_INSENSITIVE_ORDER.compare(fileA.getName(), fileB.getName());
	
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
		if ((parent.listFiles() == null || parent.listFiles().length == 0)
				|| ((query.getFilter().isPresent() && parent.listFiles(query.getFilter().get()) == null)
				|| (query.getFilter().isPresent() && parent.listFiles(query.getFilter().get()).length == 0))) {
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
		                comparator = nameComparator;
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
	public void update(EventsManager.EVENT_TYPE eventType, Object[] params) {
		var ui = grid.getUI().isPresent() ? grid.getUI().get() : null;
		if (ui != null) ui.getSession().access(() -> {
			Application.LOGGER.log(System.Logger.Level.DEBUG, "Updating listener " + this);
			this.refreshAll();
		});
	}

	@Override
	public List<EventsManager.EVENT_TYPE> getEventTypes() {
		ArrayList<EventsManager.EVENT_TYPE> types = new ArrayList<>();

		types.add(EventsManager.EVENT_TYPE.FILE_SYSTEM_CHANGED);
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
}