package visitor;
import java.util.*;

public class BasicBlock {
    HashSet<String> used = new HashSet<>(); //the vairables used in the block
    HashSet<String> def = new HashSet<>(); //variables that have been defined in the block
    HashSet<String> live_in = new HashSet<>();
    HashSet<String> live_out = new HashSet<>();

    boolean is_cj = false;
    boolean is_j = false;
    boolean is_np = false;
    boolean is_er = false;
    boolean is_lbl = false;

    public String toString() {
        return "BasicBlock";
    }

    //for CJUMP
    String fls_jmp;
    //for JUMP
    String target;

    public void print() {
        if (is_cj) {
            System.err.println("CJUMP F: " + fls_jmp);
            System.err.println("======End=====\n");
            return;
        }
        if (is_j) {
            System.err.println("JUMP " + " t: " + target);
            System.err.println("======End=====\n");
            return;
        }
        if (is_er) {
            System.err.println("ERROR");
            System.err.println("======End=====\n");
            return;
        }
        if (is_np) {
            System.err.println("NOOP");
            System.err.println("======End=====\n");
            return;
        }
        if (is_lbl) {
            System.err.println("Label");
            System.err.println("======End=====\n");
            return;
        }

        if (used.isEmpty()) {
            System.err.println("No variables in use[]");
        }
        else {
            System.err.println("Variables in use[]");
            System.err.print("  ");
            for (String v: used) {
                System.err.print(v + " ");
            }
            System.err.println();
        }

        System.err.println("=====================");

        if (def.isEmpty()) {
            System.err.println("No variables in def[]");
        }
        else {
            System.err.println("Variables in def[]");
            System.err.print("  ");
            for (String v: def) {
                System.err.print(v + " ");
            }
            System.err.println();
        }

        System.err.println("=====================");

        if (live_in.isEmpty()) {
            System.err.println("No variables in in[]");
        }
        else {
            System.err.println("Variables in in[]");
            System.err.print("  ");
            for (String v: live_in) {
                System.err.print(v + " ");
            }
            System.err.println();
        }

        System.err.println("=====================");

        if (live_out.isEmpty()) {
            System.err.println("No variables in out[]");
        }
        else {
            System.err.println("Variables in out[]");
            System.err.print("  ");
            for (String v: live_out) {
                System.err.print(v + " ");
            }
            System.err.println();
        }

        System.err.println("======End=====\n");
    }

    public boolean is_used(String v) {
        return used.contains(v);
    }

    public void put_u(String v) {
        used.add(v);
    }

    public boolean is_def(String v) {
        return def.contains(v);
    }

    public void put_d(String v) {
        def.add(v);
    }

    public boolean is_li(String v) {
        return live_in.contains(v);
    }

    public void put_li(String v) {
        live_in.add(v);
    }

    public boolean is_lo(String v) {
        return live_out.contains(v);
    }

    public void put_lo(String v) {
        live_out.add(v);
    }
}
