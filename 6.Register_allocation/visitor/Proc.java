package visitor;
import java.util.*;

class WrapperPair {
    Pair range;
    String temp;
    String status;
    WrapperPair(int s, int e, String name) {
        range = new Pair(s, e);
        temp = name;
    }
}

class RegInfo {
    String name;
    boolean status; //false if busy
    RegInfo(String nm, boolean s) {
        name = nm;
        status = s;
    } 
}

public class Proc {
    String proc_name;
    int params;
    int stack_space;
    int curr_stk;
    int max_arg;
    boolean is_spilled;
    ArrayList<BasicBlock> bbs = new ArrayList<>(); //building blocks
    HashSet<String> vars = new HashSet<>();
    HashMap<String, Pair> live_ranges = new HashMap<>();
    HashMap<String, Integer> lbl_map = new HashMap<>();
    ArrayList<ArrayList<Integer>> cfg = new ArrayList<>();
    ArrayList<RegInfo> free_regs = new ArrayList<>();
    HashMap<String, String> loc = new HashMap<>();
    ArrayList<WrapperPair> var_data = new ArrayList<>();

    Proc() {
        proc_name = "";
        params = 1;
        stack_space = 0;
        max_arg = 0;
        is_spilled = false;
        // for (int i = 0; i <= 3; i++) {
        //     free_regs.add(new RegInfo("a" + i, true));
        // }
        for (int i = 0; i <= 7; i++) {
            free_regs.add(new RegInfo("s" + i, true));
        }
        for (int i = 0; i <= 9; i++) {
            free_regs.add(new RegInfo("t" + i, true));
        }

    }

    public String toString() {
        return "Proc";
    }

    public String display() {
        return proc_name + "[" + params + "]" + "[" + stack_space + "]" + "[" + max_arg + "]";
    }

    public void print_lr() {
        System.err.println("Vars and live ranges:");
        for (var x: var_data) {
            System.err.println(x.temp + ": " + x.range.st + " - " + x.range.en);
        }
        System.err.println();
    }

    public void print_reg_alloc() {
        System.err.println("Vars and loc:");
        for (var x: loc.entrySet()) {
            System.err.println(x.getKey() + ": " + x.getValue());
        }
    }

    public void print_bbs() {
        int i = 0;
        for (var x: bbs) {
            System.err.println("======Block id:" + i + "=====");
            x.print();
            i++;
        }
    }

    public void print() {
        System.err.println(display());
        print_lr();
        print_reg_alloc();
        print_bbs();
    }
    
    public void set_nm(String name) {
        proc_name = name;
    }

    public void set_prm(int i) {
        params = i;
        curr_stk = i + 10 + 8; //this is zero indexed by the way, we are storing everything in stack bcz we need get 4 extra registers fr allocation
        stack_space = curr_stk;
    }

    public void spill(String temp) {
        //System.err.println(temp + " " + Integer.toString(curr_stk));
        loc.put(temp, "#" + Integer.toString(curr_stk));
        curr_stk++;
        stack_space = curr_stk;
    }

    public int find_reg() {
        int n = free_regs.size();
        for (int i = 0; i < n; i++) {
            if (free_regs.get(i).status) return i;
        }
        return -1;
    }

    public void restore_reg(String temp) { 
        String reg = loc.get(temp);
        int n = free_regs.size();
        for (int i = 0; i < n; i++) {
            if (free_regs.get(i).name.equals(reg)) {
                free_regs.get(i).status = true; //this is free again
                // loc.replace(temp, "dead");
                return;
            }
        }
    }

    public void put_in_reg(int i, String temp) {
        free_regs.get(i).status = false;
        loc.replace(temp, free_regs.get(i).name);
    }

    public void put_in_reg(String reg, String temp) { //will only be called for existent registers
        int n = free_regs.size(); 
        int idx = -1;
        for (int i = 0; i < n; i++) {
            if (free_regs.get(i).name.equals(reg)) {
                idx = i;
                put_in_reg(idx, temp);
                break;
            }
        }
    }

    public void set_arg(int i) {
        max_arg = i;
    }

    public void add_bb(BasicBlock bb) {
        bbs.add(bb);
    }

    public void add_lbl(String lbl, int i) {
        lbl_map.put(lbl, i);
    }

}
