package hi.verkefni.vidmot;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : Sýnir dialog sem bæði tekur á móti user input fyrir nöfn 
 *  leikmanna og sýnir stigatöfluna
 *****************************************************************************/

import java.io.IOException;
import java.util.Optional;
import java.util.function.UnaryOperator;

import hi.verkefni.vinnsla.Stig;
import hi.verkefni.vinnsla.Stigatafla;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.ButtonBar;

public class LeikmennDialog extends DialogPane {

    // FXML Tilviksbreytur
    @FXML
    private TextField fxLeikmadur1;
    @FXML
    private TextField fxLeikmadur2;
    @FXML
    private Label fxLeikmadur2Label;
    @FXML
    private TextField fxVed;
    @FXML
    private CheckBox fxCheckbox;
    @FXML
    private Tooltip fxVirkurTooltip;
    @FXML
    private ButtonType fxOk;
    @FXML
    private Label fxNafn1;
    @FXML
    private Label fxNafn2;
    @FXML
    private Label fxNafn3;
    @FXML
    private Label fxNafn4;
    @FXML
    private Label fxNafn5;
    @FXML
    private Label fxStig1;
    @FXML
    private Label fxStig2;
    @FXML
    private Label fxStig3;
    @FXML
    private Label fxStig4;
    @FXML
    private Label fxStig5;

    // Constructor
    public LeikmennDialog() {
        // Loader .fxml skjalinu
        load();

        // Gerir það að leikmaðurinn geti ekki set nein gildi í "veð" textboð nema tölu
        // á bilin 1-1000
        UnaryOperator<Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("(1000|([1-9][0-9]?[0-9]?))")) {
                return change;
            }
            return null;
        };

        fxVed.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 100, integerFilter));
        Stig[] stigatafla = new Stigatafla().getStigArray();
        if (stigatafla.length > 5) {
            Stig[] tempTafla = new Stig[5];
            System.arraycopy(stigatafla, 0, tempTafla, 0, 5);
            stigatafla = tempTafla;
        }

        // Styttir tíman þangað til að tooltipið er sýnt
        fxVirkurTooltip.setShowDelay(Duration.millis(100));

        // Sýnir "entries" í stigatöflunni á viðmótinu
        switch (stigatafla.length) {
            case 0:
                fxStig1.setVisible(false);
                fxNafn1.setVisible(false);
            case 1:
                fxStig2.setVisible(false);
                fxNafn2.setVisible(false);
            case 2:
                fxStig3.setVisible(false);
                fxNafn3.setVisible(false);
            case 3:
                fxStig4.setVisible(false);
                fxNafn4.setVisible(false);
            case 4:
                fxStig5.setVisible(false);
                fxNafn5.setVisible(false);
                break;
            default:
                break;
        }

        // Bætir "entries" við stigatöfluna á viðmótinu
        switch (stigatafla.length) {
            case 5:
                fxStig5.setText(String.valueOf(stigatafla[4].getScore()));
                fxNafn5.setText(stigatafla[4].getNafn());
            case 4:
                fxStig4.setText(String.valueOf(stigatafla[3].getScore()));
                fxNafn4.setText(stigatafla[3].getNafn());
            case 3:
                fxStig3.setText(String.valueOf(stigatafla[2].getScore()));
                fxNafn3.setText(stigatafla[2].getNafn());
            case 2:
                fxStig2.setText(String.valueOf(stigatafla[1].getScore()));
                fxNafn2.setText(stigatafla[1].getNafn());
            case 1:
                fxStig1.setText(String.valueOf(stigatafla[0].getScore()));
                fxNafn1.setText(stigatafla[0].getNafn());
            default:
                break;
        }
    }

    /**
     * Loadar .fxml skjalinu
     */
    private void load() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("leikmennDialog-view.fxml"));
        fxmlLoader.setRoot(this); // rótin á viðmótstrénu sett hér
        fxmlLoader.setController(this); // controllerinn settur hér en ekki í .fxml skránni
        try {
            fxmlLoader.load(); // viðmótstréð lesið inn (þ.e. .fxml skráin)
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Leyfir leikmanni að skrifa í seinni nafns reytinn ef checkað er í checkboxið
     */
    @FXML
    public void fxcCeckboxHandler() {
        if (fxCheckbox.isSelected()) {
            fxLeikmadur2.setDisable(false);
            fxLeikmadur2Label.setDisable(false);
            return;
        }
        fxLeikmadur2.setDisable(true);
        fxLeikmadur2Label.setDisable(true);
    }

    /**
     * Sýnir dialogið
     * 
     * @return skilar nöfnum leikmanna og hversu mikið á að veðja í hvert skipti
     */
    public String[] showDialog() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setDialogPane(this);
        return birtaLeikmennDialog(d);
    }

    /**
     * Sýnir dialogið og bíður eftir user input
     * 
     * @param d
     * @return skilar nöfnum leikmanna og hversu mikið á að veðja í hvert skipti
     */
    private String[] birtaLeikmennDialog(Dialog<ButtonType> d) {
        Optional<ButtonType> utkoma = d.showAndWait();
        if (utkoma.isPresent() && utkoma.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            if (fxCheckbox.isSelected()) {
                return new String[] { fxLeikmadur1.getText(), fxLeikmadur2.getText(),
                        fxVed.getText() };
            }
            return new String[] { fxLeikmadur1.getText(), fxVed.getText() };
        }
        return null;
    }
}
