package hi.verkefni.vinnsla;

/******************************************************************************
 *  Nafn    : Ebba Þóra Hvannberg
 *  T-póstur: ebba@hi.is
 *
 *  Lýsing  : Vinnsluklasi fyrir eitt mannspil. Hefur tegund (e. suit) og gildi (rank)
 *
 *
 *****************************************************************************/
public class SpilV {
    private final Tegund tegund;
    private final Gildi gildi;


    public SpilV(Tegund t, Gildi g) {
        tegund = t;
        gildi = g;
    }

    public Gildi getGildi() {
        return gildi;
    }

    /**
     * Heiltölu gildi á bak við spilið
     * @return heiltölugildi spils
     */
    public int getVirdi() {
        return gildi.getGildi();
    }

    public Tegund getTegund() {
        return tegund;
    }

    public String toString() {
        return gildi+" "+tegund.name();
    }
}
