package org.brownie.server.dialogs;

import java.io.File;

import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import org.brownie.server.Application;
import org.brownie.server.views.MainView;

public class PlayerDialog extends Dialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8694753283443376902L;
	
	private File media;
	private File poster;
	
	public PlayerDialog(File media, File poster, MainView mainView) {
		super();
		
		setModal(true);
		setDraggable(true);
		setResizable(true);
		setCloseOnOutsideClick(false);
		
		VerticalLayout vl = new VerticalLayout();
		vl.setAlignItems(Alignment.CENTER);
		vl.setSpacing(true);
		vl.setPadding(true);
		vl.setSizeFull();
		
		Button closeButton = new Button("Stop and close");
		closeButton.setWidthFull();
		closeButton.setIcon(VaadinIcon.CLOSE_CIRCLE.create());
		closeButton.setDisableOnClick(true);
		closeButton.addClickListener(closeListener -> {
			Application.LOGGER.log(System.Logger.Level.DEBUG,
					"Player closed " + this.hashCode() + ". User " + mainView.getCurrentUser().getName() + ".");
			close();
			closeButton.setEnabled(true);
		});

		VideoJS videoPlayer = new VideoJS(UI.getCurrent().getSession(), media, poster);
		Application.LOGGER.log(System.Logger.Level.DEBUG,
				"New player " + this.hashCode() + " for user " + mainView.getCurrentUser().getName() + ".");

		videoPlayer.setHeight("90%");
		vl.add(videoPlayer);
		vl.add(closeButton);
		
		add(vl);
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
