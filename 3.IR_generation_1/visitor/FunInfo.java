package visitor;
import java.util.*;

public class FunInfo {
    HashMap<String, Integer> parameters = new HashMap<>();
    HashMap<String, String> p_type = new HashMap<>();
    HashMap<String, Integer> variables = new HashMap<>();
    HashMap<String, String> v_type = new HashMap<>();
    ArrayList<String> signature = new ArrayList<>();
    String class_name = null;
    String fun_name = null;
    public String ret = null;
    int p_index = 1;
    //int v_index = 0;

    public void addSig(String type) {
        signature.add(type);
    }
    public String getSig(int i) {
        return signature.get(i);
    }

    public FunInfo(String cls_name, String f_name) {
        class_name = cls_name;
        assign_fname(f_name);
    }

    public String getC() {
        return class_name;
    }

    public void assign_fname(String fnm) {
        fun_name = class_name + "@" + fnm;
    }

    public String toString() {
        return fun_name;
    }

    public void add_param(String param, String type) {
        parameters.put(fun_name + "@" + param, 4*p_index);
        p_type.put(fun_name + "@" + param, type);
        p_index++;
    }

    public void add_var(String var, String type) {
        variables.put(fun_name + "@" + var, 4*p_index);
        v_type.put(fun_name + "@" + var, type);
        p_index++;
    }

    public String type(String var) {
        var = fun_name + "@" + var;
        String tp = p_type.get(var);
        if (tp != null) return tp;
        tp = v_type.get(var);
        return tp;
    }

    public void setType(String var_name, String type) {
        var_name = fun_name + "@" + var_name;
        //System.out.println("aaaaaaaaaaaaaaaaaaaaa");
        if (variables.containsKey(var_name)) {
            v_type.put(var_name, type);
        }
        else if (parameters.containsKey(var_name)) {
            p_type.put(var_name, type);
        }
    }  
}
