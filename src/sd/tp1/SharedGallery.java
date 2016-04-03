package sd.tp1;

import javafx.application.Application;
import javafx.stage.Stage;
import sd.tp1.gui.impl.GalleryWindow;

import java.io.IOException;

/*
 * Launches the local shared gallery application.
 */
public class SharedGallery extends Application {

	GalleryWindow window;
	
	public SharedGallery() throws IOException {
		window = new GalleryWindow(new SharedGalleryContentProvider());
	}	

    public static void main(String[] args){
        launch(args);
    }
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		window.start(primaryStage);
	}
}
