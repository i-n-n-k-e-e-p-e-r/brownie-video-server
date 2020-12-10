package org.brownie.server.dialogs;


import com.brownie.videojs.FileStreamFactory;
import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
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
		setDraggable(true);
		setResizable(true);
		setCloseOnOutsideClick(false);

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeUndefined();

		addOpenedChangeListener(e -> {
			if (e.isOpened()) {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player opened " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			} else {
				Application.LOGGER.log(System.Logger.Level.DEBUG,
						"Player closed " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			}
		});

		Label title = new Label(media.getName());
		title.getStyle().set("font-weight", "bold");
		mainLayout.add(title);

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

		this.setSizeUndefined();
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

	private VerticalLayout createVideoLayout(VerticalLayout mainLayout) {
		VideoJS videoPlayer = new VideoJS(UI.getCurrent().getSession(), media, poster);
		Application.LOGGER.log(System.Logger.Level.DEBUG,
				"New player " + this.hashCode() + " for user " + mainView.getCurrentUser().getName() + ".");

		videoPlayer.setWidthFull();
		videoPlayer.setHeight("-1");

		Button playButton = CommonComponents.createButton("Play", VaadinIcon.PLAY.create(), e -> videoPlayer.play());
		playButton.setWidthFull();
		Button pauseButton = CommonComponents.createButton("Pause", VaadinIcon.PAUSE.create(), e -> videoPlayer.pause());
		pauseButton.setWidthFull();

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setAlignItems(Alignment.CENTER);
		buttons.setSpacing(true);
		buttons.setMargin(true);
		buttons.setWidthFull();
		buttons.add(playButton, pauseButton);

		mainLayout.add(buttons, videoPlayer, getCloseButton("Stop and close"));

		return mainLayout;
	}

	private VerticalLayout createImageLayout(VerticalLayout mainLayout) {
		Image image = new Image();
		StreamResource resource = new StreamResource(
				media.getName(),
				new FileStreamFactory(media));
		image.setSrc(resource);

		image.setWidthFull();
		image.setHeight("-1");

		mainLayout.add(image, getCloseButton("Close"));

		return mainLayout;
	}

	private VerticalLayout createTextLayout(VerticalLayout mainLayout) {
		TextArea textArea = new TextArea();

		try {
			Files.lines(media.toPath()).forEach(line ->
					textArea.setValue(textArea.getValue() + line + System.lineSeparator()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		textArea.setSizeFull();

		mainLayout.add(textArea, getCloseButton("Close"));

		return mainLayout;
	}

	private VerticalLayout createErrorLayout(VerticalLayout mainLayout) {
		Label errorText = new Label("File format not supported");
		errorText.addComponentAsFirst(VaadinIcon.FROWN_O.create());

		errorText.setSizeFull();

		mainLayout.add(errorText, getCloseButton("Close"));

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
