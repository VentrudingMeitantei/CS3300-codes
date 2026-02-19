package visitor;
import syntaxtree.*;
import java.util.*;

class CodePair {
    String code;
    String reg;
    public String toString() {
        return "CodePair";
    }
    public CodePair(String cd, String rg) {
        code = cd;
        reg = rg;
    }
}

class EmitHelper {
    String store_callee_saved(int param) {
        String res = null;
        for (int i = 0; i <= 7; i++) {
            if (i == 0) res = "ASTORE SPILLEDARG " + (param + i) + " s" + i + "\n";
            else res += "ASTORE SPILLEDARG " + (param + i) + " s" + i + "\n";
        }
        return res;
    }

    String load_callee_saved(int param) {
        String res = null;
        for (int i = 0; i <= 7; i++) {
            if (i == 0) res = "ALOAD " +  "s" + i + " SPILLEDARG " + (param + i) + "\n";
            else res += "ALOAD " +  "s" + i + " SPILLEDARG " + (param + i) + "\n";
        }
        return res;
    }

    String store_caller_saved(int param) {
        String res = null;
        param += 8;
        for (int i = 0; i <= 9; i++) {
            if (i == 0) res = "ASTORE SPILLEDARG " + (param + i) + " t" + i + "\n";
            else res += "ASTORE SPILLEDARG " + (param + i) + " t" + i + "\n";
        }
        return res;
    }
    
    String load_caller_saved(int param) {
        String res = null;
        param += 8;
        for (int i = 0; i <= 9; i++) {
            if (i == 0) res = "ALOAD " +  "t" + i + " SPILLEDARG " + (param + i) + "\n";
            else res += "ALOAD " +  "t" + i + " SPILLEDARG " + (param + i) + "\n";
        }
        return res;
    }

    boolean isParam(String s, int params) {
        String[]parts = s.split(" ", 2);
        return Integer.parseInt(parts[1]) < params;
    } 

    boolean isSpilled(String s) {
        if (s == null || s.isEmpty()) return false;
        if (s.charAt(0) == '#') return true;
        return false;
    }

    boolean isLbl(String s) {
       if (s == null || s.isEmpty()) return false;
        if (s.charAt(0) == '@') return true;
        return false;
    }

    boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    String extract_addr(String s) {
        if (!isSpilled(s)) return null;
        return s.substring(1);
    }

    String extract_lbl(String s) {
        if (!isSpilled(s)) return null;
        return s.substring(1);
    }

}

