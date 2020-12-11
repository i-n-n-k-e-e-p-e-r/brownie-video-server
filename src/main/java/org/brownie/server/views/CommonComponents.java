package org.brownie.server.views;

import com.brownie.videojs.FileStreamFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.File;

public class CommonComponents {

    public static Button createButton(String text, Icon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text);
        button.setWidthFull();
        button.setIcon(icon);

        if (listener != null) button.addClickListener(listener);

        return button;
    }

    public static Component getDownloadButtonWrapper(String text, Icon icon, File file) {
        Button button = createButton(text, icon, null);
        if (file == null || !file.exists()) {
            button.setEnabled(false);
            return button;
        }

        StreamResource resource = new StreamResource(file.getName(), new FileStreamFactory(file));
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(resource);
        buttonWrapper.wrapComponent(button);

        return buttonWrapper;
    }
}
