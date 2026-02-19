package visitor;
import java.util.*;

public class ClassInfo {
    public HashMap<String, String> fields = new HashMap<>(); //field to type
    public HashMap<String, FunInfo> methods = new HashMap<>(); //function to function-data
    public Integer start_time = null;
    public Integer end_time = null;
    String parent = null;

    //====about parents====//
    public void setP(String p) {
        parent = p;
    }
    public String getP() {
        return parent;
    }
    
    //===Variables===//
    public boolean varPres(String var) {
        return fields.containsKey(var);
    }
    public String varType(String var) {
        if (varPres(var)) return fields.get(var);
        else return null;
    }

    public boolean methodPres(String fun, ArrayList<String> signature) {
        if (!methods.containsKey(fun)) return false;
        if (!signature.equals(methods.get(fun).signature)) return false;
        return true;
    }
    
    public boolean addVar(String var, String type) {
        if (varPres(var)) return false;
        else {
            fields.put(var, type);
            return true;
        }
    }

    //===Functions===//
    //for first two, fun is <function name>@<function signature>
    //for the third, fun is <function name>
    public boolean funPres(String fun) {
        return methods.containsKey(fun);
    }
    public String funRet(String fun) {
        if (varPres(fun)) return methods.get(fun).return_type;
        else return null;
    }
    public boolean addFun(String fun, FunInfo info) {
        if (funPres(fun)) {
            return false; //since there is no overloading, we need not check the signatures
        }
        else {
            methods.put(fun, info);
            return true;
        }
    }
}


