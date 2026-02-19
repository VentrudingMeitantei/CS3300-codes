package visitor;
import java.util.*;

public class FunInfo {
    public HashMap<String, String> parameters = new HashMap<>(); //parameters to types
    public HashMap<String, String> variables = new HashMap<>(); //variables to types
    public ArrayList<String> signature = new ArrayList<>(); 
    String return_type = null;
    public boolean isPar(String param) {
        return parameters.containsKey(param);
    }
    public boolean isVar(String v) {
        return variables.containsKey(v);
    }
    public boolean isPres(String c) {
        return (isPar(c) || isVar(c));
    }

    public boolean addPar(String param, String type) { 
       
        if (isPar(param)) return false;
        
        parameters.put(param, type);
        signature.add(type);
        return true;
    }
    public boolean addVar(String vari, String type) {
        if (isPres(vari)) {
            return false;
        }
        else {
            variables.put(vari, type);
            return true;
        }
    }
    public void setRet(String ret) {
        return_type = ret;
    }

    public String getRet() {
        return return_type;
    }
}
