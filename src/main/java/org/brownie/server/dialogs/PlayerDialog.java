package org.brownie.server.dialogs;

import java.io.File;

import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

public class PlayerDialog extends Dialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8694753283443376902L;
	
	private File media;
	private File poster;
	
	public PlayerDialog(File media, File poster) {
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
		closeButton.setWidth("90%");
		closeButton.setHeight("30px");
		
		closeButton.setDisableOnClick(true);
		closeButton.addClickListener(closeListener -> {
			close();
			closeButton.setEnabled(true);
			return;
		});

		VideoJS videoPlayer = new VideoJS(UI.getCurrent().getSession(), media, poster);
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
