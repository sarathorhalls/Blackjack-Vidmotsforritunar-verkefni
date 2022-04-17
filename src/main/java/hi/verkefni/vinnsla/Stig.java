package hi.verkefni.vinnsla;

public class Stig implements Comparable<Stig> {
    private String nafn;
    private int score;

    public Stig(String nafn, int score) {
        this.nafn = nafn;
        this.score = score;
    }

    public String getNafn() {
        return nafn;
    }

    public int getScore() {
        return score;
    }

    public String toString() {
        return nafn + " " + score;
    }

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
