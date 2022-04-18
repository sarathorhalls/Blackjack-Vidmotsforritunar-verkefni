package hi.verkefni.vidmot;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : sér um að setja upp loada tuttuguOgEinn-view.fxml skjalið og 
 *  búa til gluggann utan um blackjack forritið
 *****************************************************************************/

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

import hi.verkefni.vinnsla.Stigatafla;

public class BlackjackApplication extends Application {

    private static Stigatafla stigatafla;

    /**
     * Keyrist eftir main functioninu
     * Sækir gögnin frá tuttuguOgEinn-view.fxml og býr til glugga
     */
    @Override
    public void start(Stage stage) throws IOException {
        setStigatafla(new Stigatafla());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tuttuguOgEinn-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Sara's Blackjack");
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                stigatafla.writeToFile();
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }

    /**
     * Java Main function sem öll java forrit þurfa
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }

    public static Stigatafla getStigatafla() {
        return stigatafla;
    }

    private static void setStigatafla(Stigatafla stigatafla) {
        BlackjackApplication.stigatafla = stigatafla;
    }
}
