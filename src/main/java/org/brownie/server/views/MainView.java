package org.brownie.server.views;

import com.brownie.videojs.VideoJS;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import org.brownie.server.db.DBConnectionProvider;
import org.brownie.server.db.User;
import org.brownie.server.providers.MediaDirectories;
import org.brownie.server.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

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
@Push(PushMode.AUTOMATIC)
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

	private User currentUser;
	private TreeGrid<File> filesGrid;
	
    public MainView(@Autowired AuthenticationService authService) {
    	DBConnectionProvider.getInstance();
    	VideoJS.getResourcesRegistrations();
    	
    	LoginOverlay login = new LoginOverlay();
    	login.addLoginListener(e -> {
    		this.currentUser = authService.getValidUser(e.getUsername(), e.getPassword());
    		if (this.currentUser != null) {
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
		MediaDirectories.initDirectories();
    	this.setSizeFull();

       	filesGrid = MainViewComponents.createFilesTreeGrid(this);
		filesGrid.setSizeFull();
        if (this.currentUser.getGroup() == User.GROUP.ADMIN.ordinal()) {
        	MenuBar menuBar = MainViewComponents.createMenuBar(this);
        	menuBar.setWidthFull();
        	add(menuBar);
		}

        add(filesGrid);
    }

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public TreeGrid<File> getFilesGrid() {
    	return this.filesGrid;
	}
}
