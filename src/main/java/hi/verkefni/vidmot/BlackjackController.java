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

    // Java tilviksbreytur
    private LeikmennManager leikmennManager;
    private Leikmadur currentLeikmadur;
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
        currentLeikmadur = leikmennManager.newGame();
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
        fxBetTotal.setText(TOTAL + String.valueOf(currentLeikmadur.getBetTotal()));
        fxCurrentBet.setText(BET + String.valueOf(leikmennManager.getBetAmount()));

        // Uppfærir textan á skjánum fyrir samtals gildi spilana hjá dealerinum og
        // leikmanninum
        fxPlayerTotal.setText("0");

        // Setur nafn Leikmannsins
        fxPlayerName.setText(currentLeikmadur.getNafn());

        // Gefur leikmanninum tvö spil
        addCardToHand(false);
        addCardToHand(false);
        disableUserActions(true);
    }

    // private void newRound() {
    // if (leikmennManager.getDealer().getSamtals() < 17) {
    // addCardToHand(true);
    // fxDealerTotal.setText(Integer.toString(leikmennManager.getDealer().getSamtals()));
    // if (leikmennManager.getAllStanding()) {
    // newRound();
    // }
    // } else if (leikmennManager.getDealer().getSamtals() > 21) {
    // compareToDealer = true;
    // } else {
    // System.out.println("correctTriger");
    // compareToDealer = true;
    // }
    // canChangeRound = false;
    // }

    /****************************** STATE MANAGEMENT ******************************/

    private void leikmadurGerir() {
        /*
         * if (!checkCanDo() && !leikmennManager.getCurrentLeikmadur().isGameOver()) {
         * checkIfAllGameOver();
         * return;
         * }
         */
        checkIfCurrentLeikmadurGameOver();
    }

    private boolean checkCanDo() {
        Logger.debug("Check if {} can do", leikmennManager.getCurrentLeikmadur());
        return leikmennManager.getCurrentLeikmadur().isCanDo();
    }

    private boolean checkIfAllGameOver() {
        Logger.debug("Check if all gameOver, called from {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.isGamefinished()) {
            checkIfAnyStand();
            return true;
        }
        checkIfOtherLeikmadurCanDo();
        return false;
    }

    private boolean checkIfCurrentLeikmadurGameOver() {
        Logger.debug("Check if {} gameOver", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getCurrentLeikmadur().isStand()) {
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
            checkIfAllGameOver();
            return true;
        }
        if (checkIfCurrentLeikmadurWinOrLose()) {
            return true;
        }
        checkIfOtherLeikmadurCanDo();
        return false;
    }

    private boolean checkIfCurrentLeikmadurWinOrLose() {
        Logger.debug("Check if {} WinOrLose", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getCurrentLeikmadur().getSamtals() > 21) {
            lose(true);
            Logger.debug("{} busted", leikmennManager.getCurrentLeikmadur());
            if (allGameOver) {
                return true;
            }
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
            checkIfAllGameOver();
            return true;
        }
        if (leikmennManager.getDealer().isGameOver()) {
            if (leikmennManager.getDealer().getSamtals() > 21) {
                win();
                checkIfAllGameOver();
                return true;
            }
            compareCurrentLeikmadurToDealer();
            checkIfAllGameOver();
            if (allGameOver) {
                return true;
            }
            leikmennManager.getCurrentLeikmadur().setGameOver(true);
            return true;
        }
        return false;
    }

    private void compareCurrentLeikmadurToDealer() {
        Logger.info("comparing {} to dealer", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getDealer().getSamtals() > leikmennManager.getCurrentLeikmadur().getSamtals()) {
            lose(false);
            return;
        }
        win();
    }

    private boolean checkIfOtherLeikmadurCanDo() {
        Logger.debug("Check if other Leikmadur than {} can do", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.isOtherLeikmadursStillCanDo()) {
            changeLeikmadur();
            return true;
        }
        changeRound();
        return false;
    }

    private boolean checkIfAnyStand() {
        Logger.debug("Check if any stand, called from {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getAnyStanding()) {
            showLate = true;
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
            allGameOver = true;
            checkDealerCanPlay();
            return true;
        }
        changeLeikmadur();
        checkIfCurrentLeikmadurStand();
        return false;
    }

    private void changeRound() {
        if (checkCanDo()) {
            return;
        }
        Logger.info("Changed round from {}", leikmennManager.getCurrentLeikmadur());
        leikmennManager.setAllLeikmennCanDo();
        checkDealerCanPlay();
    }

    private boolean checkDealerCanPlay() {
        Logger.debug("Check if dealer can play, called from {}", leikmennManager.getCurrentLeikmadur());
        if (leikmennManager.getDealer().isGameOver()) {
            Logger.debug("dealerWasGameOver, with allGameOver = {}, called from {}", allGameOver,
                    leikmennManager.getCurrentLeikmadur());
            if (allGameOver) {
                checkIfCurrentLeikmadurWinOrLose();
            }
            return false;
        }
        if (leikmennManager.getDealer().getSamtals() < 17) {
            addCardToHand(true);
        }
        if (leikmennManager.getDealer().getSamtals() >= 17) {
            Logger.debug("dealer set gameOver, called from {}", leikmennManager.getCurrentLeikmadur());
            leikmennManager.getDealer().setGameOver(true);
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
        leikmennManager.getCurrentLeikmadur().setCanDo(false);
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
        leikmennManager.getCurrentLeikmadur().setStand(true);
        leikmennManager.getCurrentLeikmadur().setCanDo(false);
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
        leikmennManager.getCurrentLeikmadur().setCanDo(false);
        leikmennManager.getCurrentLeikmadur().setStand(true);
        leikmennManager.getCurrentLeikmadur().setDd(true);
        fxCurrentBet.setText(String.valueOf(leikmennManager.getBetAmount() * 2));
        addCardToHand(false);
        // Breytir útreikningunum fyrir "bet" í lokinn
    }

    @FXML
    public void SurrenderHandler() {
        leikmennManager.getCurrentLeikmadur().setCanDo(false);
        leikmennManager.getCurrentLeikmadur().setGameOver(true);
        // TODO: figure this shit out
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
            lt = new LineTo(-700 + 40 * currentLeikmadur.getCardsInPlayPos(), 480);
            int offset = leikmennManager.getCardOffset(currentLeikmadur) + currentLeikmadur.getCardsInPlayPos() - 1;
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
        return currentLeikmadur.getSamtals();
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
                        disableUserActions(false);
                        leikmadurGerir();
                        if (showLate && leikmennManager.getCurrentLeikmadur().isGameOver()) {
                            bigTextVisibility();
                            showLate = false;
                        }
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
        // currentLeikmadur.setCanDo(false);
        // currentLeikmadur.setStand(true);
        leikmennManager.lose();
        BlackjackApplication.getStigatafla().add(new Stig(currentLeikmadur.getNafn(), currentLeikmadur.getBetTotal()));
        fxBetTotal.setText(TOTAL + String.valueOf(currentLeikmadur.getBetTotal()));
        fxCurrentBet.setText(BET);
        if (bust) {
            currentLeikmadur.setCondition("BUST");
        } else {
            currentLeikmadur.setCondition("LOST");
        }
        if (!showLate) {
            bigTextVisibility();
        }
        // delay(3000, this::changeLeikmadur);
    }

    /**
     * Helper-function sem keyrir þegar leikmaðurinn vinnur og
     * uppfærir alla viðmótshluti til að sýna það
     */
    private void win() {
        // currentLeikmadur.setCanDo(false);
        // currentLeikmadur.setStand(true);
        leikmennManager.win();
        BlackjackApplication.getStigatafla().add(new Stig(currentLeikmadur.getNafn(), currentLeikmadur.getBetTotal()));
        fxBetTotal.setText(TOTAL + String.valueOf(currentLeikmadur.getBetTotal()));
        fxCurrentBet.setText(BET);
        currentLeikmadur.setCondition("WIN");
        if (!showLate) {
            bigTextVisibility();
        }
        /*
         * if (leikmennManager.isGamefinished()) {
         * showPlayButtons(false);
         * return;
         * }
         * delay(3000, this::changeLeikmadur);
         */
    }

    private void disableUserActions(boolean disable) {
        fxPlayButtons.setDisable(disable);
        if (disable) {
            for (Node button : fxPlayButtons.getChildren()) {
                button.setOpacity(0.8);
                button.setStyle("-fx-background-color: " + BUTTONPRESSEDCOLOR + ";" + BUTTONSTYLEPARAMS);
            }
            return;
        }
        for (Node button : fxPlayButtons.getChildren()) {
            button.setOpacity(1);
            button.setStyle("-fx-background-color: " + BUTTONCOLOR + ";" + BUTTONSTYLEPARAMS);
        }
    }

    /**
     * Helper-function sem uppfærir textan í miðjunni á skjánum
     * 
     * @param visible hvort sýna eða fela eigi textann
     * @param text    textinn sem við viljum að birtist
     */
    private void bigTextVisibility() {
        boolean visible = true;
        if (currentLeikmadur.getCondition() == "") {
            visible = false;
        }
        for (Node node : fxPlayerPane.getChildren()) {
            Text temp = (Text) node;
            temp.setText(currentLeikmadur.getCondition());
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
            fxStopButtons.setPrefWidth(0);
            fxStopButtons.setVisible(false);
            fxPlayButtons.setPrefWidth(1280);
            fxPlayButtons.setVisible(true);
        } else {
            fxPlayButtons.setPrefWidth(0);
            fxPlayButtons.setVisible(false);
            fxStopButtons.setPrefWidth(1280);
            fxStopButtons.setVisible(true);
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
        // TODO: rewrite
        currentLeikmadur.setCanSurrender(false);
        /*
         * if ((leikmennManager.isRoundFinished()) && changeRound) {
         * // newRound();
         * }
         * if (leikmennManager.getAllStanding() && !compareToDealer) {
         * // newRound();
         * return;
         * }
         * if (leikmennManager.isGamefinished()) {
         * return;
         * }
         */
        // Sýna spilinn hjá leikmanninum sem var að gera í smá tíma áður en skipt er
        // yfir
        // Fela spil leikmannsins sem var að gera
        int offset = leikmennManager.getCardOffset(currentLeikmadur);
        for (int i = 0; i < currentLeikmadur.getCardsInPlayPos(); i++) {
            leikmadurCards.get(i + offset).setVisible(false);
        }

        // Skipta um leikmann
        currentLeikmadur = leikmennManager.changeLeikmadur();
        /*
         * if ((!currentLeikmadur.isCanDo() || currentLeikmadur.isStand()) &&
         * !leikmennManager.getAllStandingAndCanDo()
         * && !compareToDealer) {
         * changeLeikmadur();
         * return;
         * }
         */

        // Sýna spilin hjá leikmanninum sem var verið að breyta yfir í
        offset = leikmennManager.getCardOffset(currentLeikmadur);
        for (int i = currentLeikmadur.getCardsInPlayPos(); i > 0; i--) {
            leikmadurCards.get(i + offset - 1).setVisible(true);
        }
        fxPlayerName.setText(currentLeikmadur.getNafn());
        fxBetTotal.setText(TOTAL + String.valueOf(currentLeikmadur.getBetTotal()));
        fxPlayerTotal.setText(Integer.toString(currentLeikmadur.getSamtals()));
        bigTextVisibility();
        if (currentLeikmadur.isCanSurrender()) {
            newLeikmadurGame();
        }
        /*
         * if (compareToDealer && currentLeikmadur.isStand()) {
         * calculateWinner();
         * }
         */
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
