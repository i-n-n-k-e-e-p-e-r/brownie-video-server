package org.brownie.server.providers;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.brownie.server.events.EventsManager;
import org.brownie.server.events.IEventListener;

public class FileSystemDataProvider
		extends AbstractBackEndHierarchicalDataProvider<File, FilenameFilter>
		implements IEventListener {

	private static final long serialVersionUID = -421399331824343179L;

	private final File root;
	private TreeGrid grid;

	private static final Comparator<File> nameComparator =
			(fileA, fileB) -> String.CASE_INSENSITIVE_ORDER.compare(fileA.getName(), fileB.getName());
	
	public FileSystemDataProvider(TreeGrid grid, File root) {
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
		//FIXME updates doesn't work
		System.out.println("UPDATE FOR " + this);
		grid.getUI().get().access(this::refreshAll);
		grid.setDataProvider(this);
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
		if (!file.exists()) return file;

		int i = file.getName().lastIndexOf('.');
		String fileName = "";
		if (i != -1) {
			String extension = file.getName().substring(i + 1);
			fileName = file.getName().substring(0, i) + " copy" + "." + extension;
		} else {
			fileName = file.getName() + " copy";
		}

		String dir = Paths.get(file.getAbsolutePath()).getParent().toFile().getAbsolutePath();
		Path newPath = Paths.get(dir, fileName);

		System.out.println("UNIQUE NAME " + newPath.toFile().getAbsolutePath());

		return getUniqueFileName(newPath.toFile());
	}
}