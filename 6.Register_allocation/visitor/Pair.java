package visitor;

public class Pair {
    int st;
    int en;
    Pair() {
        st = Integer.MAX_VALUE;
        en = 0;
    }
    Pair(int f, int s) {
        st = f;
        en = s;
    }
}
