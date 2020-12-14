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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

	public static String MIN_WIDTH = "360px";

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
			if (e.isOpened()) {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player opened " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			} else {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player closed " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			}
		});

		mainLayout.setPrimarySection(AppLayout.Section.NAVBAR);
		this.setWidth("-1");
		this.setHeight("-1");

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
			return;
		}

		if (FileSystemDataProvider.isDataFile(media) ) {
			add(createErrorLayout(mainLayout));
		}
	}

	private Button getCloseButton(String text) {
		Button closeButton = CommonComponents.createButton(text, VaadinIcon.CLOSE_CIRCLE.create(), e -> {
			close();
			e.getSource().setEnabled(true);
		});

		closeButton.setWidthFull();
		closeButton.setDisableOnClick(true);

		return closeButton;
	}

	private AppLayout createVideoLayout(AppLayout mainLayout) {
		VideoJS videoPlayer = new VideoJS(UI.getCurrent().getSession(), media, poster);
		Application.LOGGER.log(System.Logger.Level.DEBUG,
				"New player " + this.hashCode() + " for user " + mainView.getCurrentUser().getName() + ".");

		videoPlayer.setHeight("90%");
		videoPlayer.setWidth("-1");

		Button playButton = CommonComponents.createButton("Play", VaadinIcon.PLAY.create(), e -> videoPlayer.play());
		playButton.setWidthFull();
		Button pauseButton = CommonComponents.createButton("Pause", VaadinIcon.PAUSE.create(), e -> videoPlayer.pause());
		pauseButton.setWidthFull();

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setAlignItems(Alignment.CENTER);
		buttons.setSpacing(true);
		buttons.setMargin(true);
		buttons.setWidthFull();
		buttons.add(playButton, pauseButton, getCloseButton("Close"));

		mainLayout.addToNavbar(buttons);
		mainLayout.setContent(videoPlayer);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	private AppLayout createImageLayout(AppLayout mainLayout) {
		Image image = new Image();
		StreamResource resource = new StreamResource(
				media.getName(),
				new FileStreamFactory(media));
		image.setSrc(resource);

		image.setHeight("90%");
		image.setWidth("-1");

		mainLayout.addToNavbar(getCloseButton("Close"));
		mainLayout.setContent(image);
		mainLayout.setDrawerOpened(false);

		return mainLayout;
	}

	private AppLayout createTextLayout(AppLayout mainLayout) {
		TextArea textArea = new TextArea();

		try {
			Files.lines(media.toPath()).forEach(line ->
					textArea.setValue(textArea.getValue() + line + System.lineSeparator()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		mainLayout.addToNavbar(getCloseButton("Close"));
		mainLayout.setContent(textArea);
		mainLayout.setDrawerOpened(false);

		textArea.setSizeFull();

		return mainLayout;
	}

	private AppLayout createErrorLayout(AppLayout mainLayout) {
		Label errorText = new Label("File format not supported");
		errorText.addComponentAsFirst(VaadinIcon.FROWN_O.create());

		mainLayout.addToNavbar(getCloseButton("Close"));
		mainLayout.setContent(errorText);
		mainLayout.setDrawerOpened(false);

		errorText.setSizeFull();

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
