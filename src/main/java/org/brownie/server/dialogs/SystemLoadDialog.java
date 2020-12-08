package org.brownie.server.dialogs;

import com.sun.management.OperatingSystemMXBean;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import org.brownie.server.Application;

import java.io.File;
import java.lang.management.ManagementFactory;

public class SystemLoadDialog extends Dialog {

    public static final String CPU_LOAD_FORMAT = "CPU load: %.2f%%";
    public static final String MEMORY_USAGE_FORMAT = "Memory usage: %.2f/%.2f MB";
    public static final String DISC_USAGE_FORMAT = "Free disc space: %.2f/%.2f GB";

    private final OperatingSystemMXBean monitor = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final Label version = new Label("Version: N/A");
    private final Label cpuLoad = new Label("CPU load: N/A");
    private final Label memoryUsage = new Label("Memory usage: N/A");
    private final Label discUsage = new Label("Free disc space: N/A");

    private InnerThread innerThread = null;

    public SystemLoadDialog() {
        super();
        init();
    }

    private void init() {
        FormLayout mainLayout = new FormLayout();

        Label title = new Label("System information");
        title.getStyle().set("font-weight", "bold");
        Button closeButton = new Button();
        closeButton.setText("Close");
        closeButton.addClickListener(e -> this.close());
        closeButton.setWidthFull();

        version.setText("Server version: " + this.getClass().getPackage().getImplementationVersion());
        mainLayout.add(title,
                version,
                cpuLoad,
                memoryUsage,
                discUsage,
                closeButton);

        mainLayout.setSizeFull();
        add(mainLayout);

        this.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                stop();
            }
        });
        this.addDetachListener(e -> {
            stop();
        });

        if (innerThread == null) this.innerThread = new InnerThread(this);
        this.innerThread.start();
    }

    public static SystemLoadDialog showSystemLoadDialog() {
        SystemLoadDialog dialog = new SystemLoadDialog();
        dialog.setMinWidth("360px");
        dialog.setWidth("360px");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setModal(true);
        dialog.open();
        return dialog;
    }

    private void stop() {
        if (innerThread != null) {
            innerThread.stopInnerThread();
            innerThread = null;
        }
    }

    public void updateSystemLoadValues() {
        var ui = this.getUI().isPresent() ? this.getUI().get() : null;
        if (ui != null) ui.getSession().access(() -> {
            cpuLoad.setText(String.format(CPU_LOAD_FORMAT, monitor.getSystemCpuLoad() * 100));

            double freeMemory = (double)monitor.getFreePhysicalMemorySize() / (1024 * 1024);
            double totalMemory = (double)monitor.getTotalPhysicalMemorySize() / (1024 * 1024);

            memoryUsage.setText(String.format(MEMORY_USAGE_FORMAT,
                    totalMemory - freeMemory,
                    (double)monitor.getTotalPhysicalMemorySize() / (1024 * 1024)));

            File root = new File("/");
            discUsage.setText(String.format(DISC_USAGE_FORMAT,
                    (double)root.getUsableSpace() / 1073741824,
                    (double)root.getTotalSpace() / 1073741824));
        });
    }

    private class InnerThread extends Thread {
        private boolean terminated = false;
        private final SystemLoadDialog parentDialog;

        InnerThread(SystemLoadDialog parentDialog) {
            this.parentDialog = parentDialog;
        }

        @Override
        public void run() {
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "System monitor started " + Thread.currentThread().getId());
            while (!terminated) {
                Application.LOGGER.log(System.Logger.Level.DEBUG,
                        "System monitor running " + Thread.currentThread().getId());
                parentDialog.updateSystemLoadValues();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Application.LOGGER.log(System.Logger.Level.INFO,
                    "System monitor stopped " + Thread.currentThread().getId());
        }

        public void stopInnerThread() {
            this.terminated = true;
        }
    }
}
