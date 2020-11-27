package org.brownie.server.views;


import java.io.File;
import java.nio.file.Paths;

import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.dialogs.PlayerDialog;
import org.brownie.server.providers.FileSystemDataProvider;
import org.brownie.server.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@PWA(name = "Brownie Video Server",
        shortName = "BWS",
        description = "Simple home video server",
        enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7083727339956924275L;

	/**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
	
	private File rootFile;
	private User currentUser;
	
    public MainView(@Autowired AuthenticationService authService) {
    	DBConnectionProvider.getInstance();
    	VideoJS.getResourcesRegistrations();
    	
    	LoginOverlay login = new LoginOverlay();
    	login.addLoginListener(e -> {
    		currentUser = authService.getValidUser(e.getUsername(), e.getPassword());
    		if (currentUser != null) {
        		login.close();
        		initMainView();

        		return;
    		}

    		login.setError(true);
    	});
    	
    	LoginI18n i18n = LoginI18n.createDefault();
    	login.setI18n(i18n);
    	login.setForgotPasswordButtonVisible(false);
    	login.setTitle("Welcome to Brownie Video Server");
    	login.setDescription("Simple home video server");

    	add(login);
    	login.setOpened(true);
    }
    
    protected void initMainView() {
    	this.setSizeFull();
    	
    	if (FileSystemDataProvider.getRootFile() == null) {
    		try {
    			// FIXME root dir config needed
//    			rootFile = FileSystems.getDefault().getPath("Users", "vladimirsenchihin", "Desktop", "Not_For_Backup").toFile();
    			rootFile = Paths.get(System.getProperty("user.home"), "Desktop", "Not_For_Backup").toFile();
//    			rootFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile();
    		} catch (Exception ex) {
    			rootFile = new File(new File("").getAbsolutePath());
    		} finally {
        		if (!rootFile.exists() || !rootFile.isDirectory()) {
        			Notification.show("Can not locate root directory '" + rootFile.getAbsolutePath() + "'");
        			return;
        		}
    		}
    	}
    	
        TreeGrid<File> treeGrid = new TreeGrid<>();
        
        treeGrid.addComponentHierarchyColumn(file -> {
	        	HorizontalLayout value;
	        	if (file.isDirectory()) {
	        		value = new HorizontalLayout(VaadinIcon.FOLDER.create(), new Label(file.getName()));
	        	} else {
	        		value = new HorizontalLayout(VaadinIcon.FILE.create(), new Label(file.getName()));
	        	}
	        	value.setPadding(false);
	        	value.setSpacing(true);
		        	
		        return value;
        	}).setHeader("Name").setId("file-name");
        
        treeGrid.addComponentColumn(file -> {
        	if (!file.isDirectory()) {
            	Button playButton = new Button();
            	playButton.setText("Play");
            	playButton.setIcon(VaadinIcon.PLAY.create());
            	playButton.setWidth("100px");
            	playButton.setHeight("30px");
            	playButton.setDisableOnClick(true);
            	playButton.addClickListener(playListener -> {
            		final PlayerDialog dialog = new PlayerDialog(file, null);
            		dialog.setWidth("90%");
            		dialog.setHeight("90%");
            		playButton.setEnabled(true);
            		dialog.open();
            	});
    	        	
    	        return playButton;
        	}
        	
        	return new Label();
    	}).setHeader("...").setId("file-play");

        treeGrid.setDataProvider(new FileSystemDataProvider(rootFile));
        treeGrid.setSizeFull();
        
        add(treeGrid);
    }

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

}
