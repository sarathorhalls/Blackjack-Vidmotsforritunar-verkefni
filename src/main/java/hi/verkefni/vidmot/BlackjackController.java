package hi.verkefni.vidmot;

import java.net.URL;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import hi.verkefni.vinnsla.Leikmadur;
import hi.verkefni.vinnsla.LeikmennManager;
import hi.verkefni.vinnsla.SpilV;
import hi.verkefni.vinnsla.Stig;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : Controllerinn sér um að keyra flest allt sem snýr að viðmótinu.
 *  Eins og að setja upp nýjan leik, loada TextInputDialog-inu sem biður leikmann
 *  um nafn, er með Handlerana fyrir alla takkana, sér um öll animations og
 *  displayar hvort leikmaðurinn hafi unnið eða ekki.
 *****************************************************************************/

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class BlackjackController implements Initializable {
    private static final String BET = "Bet:    ";
    private static final String TOTAL = "Total: ";

    // FXML Tilviksbreytur
    @FXML
    private GridPane mainGridPane;

    @FXML
    private Text fxBetTotal;
    @FXML
    private Text fxCurrentBet;

    @FXML
    private Text fxDealerTotal;
    @FXML
    private Pane fxDealerPane;

    @FXML
    private Text fxPlayerTotal;
    @FXML
    private Text fxPlayerName;
    @FXML
    private Pane fxPlayerPane;

    @FXML
    private HBox fxPlayButtons;
    @FXML
    private HBox fxStopButtons;

    @FXML
    private Button fxSurrenderButton;

    // Java tilviksbreytur
    private LeikmennManager leikmennManager;
    private boolean allGameOver;
    private boolean showLate;

    // Tilviksbreytur til að breyta litnum á tökkunum þegar ýtt er á þá
    private static final String BUTTONSTYLEPARAMS = "-fx-border-width: 3px; -fx-border-color: black; -fx-border-radius: 3px;";
    private static final String BUTTONCOLOR = "#bd132f";
    private static final String BUTTONPRESSEDCOLOR = "#9c1026";

    // Tilviksbreytur fyrir animationQueue
    private Node[] animationQueue;
    private Path[] animationPathQueue;
    private boolean animationQueueBool;
    private int animationQueuePos;
    private ObservableList<Spil> leikmadurCards;

    /****************************** RESETING STATES ******************************/

    /**
     * Keyrist bæði þegar Initalize() kallar í það þegar leikurinn byrjar en líka
     * þegar
     * leikmaðurinn ýtir á CONTINUE
     * frumstillir tilviksbreytur, breytir tökkunum til baka ef þú varst að klára
     * leik
     * og bættir spilum við höndina hjá dealer og leikmanninum
     */
    @FXML
    public void NewGameHandler() {
        // Frumstillum java tilviksbreytur
        leikmennManager.newGame();
        allGameOver = false;
        showLate = false;

        // Hreinsa spilin af skjánum ef þú varst að klára leik
        if (animationQueue != null) {
            Node[] temp = new Node[24];
            for (int i = 0; i < animationQueuePos; i++) {
                for (Node node : mainGridPane.getChildren()) {
                    if (node.equals(animationQueue[i])) {
                        temp[i] = node;
                    }
                }
            }
            for (Node node : temp) {
                mainGridPane.getChildren().remove(node);
            }
        }

        leikmadurCards = FXCollections.observableArrayList();

        // Frumstillum animationQueue tilviksbreyturnar
        animationQueue = new Node[24];
        animationPathQueue = new Path[24];
        animationQueueBool = true;
        animationQueuePos = 0;

        // Tekur burt textan um hvort leikmaðurinn hafi unnið eða ekki
        bigTextVisibility();

        fxDealerTotal.setText("0");

        // Frumstillir takkana neðst
        showPlayButtons(true);

        // Gefur dealerinum eitt spil og leikmanninum tvö spil
        addCardToHand(true);

        // Leikmadurinn fær spilin sín
        newLeikmadurGame();
    }

    private void newLeikmadurGame() {
        // Setur bet gildin
        fxBetTotal.setText(TOTAL + String.valueOf(leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxCurrentBet.setText(BET + String.valueOf(leikmennManager.getBetAmount()));

        // Uppfærir textan á skjánum fyrir samtals gildi spilana hjá dealerinum og
        // leikmanninum
        fxPlayerTotal.setText("0");

        // Setur nafn Leikmannsins
        Logger.debug("Tried setting name to: {}", leikmennManager.getCurrentLeikmadur().getNafn());
        fxPlayerName.setText(leikmennManager.getCurrentLeikmadur().getNafn());

        // Sýnir surrender takkann
        hideSurrender(false);

        // Gefur leikmanninum tvö spil
        addCardToHand(false);
        addCardToHand(false);
        disableUserActions(true);
    }

    /****************************** STATE MANAGEMENT ******************************/

    private void leikmadurGerir() {
        Logger.info("Tried changing states, called from {}", leikmennManager.getCurrentLeikmadur());
        if (checkCanDo()) {
            return;
        }
        checkIfCurrentLeikmadurGameOver();
    }

    private boolean checkCanDo() {
        Logger.debug("Check if {} can do: {}", leikmennManager.getCurrentLeikmadur(),
                leikmennManager.getCurrentLeikmadur().isCanDo());
        return leikmennManager.getCurrentLeikmadur().isCanDo();
    }

    private boolean checkIfAllGameOver() {
        Logger.debug("Check if all gameOver, called from {} : {}", leikmennManager.getCurrentLeikmadur(),
                leikmennManager.isAllLeikmadurGameOver());
        if (leikmennManager.isAllLeikmadurGameOver()) {
            allGameOver = true;
            checkIfAnyStand();
            return true;
        }
        checkIfOtherLeikmadurCanDo();
        return false;
    }

    private boolean checkIfCurrentLeikmadurGameOver() {
        Logger.debug("Check if {} gameOver: {}", leikmennManager.getCurrentLeikmadur(), (leikmennManager
                .getCurrentLeikmadur().isStand() || leikmennManager.getCurrentLeikmadur().getSamtals() > 21
                || (leikmennManager.getDealer().isGameOver() && leikmennManager.getCurrentLeikmadur().isGameOver()
                        && leikmennManager.getDealer().getSamtals() <= 21)));
        if (leikmennManager.getCurrentLeikmadur().isStand()) {
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
            checkIfAllGameOver();
            return true;
        }
        if (checkIfCurrentLeikmadurBust()) {
            return true;
        }
        if (checkIfCurrentLeikmadurWinOrLose()) {
            callWinOrLose();
            return true;
        }
        checkIfOtherLeikmadurCanDo();
        return false;
    }

    private boolean checkIfCurrentLeikmadurWinOrLose() {
        Logger.debug("Check if {} WinOrLose: {}", leikmennManager.getCurrentLeikmadur(),
                (leikmennManager.getCurrentLeikmadur().getSamtals() <= 21 && leikmennManager.getDealer().isGameOver()
                        && leikmennManager.getCurrentLeikmadur().isGameOver()
                        && leikmennManager.getDealer().getSamtals() <= 21));

        if (checkIfCurrentLeikmadurBust()) {
            return false;
        }

        // Checks if both player and dealer are finished
        // as well as checking wheater dealer has busted
        if (leikmennManager.getDealer().isGameOver()) {
            if (leikmennManager.getDealer().getSamtals() > 21) {
                leikmennManager.getCurrentLeikmadur().setGameOver(true);
                Logger.debug("dealer bust, called from: {}", leikmennManager.getCurrentLeikmadur());
                showLate = true;
                win();
                checkIfAllGameOver();
                return false;
            }
            if (leikmennManager.getCurrentLeikmadur().isGameOver()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfCurrentLeikmadurBust() {
        Logger.debug("Check if {} Bust", leikmennManager.getCurrentLeikmadur());
        // Checks if player bust
        if (leikmennManager.getCurrentLeikmadur().getSamtals() > 21) {
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
            lose(true);
            Logger.debug("{} busted", leikmennManager.getCurrentLeikmadur());
            if (allGameOver) {
                return true;
            }
            checkIfAllGameOver();
            return true;
        }
        return false;
    }

    private void callWinOrLose() {
        Logger.info("called WinOrLose from {}", leikmennManager.getCurrentLeikmadur());
        if (checkIfCurrentLeikmadurWinsDealer()) {
            Logger.info("{} wins", leikmennManager.getCurrentLeikmadur());
            win();
        } else {
            Logger.info("{} loses", leikmennManager.getCurrentLeikmadur());
            lose(false);
        }
        checkIfAllGameOver();
    }

    private boolean checkIfCurrentLeikmadurWinsDealer() {
        Logger.info("comparing {} to dealer: {}", leikmennManager.getCurrentLeikmadur(),
                leikmennManager.getDealer().getSamtals() <= leikmennManager.getCurrentLeikmadur().getSamtals());
        return leikmennManager.getDealer().getSamtals() <= leikmennManager.getCurrentLeikmadur().getSamtals();
    }

    private boolean checkIfOtherLeikmadurCanDo() {
        Logger.debug("Check if other Leikmadur than {} can do", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.isOtherLeikmadursStillCanDo()) {
            Logger.info("change called from checkIfOtherLeikmadurCanDo");
            disableUserActions(true);
            delay(1500, this::changeLeikmadur);
            return true;
        }
        delay(1500, this::changeRound);
        return false;
    }

    private boolean checkIfAnyStand() {
        Logger.debug("Check if any stand, called from {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getAnyStanding()) {
            if (!leikmennManager.getDealer().isGameOver()) {
                showLate = true;
            }
            checkIfCurrentLeikmadurStand();
            return true;
        }
        showPlayButtons(false);
        return false;
    }

    private boolean checkIfCurrentLeikmadurStand() {
        Logger.debug("Check if {} stand", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getCurrentLeikmadur().isStand()) {
            leikmennManager.getCurrentLeikmadur().setStand(false);
            checkDealerCanPlay();
            return true;
        }
        Logger.info("change called from checkIfCurrentLeikmadurStand");
        delay(1500, this::changeLeikmadur);
        delay(1500, this::checkIfAnyStand);
        return false;
    }

    private void changeRound() {
        Logger.info("Tried to change round, called from {}", leikmennManager.getCurrentLeikmadur());
        if (checkCanDo()) {
            return;
        }
        Logger.info("Changed round from {}", leikmennManager.getCurrentLeikmadur());
        leikmennManager.setAllLeikmennCanDo();
        Logger.info("Change called from changeRound");
        changeLeikmadur();
        checkDealerCanPlay();
    }

    private boolean checkDealerCanPlay() {
        Logger.debug("Check if dealer can play, called from {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getDealer().isGameOver()) {
            Logger.debug("dealerWasGameOver, with allGameOver = {}, called from {}", allGameOver,
                    leikmennManager.getCurrentLeikmadur());
            if (allGameOver && checkIfCurrentLeikmadurWinOrLose()) {
                callWinOrLose();
            }
            return false;
        }
        if (leikmennManager.getCurrentLeikmadur().getCondition() == "BUST") {
            return false;
        }
        if (leikmennManager.getDealer().getSamtals() < 17) {
            addCardToHand(true);
        }
        if (leikmennManager.getDealer().getSamtals() >= 17) {
            Logger.debug("dealer set gameOver, called from {}", leikmennManager.getCurrentLeikmadur());
            leikmennManager.getDealer().setGameOver(true);
        }
        if (leikmennManager.getDealer().getSamtals() > 21) {
            checkIfCurrentLeikmadurWinOrLose();
        }
        if (allGameOver) {
            Logger.debug("dealer check called again because allGameOver");
            checkDealerCanPlay();
        }
        return true;
    }

    /******************** HANDLES INPUT FROM JAVAFX BUTTONS ********************/

    /**
     * Keyrist þegar leikmaðurinn ýtir á HIT
     * Bætir spili við hendina hjá leikmanninum og athugar hvort leikmaðurinn hafi
     * sprungið
     * 
     * @param e
     */
    @FXML
    public void HitHandler() {
        defaultButtonFunctions();
        addCardToHand(false);
        if (leikmennManager.getCurrentLeikmadur().getSamtals() > 21) {
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
        }
    }

    /**
     * Keyrist þegar leikmaðurinn ýtir á STAND
     * Bætir spilum við hendina hjá dealerinum þangað til að dealerinn er komin með
     * samtals 17 eða hærra
     */
    @FXML
    public void StandHandler() {
        defaultButtonFunctions();
        leikmennManager.getCurrentLeikmadur().setStand(true);
        leikmadurGerir();
    }

    /**
     * Keyrist þegar leikmaðurinn ýtir á DOUBLE DOWN
     * Bætir spili við hendina hjá leikmanninum, setur dd = true,
     * athugar hvort leikmaðurinn hafi sprungið og keyrir síðan StandHandler() ef
     * leikmaðurinn sprakk ekki
     */
    @FXML
    public void DDHandler() {
        defaultButtonFunctions();
        leikmennManager.getCurrentLeikmadur().setStand(true);
        leikmennManager.getCurrentLeikmadur().setDd(true);
        fxCurrentBet.setText(String.valueOf(leikmennManager.getBetAmount() * 2));
        addCardToHand(false);
        // Breytir útreikningunum fyrir "bet" í lokinn
    }

    @FXML
    public void SurrenderHandler() {
        defaultButtonFunctions();
        leikmennManager.getCurrentLeikmadur().setGameOver(true);
        leikmennManager.getCurrentLeikmadur().setSurrendered(true);
        fxCurrentBet.setText(String.valueOf(leikmennManager.getBetAmount() * 0.5));
        lose(false);
        checkIfAllGameOver();
    }

    private void defaultButtonFunctions() {
        // Breyta því að leikmadurinn sem var að gera geti ekki lengur "surrender"-að
        leikmennManager.getCurrentLeikmadur().setCanDo(false);
        leikmennManager.getCurrentLeikmadur().setCanSurrender(false);
        // Felur surrender takkann ef Leikmadur getur ekki "surrender"-að
        hideSurrender(true);
    }

    /**
     * Keyrist þegar leikmaðurinn ýtir á QUIT
     * lokar glugganum og slekkur á java processinu
     */
    @FXML
    public void QuitHandler() {
        BlackjackApplication.getStigatafla().writeToFile();
        Platform.exit();
        System.exit(0);
    }

    /********** MAKES BUTTONS CHANGE COLOR WHEN PRESSED **********/

    /**
     * Keyrist þegar leikmaðurinn ýtir niður á einhvern af tökkunum neðst
     * breytir litnum á takkanum sem ýtt var á til að takarnir líti út fyrir
     * að vera takkar sem virka
     * 
     * @param e
     */
    @FXML
    public void ButtonColorChangerP(MouseEvent e) {
        Button hitButton = (Button) e.getSource();
        hitButton.setStyle("-fx-background-color: " + BUTTONPRESSEDCOLOR + ";" + BUTTONSTYLEPARAMS);
    }

    /**
     * Keyrist þegar leikmaðurinn sleppir einhverjum af tökkunum neðst
     * breytir litnum á takkanum sem sleppt var á til að takarnir líti út fyrir
     * að vera takkar sem virka
     * 
     * @param e
     */
    @FXML
    public void ButtonColorChangerR(MouseEvent e) {
        Button hitButton = (Button) e.getSource();
        hitButton.setStyle("-fx-background-color: " + BUTTONCOLOR + ";" + BUTTONSTYLEPARAMS);
    }

    /******************** ANIMATES CARDS ********************/

    /**
     * Helper-function til að bæta spili við hendina hjá völdum leikmanni og
     * byrjar að animata spilið í hendina hjá leikmanninum
     * 
     * @param leikmadur tekur inn leikmanninn sem bæta á spili við hendina hjá
     * @return skilar nýju samtals tölunni
     */
    private int addCardToHand(boolean isDealer) {
        // Slökkvum á user input frá neðri tökkunum
        disableUserActions(true);

        // Drögum spil og setjum í samtals
        SpilV card = leikmennManager.addCardToLeikmadur(isDealer);

        // Búum til nýtt spil út frá spil klasanum
        Spil cardView = new Spil(card);

        // Bætum spilinu við animationQueue-ið
        animationQueue[animationQueuePos] = cardView;

        // Setjum upphafs staðsetningu fyrir PathTransition
        MoveTo mt = new MoveTo(200, 122);

        // Setjum enda staðsetningu fyrir PathTransition
        // eftir því hvort delaerinn á að fá spilið eða leikmaðurinn
        LineTo lt;
        if (!isDealer) {
            lt = new LineTo(-700 + 40 * leikmennManager.getCurrentLeikmadur().getCardsInPlayPos(), 480);
            int offset = leikmennManager.getCardOffset(leikmennManager.getCurrentLeikmadur())
                    + leikmennManager.getCurrentLeikmadur().getCardsInPlayPos() - 1;
            leikmadurCards.add(offset, cardView);
        } else {
            lt = new LineTo(-700 + 40 * leikmennManager.getDealer().getCardsInPlayPos(), 130);
        }

        // Bætum upphafs og enda staðsetningu við Path og setjum það í sitt
        // animationQueue
        Path path = new Path();
        path.getElements().add(mt);
        path.getElements().add(lt);
        animationPathQueue[animationQueuePos] = path;

        // Keyrum síðan á animateCards sem keyrir PathTranstion
        animateCards(path, cardView, animationQueuePos++);
        return leikmennManager.getCurrentLeikmadur().getSamtals();
    }

    /**
     * Recursive function sem keyrir bíður eftir að animation klárist áður en það
     * byrjar
     * að animata næsta spil
     * Notar PathTransition frá JavaFX
     * 
     * @param path         tekurinn Path sem animationið á að fara eftir (hvort
     *                     spilið eigi að
     *                     lenda hjá leikmanni eða dealer og í hvaða röð spilið á að
     *                     vera)
     * @param toAnimate    spilið sem á að animatea
     * @param animationPos hvar í röðinni á animationQueueinu functionið á að vera
     *                     keyra á
     */
    private void animateCards(Path path, Node toAnimate, int animationPos) {
        // Ef animation er í gangi bíður það eftir að það klárist áður en functionið
        // keyrir aftur
        if (animationQueueBool) {
            animationQueueBool = false;

            // Bætir spilinu við gluggan
            mainGridPane.add(toAnimate, 3, 1);

            // PathTranstion animationið
            PathTransition pt = new PathTransition(Duration.millis(2000), path, toAnimate);

            // Þegar PathTransitionið er búið að keyra kíkjum við hvort nýtt spil sé í
            // animationQueueinu
            // ef svo er þá keyrum við animateCards aftur með næsta spilinu í
            // animationQueueinu
            // ef ekki sýnum við heildartölurnar
            // ef StandHandler var kallað keyrum við líka calculateWinner()
            pt.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    animationQueueBool = true;
                    if (animationQueue[animationPos + 1] != null) {
                        animateCards(animationPathQueue[animationPos + 1], animationQueue[animationPos + 1],
                                animationPos + 1);
                    } else {
                        fxDealerTotal.setText(Integer.toString(leikmennManager.getDealer().getSamtals()));
                        fxPlayerTotal.setText(Integer.toString(leikmennManager.getCurrentLeikmadur().getSamtals()));
                        leikmadurGerir();
                        if (showLate && leikmennManager.getCurrentLeikmadur().isGameOver()) {
                            bigTextVisibility();
                            showLate = false;
                            if (leikmennManager.isAllLeikmadurGameOver()) {
                                Logger.info("Change called from AnimateCards");
                                delay(1500, BlackjackController.this::changeLeikmadur);
                            }
                        }
                        disableUserActions(false);
                    }
                }
            });
            pt.play();
        }
    }

    /******************** SHOW VARIOUS SCREEN ELEMENTS ********************/

    /**
     * Helper-function sem keyrir þegar leikmaðurinn tapar og
     * uppfærir alla viðmótshluti til að sýna það
     * 
     * @param bust segir functioninu hvort leikmaðurinn hafi sprungið (BUST)
     */
    private void lose(boolean bust) {
        Logger.info("lose shown for {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getCurrentLeikmadur().getCondition() != "LOSE"
                && leikmennManager.getCurrentLeikmadur().getCondition() != "BUST") {
            leikmennManager.lose();
        }
        BlackjackApplication.getStigatafla().add(new Stig(leikmennManager.getCurrentLeikmadur().getNafn(),
                leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxBetTotal.setText(TOTAL + String.valueOf(leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxCurrentBet.setText(BET);
        if (bust) {
            leikmennManager.getCurrentLeikmadur().setCondition("BUST");
        } else {
            leikmennManager.getCurrentLeikmadur().setCondition("LOST");
        }
        if (!showLate) {
            bigTextVisibility();
        }
    }

    /**
     * Helper-function sem keyrir þegar leikmaðurinn vinnur og
     * uppfærir alla viðmótshluti til að sýna það
     */
    private void win() {
        Logger.info("win shown for {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getCurrentLeikmadur().getCondition() != "WIN") {
            leikmennManager.win();
        }
        BlackjackApplication.getStigatafla().add(new Stig(leikmennManager.getCurrentLeikmadur().getNafn(),
                leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxBetTotal.setText(TOTAL + String.valueOf(leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxCurrentBet.setText(BET);
        leikmennManager.getCurrentLeikmadur().setCondition("WIN");
        if (!showLate) {
            bigTextVisibility();
        }
    }

    private void disableUserActions(boolean disable) {
        fxPlayButtons.setDisable(disable);
        fxStopButtons.setDisable(disable);
        fxSurrenderButton.setDisable(disable);
        if (disable) {
            for (Node button : fxPlayButtons.getChildren()) {
                button.setOpacity(0.8);
                button.setStyle("-fx-background-color: " + BUTTONPRESSEDCOLOR + ";" + BUTTONSTYLEPARAMS);
            }
            for (Node button : fxStopButtons.getChildren()) {
                button.setOpacity(0.8);
                button.setStyle("-fx-background-color: " + BUTTONPRESSEDCOLOR + ";" + BUTTONSTYLEPARAMS);
            }
            fxSurrenderButton.setOpacity(0.8);
            fxSurrenderButton.setStyle("-fx-background-color: " + BUTTONPRESSEDCOLOR + ";" + BUTTONSTYLEPARAMS);
            return;
        }
        for (Node button : fxPlayButtons.getChildren()) {
            button.setOpacity(1);
            button.setStyle("-fx-background-color: " + BUTTONCOLOR + ";" + BUTTONSTYLEPARAMS);
        }
        for (Node button : fxStopButtons.getChildren()) {
            button.setOpacity(1);
            button.setStyle("-fx-background-color: " + BUTTONCOLOR + ";" + BUTTONSTYLEPARAMS);
        }
        fxSurrenderButton.setOpacity(1);
        fxSurrenderButton.setStyle("-fx-background-color: " + BUTTONCOLOR + ";" + BUTTONSTYLEPARAMS);
    }

    /**
     * Helper-function sem uppfærir textan í miðjunni á skjánum
     * 
     * @param visible hvort sýna eða fela eigi textann
     * @param text    textinn sem við viljum að birtist
     */
    private void bigTextVisibility() {
        boolean visible = true;
        if (leikmennManager.getCurrentLeikmadur().getCondition() == "") {
            visible = false;
        }
        for (Node node : fxPlayerPane.getChildren()) {
            Text temp = (Text) node;
            temp.setText(leikmennManager.getCurrentLeikmadur().getCondition());
            temp.setVisible(visible);
        }
    }

    /**
     * breytir tökkunum niðri á milli hvort leikmaðurinn
     * sé að spila eða ef leikurinn er búinn
     * 
     * @param play hvort sýna eigi play takkana eða
     */
    private void showPlayButtons(boolean play) {
        if (play) {
            Logger.info("Displayed play buttons, called from {}", leikmennManager.getCurrentLeikmadur());
            fxStopButtons.setPrefWidth(0);
            fxStopButtons.setVisible(false);
            fxPlayButtons.setPrefWidth(1280);
            fxPlayButtons.setVisible(true);
            ((HBox) fxSurrenderButton.getParent()).setPrefWidth(320);
            ((HBox) fxSurrenderButton.getParent()).setVisible(true);
        } else {
            Logger.info("Displayed continue buttons, called from {}", leikmennManager.getCurrentLeikmadur());
            fxPlayButtons.setPrefWidth(0);
            fxPlayButtons.setVisible(false);
            fxStopButtons.setPrefWidth(1280);
            fxStopButtons.setVisible(true);
            ((HBox) fxSurrenderButton.getParent()).setPrefWidth(0);
            ((HBox) fxSurrenderButton.getParent()).setVisible(false);
        }

    }

    private void hideSurrender(boolean hide) {
        if (hide) {
            fxPlayButtons.setPrefWidth(1280);
            for (Node button : fxPlayButtons.getChildren()) {
                ((Button) button).setPrefWidth(1280 / 3);
            }
        } else {
            fxPlayButtons.setPrefWidth(960);
            for (Node button : fxPlayButtons.getChildren()) {
                ((Button) button).setPrefWidth(320);
            }
        }
    }

    /******************** CHANGEING PLAYER AFTER SET TIME ********************/

    private static void delay(long millis, Runnable toRunAfterDelay) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> toRunAfterDelay.run());
        new Thread(sleeper).start();
    }

    private void changeLeikmadur() {
        if (leikmennManager.getAmountOfLeikmen() == 1) {
            return;
        }
        Logger.info("Changed player from: {}", leikmennManager.getCurrentLeikmadur());

        // Fela spil leikmannsins sem var að gera
        int offset = leikmennManager.getCardOffset(leikmennManager.getCurrentLeikmadur());
        for (int i = 0; i < leikmennManager.getCurrentLeikmadur().getCardsInPlayPos(); i++) {
            leikmadurCards.get(i + offset).setVisible(false);
        }

        // Skipta um leikmann
        leikmennManager.changeLeikmadur();
        if ((leikmennManager.getCurrentLeikmadur().isStand() || leikmennManager.getCurrentLeikmadur().isGameOver())
                && !allGameOver) {
            changeLeikmadur();
        }
        Logger.info("Changed player to: {}", leikmennManager.getCurrentLeikmadur());

        // Sýna spilin hjá leikmanninum sem var verið að breyta yfir í
        offset = leikmennManager.getCardOffset(leikmennManager.getCurrentLeikmadur());
        for (int i = leikmennManager.getCurrentLeikmadur().getCardsInPlayPos(); i > 0; i--) {
            leikmadurCards.get(i + offset - 1).setVisible(true);
        }

        // Setja JavaFX gildin fyrir nýja notandan
        fxPlayerName.setText(leikmennManager.getCurrentLeikmadur().getNafn());
        fxBetTotal.setText(TOTAL + String.valueOf(leikmennManager.getCurrentLeikmadur().getBetTotal()));
        fxPlayerTotal.setText(Integer.toString(leikmennManager.getCurrentLeikmadur().getSamtals()));
        disableUserActions(false);
        bigTextVisibility();

        if (leikmennManager.getCurrentLeikmadur().isCanSurrender()) {
            newLeikmadurGame();
        }
    }

    /******************** CALLED WHEN STATRING THE GAME ********************/

    /**
     * Keyrist þegar leikurinn byrjar
     * býr til og sýnir TextInputDialog til að spyrja Leikmanninn til nafns og býr
     * síðan til leikmennina tvö (dealer og leikmadur) og setur nafnið sem hann fékk
     * frá Dialoginu í Leikmadur leikmanninn
     * frumstillir síðan bet og keyrir NewGameHandler() til að byrja leikinn
     * 
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LeikmennDialog leikmennDialog = new LeikmennDialog();
        String[] response = leikmennDialog.showDialog();
        if (response == null) {
            QuitHandler();
            return;
        }
        if (response.length == 2) {
            leikmennManager = new LeikmennManager(new Leikmadur[] { new Leikmadur(fxPlayerPane, response[0]) },
                    Integer.valueOf(response[1]), new Leikmadur(fxDealerPane, "Dealer"));
        }
        if (response.length == 3) {
            leikmennManager = new LeikmennManager(
                    new Leikmadur[] { new Leikmadur(fxPlayerPane, response[0]),
                            new Leikmadur(fxDealerPane, response[1]) },
                    Integer.valueOf(response[2]), new Leikmadur(fxDealerPane, "Dealer"));
        }
        NewGameHandler();
    }
}