public class CodeEmitter extends GJDepthFirst<Object, Object>{
   ArrayList<Proc> procedures = new ArrayList<>();
   EmitHelper emmet = new EmitHelper(); //Everything is Awesome!
   int proc_num = 0;
   public CodeEmitter(RegAlloc ra) {
      procedures = ra.procedures;
   }
    /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public Object visit(Goal n, Object argu) {
      Object res = null;
      res = procedures.get(proc_num).display() + "\n";
      res += (String)n.f1.accept(this, argu);
      res += "END\n";
      if (procedures.get(proc_num).is_spilled) res += "//SPILLED\n";
      else res += "//NOTSPILLED\n";
      if (n.f3 != null) {
        for (Enumeration<Node> e = n.f3.elements(); e.hasMoreElements();) {
            Node node = e.nextElement();
            res += (String)node.accept(this, argu);
        }
      }
      return res;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public Object visit(StmtList n, Object argu) {
      Object res = null;
      
      if (n.f0 != null) {
           for (Enumeration<Node> e = n.f0.elements(); e.hasMoreElements();) {
               Node node = e.nextElement();        // each is (Label()? Stmt())  
               NodeSequence seq = (NodeSequence) node;  
               NodeOptional maybeLabel = (NodeOptional) seq.elementAt(0);
               Proc curr_proc = procedures.get(proc_num);
               if (maybeLabel.present()) {
                   Label label = (Label) maybeLabel.node;
                   String lam_name = curr_proc.proc_name + "_" + (String)label.accept(this, argu);        // or call your logic directly
                   if (res == null) res = lam_name + "\n";
                   else res += lam_name + "\n";
               }  
               //Proc curr_proc = procedures.get(proc_num);
               Stmt stmt = (Stmt) seq.elementAt(1);
               if (res == null) res = (String)stmt.accept(this, curr_proc);             // or perform your operation
               else res += (String)stmt.accept(this, curr_proc);
           }
      }

      return res;
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public Object visit(Procedure n, Object argu) {
      Object res = null;
      proc_num++;
      Proc curr_proc = procedures.get(proc_num);
      res = curr_proc.display() + "\n";
      res += emmet.store_callee_saved(curr_proc.params);
      res += (String)n.f4.accept(this, curr_proc);
      res += emmet.load_callee_saved(curr_proc.params);
      res += "END\n";
      if (curr_proc.is_spilled) res += "//SPILLED\n";
      else res += "//NOTSPILLED\n";

      return res;
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public Object visit(Stmt n, Object argu) {
      Object res = null;
      res = (String)n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "NOOP"
    */
   public Object visit(NoOpStmt n, Object argu) {
      Object res = null;
      res = "NOOP\n";
      return res;
   }

   /**
    * f0 -> "ERROR"
    */
   public Object visit(ErrorStmt n, Object argu) {
      Object res = null;
      res = "ERROR\n";
      return res;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
   public Object visit(CJumpStmt n, Object argu) {
      Object res = null;
      String loc = (String)n.f1.accept(this, argu);
      if (emmet.isSpilled(loc)) {
        res = "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n"; 
        loc = "v0";
        res += "CJUMP ";
      }
      else {
        res = "CJUMP ";
      }
      res += loc + " ";
      res += ((Proc)argu).proc_name + "_" + (String)n.f2.accept(this, argu) + "\n";
      return res;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public Object visit(JumpStmt n, Object argu) {
      Object res = null;
      res = "JUMP ";
      res += ((Proc)argu).proc_name + "_" + (String)n.f1.accept(this, argu) + "\n";
      return res;
   }
   //some skill issue happening
   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
   public Object visit(HStoreStmt n, Object argu) {
      Object res = null;

      String loc1 = (String)n.f1.accept(this, argu);
      if (emmet.isSpilled(loc1)) {
        res = "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc1) + "\n";
        loc1 = "v0";
      }
      String offset = (String)n.f2.accept(this, argu);
      String loc2 = (String)n.f3.accept(this, argu);
      if (emmet.isSpilled(loc2)) {
        if (res == null) res = "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc2) + "\n";
        else res += "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc2) + "\n";
        loc2 = "v1";
      }

      if (res == null) res = "HSTORE ";
      else res += "HSTORE ";
      res += loc1 + " " + offset + " " + loc2 + "\n";

      return res;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
   public Object visit(HLoadStmt n, Object argu) {
      Object res = null;

      String loc1 = (String)n.f1.accept(this, argu);
      if (loc1.equals("Dead code")) return "";
      String write_back = "";
      if (emmet.isSpilled(loc1)) {
        res = "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc1) + "\n";
        write_back = "ASTORE SPILLEDARG " + emmet.extract_addr(loc1) + " v0\n";
        loc1 = "v0";
      }
      String loc2 = (String)n.f2.accept(this, argu);
      if (emmet.isSpilled(loc2)) {
        if (res == null) res = "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc2) + "\n";
        else res += "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc2) + "\n";
        loc2 = "v1";
      }
      String offset = (String)n.f3.accept(this, argu);
      if (res == null) res = "HLOAD ";
      else res += "HLOAD ";
      res += loc1 + " " + loc2 + " " + offset + "\n";
      res += write_back;
      return res;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public Object visit(MoveStmt n, Object argu) {
      Object res = null;
      String loc = (String)n.f1.accept(this, argu);
      if (loc.equals("Dead code")) return "";
      CodePair p = (CodePair)n.f2.accept(this, argu);
      String write_back = "";
      res = p.code;
      if (emmet.isSpilled(loc)) {
        if (!p.reg.equals("v0")) {
            res += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
            write_back = "ASTORE SPILLEDARG " + emmet.extract_addr(loc) + " v0\n";
            loc = "v0";
        }
        else { //this is the case of function call
            res += "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
            write_back = "ASTORE SPILLEDARG " + emmet.extract_addr(loc) + " v1\n";
            loc = "v1";
        }
      }
      res += "MOVE " + loc + " " + p.reg + "\n";
      res += write_back;
      return res;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public Object visit(PrintStmt n, Object argu) {
      Object res = null;
      String loc = (String)n.f1.accept(this, argu);
      if (emmet.isSpilled(loc)) {
        res = "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
        loc = "v0";
      }
      if (res != null) res += "PRINT " + loc + "\n"; 
      else res = "PRINT " + loc + "\n";
      return res;
   }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public Object visit(Exp n, Object argu) {
      Object res = null;
      res = n.f0.accept(this, argu);
      if (res.toString().equals("CodePair")) return res;
      else  { //this case it is a simple expression
        //System.err.println("entered this place");
        String loc = (String)res;
        if (loc.equals("Dead code")) return new CodePair("", "");
        String code = "";
        //System.err.println(loc);
        if (emmet.isSpilled(loc)) {
            code += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
            loc = "v0";
        }
        else if (emmet.isInteger(loc)) {
            code += "MOVE v0 " + loc + "\n";
            loc = "v0";
        }
        //System.err.print(code);
        res = new CodePair(code, loc);
        return res;
      }
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
   public Object visit(StmtExp n, Object argu) {
      Object res = null;
      res = (String)n.f1.accept(this, argu);
      
      String loc = (String)n.f3.accept(this, argu);
      if (emmet.isSpilled(loc)) {
        res += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
      }
      else {
        res += "MOVE v0 " + loc + "\n";
      }
      return res;
   }

   //this does not need explicit assignment to a new register
   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public Object visit(Call n, Object argu) {
      Object res = null;
      String code = "";
      String simp_ex = (String)n.f1.accept(this, argu);
      //System.err.println(simp_ex);
      if (emmet.isSpilled(simp_ex)) {
        code += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(simp_ex) + "\n";
        simp_ex = "v0";
      }

      code += emmet.store_caller_saved(((Proc)argu).params);
      //more formalities here
      if (n.f3 != null) {
         int count = 0;
         for ( Enumeration<Node> e = n.f3.elements(); e.hasMoreElements(); ) {
            Node node = e.nextElement();
            String loc = (String)node.accept(this, argu);
            if (emmet.isSpilled(loc)) {
                code += "ALOAD v1 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
                loc = "v1";
            }
            if (count <= 3) {
                code += "MOVE a" + count + " " + loc + "\n";
            }
            code += "PASSARG " + Integer.toString(count + 1) + " " + loc + "\n"; 
            count++;
         }
      }

      code += "CALL " + simp_ex + "\n";
      code += emmet.load_caller_saved(((Proc)argu).params);
      code += "MOVE v1 v0\n";
      
      res = new CodePair(code, "v1");
      return res;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public Object visit(HAllocate n, Object argu) {
      //System.err.println("eeee");
      Object res = null;
      String code = "";
      String simp_ex = (String)n.f1.accept(this, argu);
      if (emmet.isSpilled(simp_ex)) {
        code += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(simp_ex) + "\n";
        simp_ex = "v0";
      }
      
      boolean one_plus = true;
      
      if (!one_plus) {
         if (emmet.isInteger(simp_ex)) {
            int size = Integer.parseInt(simp_ex);
            code += "MOVE v0 " + Integer.toString(size + 4) + "\n";
          }
         else {
            code += "MOVE " + "v0" + " PLUS " + simp_ex + " 4" + "\n";
          }
      }
      
      else {
          code += "MOVE v0 " + simp_ex + "\n"; 
      }
      
      code += "MOVE v0 HALLOCATE " + "v0" + "\n";
      //System.err.println(code);
      res = new CodePair(code, "v0");
      return res;
   }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public Object visit(BinOp n, Object argu) {
      Object res = null;
      String code = "";
      String op = (String)n.f0.accept(this, argu);
      String loc = (String)n.f1.accept(this, argu);
      String simp_ex = (String)n.f2.accept(this, argu);

      if (emmet.isSpilled(loc)) {
        code += "ALOAD v0 SPILLEDARG " + emmet.extract_addr(loc) + "\n";
        loc = "v0"; 
      }

      if (emmet.isSpilled(simp_ex)) {
        code += "ALOAD v1 SPILLEDARG " + emmet.extract_addr(simp_ex) + "\n";
        simp_ex = "v1";
      }

      code += "MOVE v0 " + op + " " + loc + " " + simp_ex + "\n";

      res = new CodePair(code, "v0");
      return res;
   }

   /**
    * f0 -> "LE"
    *       | "NE"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    *       | "DIV"
    */
   public Object visit(Operator n, Object argu) {
      Object res = null;
      res = ((NodeToken) n.f0.choice).tokenImage;
      return res;
   }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public Object visit(SimpleExp n, Object argu) {
      Object res = null;
      res = (String)n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public Object visit(Temp n, Object argu) {
      Object res = null;
      String temp = "TEMP " + (String)n.f1.accept(this, argu);
      int start = ((Proc)argu).live_ranges.get(temp).st;
      if (start == Integer.MAX_VALUE) res = "a0";
      else res = ((Proc)argu).loc.get(temp);
      return res;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Object visit(IntegerLiteral n, Object argu) {
      Object res = null;
      res = n.f0.toString();
      return res;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Object visit(Label n, Object argu) {
      Object res = null;
      res = n.f0.toString();
      return res;
   }

}

//    //
//    // Auto class visitors--probably don't need to be overridden.
//    //
//    public Object visit(NodeList n, Object argu) {
//       Object res = null;
//       int _count=0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeListOptional n, Object argu) {
//       if ( n.present() ) {
//          Object res = null;
//          int _count=0;
//          for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//             e.nextElement().accept(this,argu);
//             _count++;
//          }
//          return res;
//       }
//       else
//          return null;
//    }

//    public Object visit(NodeOptional n, Object argu) {
//       if ( n.present() )
//          return n.node.accept(this,argu);
//       else
//          return null;
//    }

//    public Object visit(NodeSequence n, Object argu) {
//       Object res = null;
//       int _count=0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeToken n, Object argu) { return null; }
