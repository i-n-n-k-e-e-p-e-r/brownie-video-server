package org.brownie.server.dialogs;


import com.brownie.videojs.FileStreamFactory;
import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.StreamResource;
import org.brownie.server.Application;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.views.CommonComponents;
import org.brownie.server.views.MainView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PlayerDialog extends Dialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8694753283443376902L;

	private File media;
	private File poster;
	private final MainView mainView;
	
	public PlayerDialog(File media, File poster, MainView mainView) {
		super();

		this.media = media;
		this.poster = poster;
		this.mainView = mainView;

		setModal(true);
		setDraggable(false);
		setResizable(false);

		AppLayout mainLayout = new AppLayout();

		addOpenedChangeListener(e -> {
			this.mainView.getFilesGrid().deselectAll();
			if (e.isOpened()) {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player opened " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			} else {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player closed " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
				this.mainView.getFilesGrid().focus();
			}
			this.mainView.getFilesGrid().select(media);
		});

		mainLayout.setPrimarySection(AppLayout.Section.NAVBAR);
		setWidth("100%");
		setHeight("100%");

		if (FileSystemDataProvider.isVideo(media) || FileSystemDataProvider.isAudio(media)) {
			add(createVideoLayout(mainLayout));
			return;
		}

		if (FileSystemDataProvider.isImage(media) ) {
			add(createImageLayout(mainLayout));
			return;
		}

		if (FileSystemDataProvider.isText(media) ) {
			add(createTextLayout(mainLayout));
			setResizable(true);
			return;
		}

		if (FileSystemDataProvider.isDataFile(media) ) {
			add(createErrorLayout(mainLayout));
		}
	}

	private Button getCloseButton() {
		Button closeButton = CommonComponents.createButton("Close", VaadinIcon.CLOSE_CIRCLE.create(), e -> {
			close();
			e.getSource().setEnabled(true);
		});

		closeButton.setWidthFull();
		closeButton.setDisableOnClick(true);

		return closeButton;
	}

	private AppLayout createVideoLayout(AppLayout mainLayout) {
		VerticalLayout contentLayout = new VerticalLayout();
		VideoJS videoPlayer = new VideoJS(UI.getCurrent().getSession(), media, poster);
		Application.LOGGER.log(System.Logger.Level.DEBUG,
				"New player " + this.hashCode() + " for user " + mainView.getCurrentUser().getName() + ".");

		videoPlayer.setHeight("-1");
		videoPlayer.setWidth("95%");

		contentLayout.setPadding(true);
		contentLayout.setAlignItems(Alignment.CENTER);
		contentLayout.setSpacing(true);
		contentLayout.setMargin(true);
		contentLayout.add(CommonComponents.getDownloadButtonWrapper(media.getName(), media).getKey(), videoPlayer);
		contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		contentLayout.setWidth("98%");
		contentLayout.setHeight("94%");

		Button playButton = CommonComponents.createButton("Play", VaadinIcon.PLAY.create(), e -> videoPlayer.play());
		playButton.setWidthFull();
		Button pauseButton = CommonComponents.createButton("Pause", VaadinIcon.PAUSE.create(), e -> videoPlayer.pause());
		pauseButton.setWidthFull();

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setAlignItems(Alignment.CENTER);
		buttons.setSpacing(true);
		buttons.setMargin(true);
		buttons.setWidthFull();
		buttons.add(playButton,
				pauseButton,
				getCloseButton());

		mainLayout.addToNavbar(buttons);
		mainLayout.setContent(contentLayout);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	private AppLayout createImageLayout(AppLayout mainLayout) {
		VerticalLayout contentLayout = new VerticalLayout();
		Image image = new Image();
		StreamResource resource = new StreamResource(
				media.getName(),
				new FileStreamFactory(media));
		image.setSrc(resource);

		image.setHeight("95%");
		image.setWidth("-1");

		contentLayout.setPadding(true);
		contentLayout.setAlignItems(Alignment.CENTER);
		contentLayout.setSpacing(true);
		contentLayout.setMargin(true);
		contentLayout.add(CommonComponents.getDownloadButtonWrapper(media.getName(), media).getKey(), image);
		contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		contentLayout.setWidth("98%");
		contentLayout.setHeight("94%");

		mainLayout.addToNavbar(getCloseButton());
		mainLayout.setContent(contentLayout);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	private AppLayout createTextLayout(AppLayout mainLayout) {
		VerticalLayout contentLayout = new VerticalLayout();
		TextArea textArea = new TextArea();

		try {
			Files.lines(media.toPath()).forEach(line ->
					textArea.setValue(textArea.getValue() + line + System.lineSeparator()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		textArea.setSizeFull();
		contentLayout.setPadding(true);
		contentLayout.setAlignItems(Alignment.CENTER);
		contentLayout.setSpacing(true);
		contentLayout.setMargin(true);
		contentLayout.add(CommonComponents.getDownloadButtonWrapper(media.getName(), media).getKey(), textArea);
		contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		contentLayout.setWidth("98%");
		contentLayout.setHeight("94%");

		mainLayout.addToNavbar(getCloseButton());
		mainLayout.setContent(contentLayout);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	private AppLayout createErrorLayout(AppLayout mainLayout) {
		VerticalLayout contentLayout = new VerticalLayout();
		Label errorText = new Label("File format not supported");
		errorText.addComponentAsFirst(VaadinIcon.FROWN_O.create());

		errorText.setSizeFull();
		contentLayout.setPadding(true);
		contentLayout.setAlignItems(Alignment.CENTER);
		contentLayout.setSpacing(true);

		contentLayout.add(CommonComponents.getDownloadButtonWrapper(media.getName(), media).getKey(), errorText);
		contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		contentLayout.setWidth("98%");
		contentLayout.setHeight("94%");

		mainLayout.addToNavbar(getCloseButton());
		mainLayout.setContent(contentLayout);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	public File getMedia() {
		return media;
	}

	public void setMedia(File media) {
		this.media = media;
	}

	public File getPoster() {
		return poster;
	}

	public void setPoster(File poster) {
		this.poster = poster;
	}
}
