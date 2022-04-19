package hi.verkefni.vinnsla;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : innleiðing á LeikmaðurInterface
 *****************************************************************************/

import javafx.scene.layout.Pane;

public class Leikmadur implements LeikmadurInterface {

    // Tilviksbreytur
    private SpilV[] cardsInPlay;
    private int cardsInPlayPos;
    private int cardTotal;
    private int betTotal;
    private String name;
    private String condition;
    private Pane cardPane;
    private boolean canSurrender;
    private boolean canDo;
    private boolean gameOver;
    private boolean stand;
    private boolean dd;
    private boolean surrendered;

    /**
     * Constructor
     * 
     * @param cardPane Pane-ið sem spilinn eiga að lenda á
     * @param name     nafnið á leikmanninum
     */
    public Leikmadur(Pane cardPane, String name) {
        this.name = name;
        this.setCondition("");
        this.cardPane = cardPane;
        setCanDo(true);
        this.betTotal = 1000;
    }

    /**
     * Consturctor ef leikmaður er ekki með nafn
     * 
     * @param cardPane Pane-ið sem spilinn eiga að lenda
     */
    public Leikmadur(Pane cardPane) {
        this(cardPane, "");
    }

    /**
     * gefur leikmanninum spil
     * 
     * @param spilV spilið sem gefa á leikmanninum
     */
    public void gefaSpil(SpilV spilV) {
        cardsInPlay[cardsInPlayPos++] = spilV;

        // Ef leikmaðurinn fær ás á hendi þegar hann er með meira en 11 samtals
        // gefum við honum 1 í staðinn fyrir 11
        if (cardTotal > 11 && spilV.getGildi() == Gildi.AS) {
            cardTotal += 1;
            return;
        }
        cardTotal += spilV.getGildi().getGildi();
    }

    /**
     * skilar hvort dealer vinnur
     * 
     * @param player
     * @return hvort dealerinn hafi unnið
     */
    public boolean vinnurDealer(LeikmadurInterface player) {
        return this.cardTotal > player.getSamtals() && this.cardTotal < 22;
    }

    /**
     * keyrir vinnurDealer á leikmanninn og skilar leikmanninum sem vann
     * 
     * @param player
     * @return leikmaurinn sem vann
     */
    public LeikmadurInterface hvorVann(LeikmadurInterface player) {
        if (!vinnurDealer(player)) {
            return player;
        }
        return this;
    }

    /**
     * frumstillir allar tilviksbreytur
     */
    public void nyrLeikur() {
        cardsInPlay = new SpilV[11];
        condition = "";
        cardsInPlayPos = 0;
        cardTotal = 0;
        canSurrender = true;
        canDo = true;
        gameOver = false;
        dd = false;
        stand = false;
        surrendered = false;
    }

    /**
     * Breytir samtals fyrir leikmanninn.
     * breytir einnig veðinu um 2 eða 0,5 ef leikmaður DDaði eða surrenderaði
     * 
     * @param change hversu mikið á að breyta samtals um
     */
    public void changeTotal(int change) {
        if (dd) {
            betTotal += 2 * change;
            return;
        }
        if (surrendered) {
            betTotal += (int) (0.5 * change);
            return;
        }
        betTotal += change;
    }

    // Getters and setters

    /**
     * Skilar hversu mörg spil leikmaðurinn er kominn með
     * 
     * @return fjöldi spila
     */
    public int getCardsInPlayPos() {
        return cardsInPlayPos;
    }

    /**
     * @return skilar samtals tölunni
     */
    public int getSamtals() {
        return cardTotal;
    }

    /**
     * @return skilar nafn leikmansins
     */
    public String getNafn() {
        return name;
    }

    /**
     * setur nafið á leikmanninum
     * 
     * @param s nafnið sem skýra á leikmanninn
     */
    public void setNafn(String s) {
        this.name = s;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * @return skilar nýju array með öllum spilum í hendinni
     */
    public SpilV[] getCardsInPlay() {
        SpilV[] finalA = new SpilV[cardsInPlayPos];
        System.arraycopy(cardsInPlay, 0, finalA, 0, cardsInPlay.length);
        return finalA;
    }

    /**
     * @return skilar Pane leikmannsins
     */
    public Pane getPane() {
        return this.cardPane;
    }

    public boolean isCanSurrender() {
        return canSurrender;
    }

    public void setCanSurrender(boolean canSurrender) {
        this.canSurrender = canSurrender;
    }

    public boolean isCanDo() {
        return canDo;
    }

    public void setCanDo(boolean canDo) {
        this.canDo = canDo;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getBetTotal() {
        return betTotal;
    }

    public void setDd(boolean dd) {
        this.dd = dd;
    }

    public boolean isStand() {
        return stand;
    }

    public void setStand(boolean stand) {
        this.stand = stand;
    }

    public String toString() {
        return this.name;
    }

    public void setSurrendered(boolean surrendered) {
        this.surrendered = surrendered;
    }
}
