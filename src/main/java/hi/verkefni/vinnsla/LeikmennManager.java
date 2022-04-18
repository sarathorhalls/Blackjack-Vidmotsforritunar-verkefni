package hi.verkefni.vinnsla;

import org.tinylog.Logger;

public class LeikmennManager {
    private Leikmadur dealer;
    private Leikmadur[] leikmenn;
    private Stokkur deck;
    private int betAmount;
    private int currentlySelected;

    public LeikmennManager(Leikmadur[] leikmenn, int betAmount, Leikmadur dealer) {
        this.dealer = dealer;
        this.leikmenn = new Leikmadur[leikmenn.length];
        System.arraycopy(leikmenn, 0, this.leikmenn, 0, leikmenn.length);
        deck = new Stokkur();
        this.betAmount = betAmount;
        currentlySelected = 0;
    }

    public Leikmadur newGame() {
        for (Leikmadur leikmadur : leikmenn) {
            leikmadur.nyrLeikur();
        }
        dealer.nyrLeikur();
        currentlySelected = 0;
        return leikmenn[currentlySelected];
    }

    public SpilV addCardToLeikmadur(boolean isDealer) {
        SpilV card = deck.dragaSpil();
        if (isDealer) {
            dealer.gefaSpil(card);
            return card;
        }
        leikmenn[currentlySelected].gefaSpil(card);
        return card;
    }

    public void lose() {
        leikmenn[currentlySelected].changeTotal(-betAmount);
    }

    public void win() {
        leikmenn[currentlySelected].changeTotal(betAmount);
    }

    public Leikmadur changeLeikmadur() {
        if (++currentlySelected >= leikmenn.length) {
            currentlySelected = 0;
        }
        return leikmenn[currentlySelected];
    }

    // Getters and setters
    public boolean isGamefinished() {
        for (Leikmadur leikmadur : leikmenn) {
            if (leikmadur.isGameOver()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOtherLeikmadursStillCanDo() {
        for (Leikmadur leikmadur : leikmenn) {
            if (leikmadur == leikmenn[currentlySelected]) {
                continue;
            }
            if (leikmadur.isCanDo()) {
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

    public boolean getAllStandingAndCanDo() {
        for (Leikmadur leikmadur : leikmenn) {
            if (!leikmadur.isStand() && leikmadur.isCanDo()) {
                return false;
            }
        }
        return true;
    }

    public void setAllLeikmennCanDo() {
        for (Leikmadur leikmadur : leikmenn) {
            leikmadur.setCanDo(true);
        }
    }
}
