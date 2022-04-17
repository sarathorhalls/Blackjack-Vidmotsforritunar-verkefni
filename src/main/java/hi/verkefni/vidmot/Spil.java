package hi.verkefni.vidmot;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : Sérhæfður klasi sem býr til spil node fyrir addCardToHand() 
 *  functionið í BlackjackController 
 *****************************************************************************/

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import hi.verkefni.vinnsla.SpilV;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class Spil extends AnchorPane {
    @FXML
    ImageView fxCardTegund1;
    @FXML
    ImageView fxCardTegund2;
    @FXML
    Text fxCardGildi1;
    @FXML
    Text fxCardGildi2;

    /**
     * Constuctor
     * 
     * @param spil spilið sem á að byggja Node eftir
     */
    public Spil(SpilV spil) {
        // Loadar spil-view.fxml skjalinu
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("spil-view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (Exception e) {
            throw new RuntimeException("error in loading card from Spil.java " + e);
        }

        // Setur gildið á node spilið
        fxCardGildi1.setText(String.valueOf(spil.getGildi()));
        fxCardGildi2.setText(String.valueOf(spil.getGildi()));

        // Loadar Tegundar myndunum
        String tegundUrl = "myndir/" + (String.valueOf(spil.getTegund())).toLowerCase() + ".png";
        Image tegundImg;
        try {
            tegundImg = new Image(getClass().getResourceAsStream(tegundUrl));

            // Setur tegundar myndirnar á Node spilið
            fxCardTegund1.setImage(tegundImg);
            fxCardTegund2.setImage(tegundImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
