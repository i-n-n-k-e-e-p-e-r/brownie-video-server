package org.brownie.server.views;

import com.brownie.videojs.FileStreamFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;

public class CommonComponents {

    public static Button createButton(String text, Icon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text);
        button.setWidthFull();
        button.setIcon(icon);

        if (listener != null) button.addClickListener(listener);

        return button;
    }

    public static Map.Entry<Component, Button> getDownloadButtonWrapper(String text, File file) {
        Button button = createButton(text, VaadinIcon.DOWNLOAD.create(), null);
        button.setEnabled(true);
        if (file == null || !file.exists()) {
            button.setEnabled(false);
            return new AbstractMap.SimpleEntry<>(null, button);
        }

        StreamResource resource = new StreamResource(file.getName(), new FileStreamFactory(file));
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(resource);
        buttonWrapper.wrapComponent(button);

        return new AbstractMap.SimpleEntry<>(buttonWrapper, button);
    }

}
