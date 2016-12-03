package lambda.ast;

/**
 * Represents the constructor for tupels.
 */
public class TupelConstant {
    private int length;

    public TupelConstant(int length) {
        assert(length >= 0);
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TupelConstant that = (TupelConstant) o;

        return getLength() == that.getLength();

    }

    @Override
    public int hashCode() {
        return getLength();
    }

    @Override
    public String toString() {
        return "tupel_" + length;
    }
}
