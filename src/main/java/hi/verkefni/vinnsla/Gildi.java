package hi.verkefni.vinnsla;

/******************************************************************************
 *  Nafn    : Ebba Þóra Hvannberg
 *  T-póstur: ebba@hi.is
 *
 *  Lýsing  : Upptalning á gildi spila. Gosi, Drottning og Kóngur gilda 10,
 *  ás gildir 11
 *
 *****************************************************************************/
public enum Gildi {
    TVISTUR(2),
    THRISTUR(3),
    FJARKI(4),
    FIMMA(5),
    SEXA(6),
    SJOA(7),
    ATTA(8),
    NIA(9),
    TIA(10),
    GOSI(10),
    DROTTNING(10),
    KONGUR(10),
    AS(11);

    private final int gildi;

    Gildi(int gildi) {
        this.gildi = gildi;
    }

    int getGildi() {
        return gildi;
    }

    public String toString() {
        return ordinal () <9 ? String.valueOf(gildi) :name().substring(0,1);
    }
}
