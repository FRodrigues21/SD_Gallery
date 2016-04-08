package sd.tp1.clt;

import javafx.application.Application;
import javafx.stage.Stage;
import sd.tp1.gui.impl.GalleryWindow;

import java.io.IOException;

/**
 * Francisco Rodrigues 42727
 * Luis Abreu 43322
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
