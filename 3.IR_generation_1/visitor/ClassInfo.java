package visitor;
import java.util.*;

public class ClassInfo {
    public HashMap<String, Integer> u_vtable = new HashMap<>();
    public HashMap<String, String> uvt_type = new HashMap<>();
    public HashMap<String, Integer> u_contents = new HashMap<>();
    public HashMap<String, String> uc_type = new HashMap<>();
    public HashMap<String, Integer> l_vtable = new HashMap<>();
    public HashMap<String, String> lvt_type = new HashMap<>();
    public HashMap<String, Integer> l_contents = new HashMap<>();
    public HashMap<String, String> lc_type = new HashMap<>();
    String parent = null;
    String name = null;
    int f_index = 1; //0 is vtable by default
    int m_index = 0;

    public void add_field(String fld, String type) { //this is only for variables
        l_contents.put(name + "@" + fld, f_index);
        lc_type.put(name + "@" + fld, type);
        f_index++;
    }

    public void add_method(String mtd, String type) {
        l_vtable.put(name + "@" + mtd, m_index);
        lvt_type.put(name + "@" + mtd, type);
        m_index++;
    }

    public int get_findex(String fld) { //called only on global fields
        return u_contents.get(fld);
    }

    public int get_mindex(String mtd) {
        return u_vtable.get(mtd);
    }

    public String getP() {
        return parent;
    }

    public String toString() {
        return name;
    }

    public void setType(String var_name, String type) {
        uc_type.put(name + "@" + var_name, type);
    }
}


