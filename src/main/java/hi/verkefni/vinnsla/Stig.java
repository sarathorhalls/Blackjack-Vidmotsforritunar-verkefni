package hi.verkefni.vinnsla;

/******************************************************************************
 * Nafn : Sara Þórhallsdóttir
 * T-póstur: kgt2@hi.is
 *
 * Lýsing : Hlutur sem geymir nafn og stig leikmanns fyrir stigatöfluna
 *****************************************************************************/

public class Stig implements Comparable<Stig> {
    // Java Tilviksbreytur
    private String nafn;
    private int score;

    /**
     * Constructor
     * 
     * @param nafn  nafn leikmanns
     * @param score betTotal score leikmanns
     */
    public Stig(String nafn, int score) {
        this.nafn = nafn;
        this.score = score;
    }

    // Getters and Setters

    public String getNafn() {
        return nafn;
    }

    public int getScore() {
        return score;
    }

    public String toString() {
        return nafn + " " + score;
    }

    // Overriden Object functions

    @Override
    public int compareTo(Stig o) {
        return this.getScore() - o.getScore();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + score;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Stig other = (Stig) obj;
        if (score != other.score)
            return false;
        return true;
    }
}
