package hi.verkefni.vinnsla;

public class LeikmennManager {
    private Leikmadur dealer;
    private Leikmadur[] leikmenn;
    private Stokkur deck;
    private int betAmount;
    private int currentlySelected;

    public LeikmennManager(Leikmadur[] players, int betAmount, Leikmadur dealer) {
        this.dealer = dealer;
        leikmenn = new Leikmadur[players.length];
        for (int i = 0; i < players.length; i++) {
            leikmenn[i] = players[i];
        }
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

    public SpilV addCardToPlayer(boolean isDealer) {
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

    public Leikmadur changePlayer() {
        if (++currentlySelected >= leikmenn.length) {
            currentlySelected = 0;
        }
        return leikmenn[currentlySelected];
    }

    // Getters and setters
    public boolean isGamefinished() {
        for (Leikmadur leikmadur : leikmenn) {
            if (leikmadur.isCanDo()) {
                return false;
            }
        }
        return true;
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
        System.err.println("counter overflow, leikmadur not found");
        return counter;
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
}
