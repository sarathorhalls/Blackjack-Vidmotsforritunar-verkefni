package hi.verkefni.vinnsla;

/******************************************************************************
 *  Nafn    : Sara Þórhallsdóttir
 *  T-póstur: kgt2@hi.is
 *
 *  Lýsing  : Heldur utan um alla leikmenn sem eru í leiknum og sér um að
 *  skipta á milli þeirra
 *****************************************************************************/

import org.tinylog.Logger;

public class LeikmennManager {
    // Java Tilviksbreytur
    private Leikmadur dealer;
    private Leikmadur[] leikmenn;
    private Stokkur deck;
    private int betAmount;
    private int currentlySelected;

    /**
     * Constructor
     * 
     * @param leikmenn  Fylki með öllum notenda leikmönnunum
     * @param betAmount hversu mikið á að veðja í hvert skipti
     * @param dealer    dealerLeikmaðurinn
     */
    public LeikmennManager(Leikmadur[] leikmenn, int betAmount, Leikmadur dealer) {
        this.dealer = dealer;
        this.leikmenn = new Leikmadur[leikmenn.length];
        System.arraycopy(leikmenn, 0, this.leikmenn, 0, leikmenn.length);
        deck = new Stokkur();
        this.betAmount = betAmount;
        currentlySelected = 0;
    }

    /**
     * Núllstillir alla leikmenn og dealerinn
     * 
     * @return núverandi leikmaður
     */
    public Leikmadur newGame() {
        // Núllstillir leikmenn
        for (Leikmadur leikmadur : leikmenn) {
            leikmadur.nyrLeikur();
        }

        // Núllstillir dealer
        dealer.nyrLeikur();

        // Núllstillir leikmennManager
        currentlySelected = 0;
        return leikmenn[currentlySelected];
    }

    /**
     * Bætir spili valið af handhófi úr 6 stokkum við hendi hjá leikmanni eða dealer
     * 
     * @param isDealer hvort bæta eigi spilinu við hendina hjá delaerinum eða
     *                 núverandi leikmanni
     * @return skilar spilinu sem bætt var við hendina
     */
    public SpilV addCardToLeikmadur(boolean isDealer) {
        // TODO: handle if all 6 decks are finished

        // Dregur spil
        SpilV card = deck.dragaSpil();

        if (isDealer) {
            dealer.gefaSpil(card);
            return card;
        }
        leikmenn[currentlySelected].gefaSpil(card);
        return card;
    }

    /**
     * Dregur betamount af samtals hjá núverandi leikmanni
     */
    public void lose() {
        leikmenn[currentlySelected].changeTotal(-betAmount);
    }

    /**
     * bætir betamount við samtals hjá núverandi leikmanni
     */
    public void win() {
        leikmenn[currentlySelected].changeTotal(betAmount);
    }

    /**
     * Breytir um núverandi leikmann
     * 
     * @return skilar nýja núverandi leikmanni
     */
    public Leikmadur changeLeikmadur() {
        if (++currentlySelected >= leikmenn.length) {
            currentlySelected = 0;
        }
        return leikmenn[currentlySelected];
    }

    // Getters and setters

    public boolean isAllLeikmadurGameOver() {
        for (Leikmadur leikmadur : leikmenn) {
            if (!leikmadur.isGameOver()) {
                return false;
            }
        }
        return true;
    }

    public boolean isOtherLeikmadursStillCanDo() {
        for (Leikmadur leikmadur : leikmenn) {
            if (leikmadur == leikmenn[currentlySelected]) {
                continue;
            }
            if (leikmadur.isCanDo() && !leikmadur.isStand()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRoundFinished() {
        return leikmenn.length - 1 == currentlySelected;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public Leikmadur getDealer() {
        return dealer;
    }

    public Leikmadur getCurrentLeikmadur() {
        return leikmenn[currentlySelected];
    }

    public int getAmountOfLeikmen() {
        return leikmenn.length;
    }

    public int getCardOffset(Leikmadur leikmadur) {
        int counter = 0;
        for (Leikmadur testAgainstLeikmadur : leikmenn) {
            if (testAgainstLeikmadur.equals(leikmadur)) {
                return counter;
            }
            counter += testAgainstLeikmadur.getCardsInPlayPos();
        }
        Logger.error("counter overflow, leikmadur not found");
        return counter;
    }

    public boolean getAnyStanding() {
        for (Leikmadur leikmadur : leikmenn) {
            if (leikmadur.isStand()) {
                return true;
            }
        }
        return false;
    }

    public boolean getAllStanding() {
        for (Leikmadur leikmadur : leikmenn) {
            if (!leikmadur.isStand()) {
                return false;
            }
        }
        return true;
    }

    public void setAllLeikmennCanDo() {
        for (Leikmadur leikmadur : leikmenn) {
            if (!leikmadur.isStand()) {
                leikmadur.setCanDo(true);
            }
        }
    }
}
