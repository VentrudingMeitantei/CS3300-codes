package visitor;
import syntaxtree.*;
import java.util.*;

class Pair {
    public String t; //name of the thing
    public String c; //associated code
    public Pair(String f, String s) {
        this.t = f;
        this.c = s;
    }
}

public class Mini_to_Micro extends GJDepthFirst<Object, Object> {
    int temp_count = 10000;
    String nl = "\n";
    String sp = " ";
    String newTemp() {
        String tmp = "TEMP " + temp_count;
        temp_count++;
        return tmp;
    }
    public Object visit(NodeList n, Object argu) {
      Object _ret=null;
      String res = "";
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         res += e.nextElement().accept(this,argu);
         _count++;
      }
      return res;
   }

   public Object visit(NodeListOptional n, Object argu) {
    // System.out.println("aaaa");
      if ( n.present() ) {
         Object _ret=null;
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
         }
         return _ret;
      }
      else
         return null;
   }

   public Object visit(NodeOptional n, Object argu) {
      if ( n.present() )
         return n.node.accept(this,argu);
      else
         return null;
   }

   public Object visit(NodeSequence n, Object argu) {
    //System.out.println("aaaaa");
      Object _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public Object visit(NodeToken n, Object argu) { return null; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public Object visit(Goal n, Object argu) {
      String res = "" ;
      res = "MAIN" + nl;
    //   res += "start" + nl;
      res += n.f1.accept(this, argu) + nl;
    //   res += "boom" + nl;
      res += "END" + nl;
      NodeListOptional p = n.f3;
      if (p != null && p.present()) {
        for (Enumeration<Node> e = p.elements(); e.hasMoreElements();) {
            res += e.nextElement().accept(this, argu);
        }
      }
      return res;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public Object visit(StmtList n, Object argu) {
      String res = "" ;
      NodeListOptional lst = n.f0;
      if (lst != null && lst.present()) {
        for (Enumeration<Node> e = lst.elements(); e.hasMoreElements();) {
            NodeSequence seq = (NodeSequence) e.nextElement();
            NodeOptional maybeLabel = (NodeOptional) seq.elementAt(0);
            Stmt stmtNode = (Stmt) seq.elementAt(1);

            if (maybeLabel != null && maybeLabel.present()) {
                Label lbl = (Label) maybeLabel.node;
                String labelStr = (String) lbl.f0.toString();
                if (res.equals("")) res = labelStr + nl;
                else res += labelStr + nl;
            }
            String stmtStr = (String) stmtNode.accept(this, argu);
            if (res.equals("")) res = stmtStr;
            else res += stmtStr;
        }
      }
      //System.out.println(res);

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
      String res = "" ;
      Pair body = (Pair)n.f4.accept(this, argu);
      res = n.f0.f0.toString() + sp + "[" + n.f2.f0.toString() + "]" + nl;
      res += "BEGIN" + nl;
      res += body.c;
      res += "RETURN" + nl;
      res += body.t + nl;
      res += "END" + nl;
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
      String res = "";
      res = (String)n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "NOOP"
    */
   public Object visit(NoOpStmt n, Object argu) {
      String res = "" ;
      res = "NOOP" + nl;
      return res;
   }

   /**
    * f0 -> "ERROR"
    */
   public Object visit(ErrorStmt n, Object argu) {
      String res = "" ;
      res = "ERROR" + nl;
      return res;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Exp()
    * f2 -> Label()
    */
   public Object visit(CJumpStmt n, Object argu) {
      String res = "" ;
      Pair exp = (Pair)n.f1.accept(this, argu);
      res = exp.c;
      res += "CJUMP" + sp + exp.t + sp + n.f2.f0.toString() + nl;
      return res;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public Object visit(JumpStmt n, Object argu) {
      String res = "" ;
      res = "JUMP" + sp + n.f1.f0.toString() + nl;
      return res;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Exp()
    * f2 -> IntegerLiteral()
    * f3 -> Exp()
    */
   public Object visit(HStoreStmt n, Object argu) {
      String res = "" ;
      Pair exp1 = (Pair)n.f1.accept(this, argu);
      Pair exp2 = (Pair)n.f3.accept(this, argu);
      res = exp1.c + exp2.c;
      res += "HSTORE" + sp + exp1.t + sp + n.f2.f0.toString() + sp + exp2.t + nl;
      return res;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Exp()
    * f3 -> IntegerLiteral()
    */
   public Object visit(HLoadStmt n, Object argu) {
      String res = "" ;
      Pair tmp = (Pair)n.f1.accept(this, argu);
      Pair exp = (Pair)n.f2.accept(this, argu);
      res = tmp.c + exp.c;
      res += "HLOAD" + sp + tmp.t + sp + exp.t + sp + n.f3.f0.toString() + nl;
      return res;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public Object visit(MoveStmt n, Object argu) {
        String res = "" ;
        Pair tmp = (Pair)n.f1.accept(this, argu);
        Pair exp = (Pair)n.f2.accept(this, argu);
        res = tmp.c;
        //res += "blaaah\n";
        res += exp.c ;
        //res += "bleeeeeh\n";
        res += "MOVE" + sp + tmp.t + sp + exp.t + nl; 
        return res;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> Exp()
    */
   public Object visit(PrintStmt n, Object argu) {
      String res = "" ;
      Pair exp = (Pair)n.f1.accept(this, argu);
      res = exp.c;
      res += "PRINT" + sp + exp.t + nl;
      return res;
   }

   /**
    * f0 -> StmtExp()
    *       | Call()
    *       | HAllocate()
    *       | BinOp()
    *       | Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public Object visit(Exp n, Object argu) {
      Object res = null;
      res = n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> Exp()
    * f4 -> "END"
    */
   public Object visit(StmtExp n, Object argu) {
      Pair p = null;
      String new_tmp = newTemp();
      String code = null;
      Pair ret = (Pair)n.f3.accept(this, argu);
      
      code = "MOVE" + sp + new_tmp + nl; 
      code += "BEGIN" + nl;
      //yet to handle stmtlist
      code += n.f1.accept(this, argu);
      
      code += ret.c;
      code += "RETURN" + nl;
      code += ret.t + nl;
      code += "END" + nl;
      p = new Pair(new_tmp, code);
      return p;
   }

   /**
    * f0 -> "CALL"
    * f1 -> Exp()
    * f2 -> "("
    * f3 -> ( Exp() )*
    * f4 -> ")"
    */
   public Object visit(Call n, Object argu) {
      Pair p = null;
      ArrayList<Pair> pair_list = new ArrayList<>();
      String code = null;
      String new_tmp = newTemp();
      Pair fun_called = (Pair)n.f1.accept(this, argu);
      NodeListOptional lst = n.f3;
      if (lst != null && lst.present()) {
        for (Enumeration<Node> e = lst.elements(); e.hasMoreElements();) {
            Pair curr_pair = (Pair)e.nextElement().accept(this, argu);
            pair_list.add(curr_pair);
        }
      }
      //now the code part
      code = fun_called.c;
      for (Pair x: pair_list) {
        code += x.c;
      }
      //the function call
      code += "MOVE" + sp + new_tmp + sp + "CALL" + sp + fun_called.t + sp + "(" + sp;
      for (Pair x: pair_list) {
        code += x.t + sp;
      }
      code += ")" + nl;
      p = new Pair(new_tmp, code);
      return p;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> Exp()
    */
   public Object visit(HAllocate n, Object argu) {
      Pair p = null;
      Pair exp = (Pair)n.f1.accept(this, argu);
      String new_tmp = newTemp();
      String code = null;
      code = exp.c;
      code += "MOVE" + sp + new_tmp + sp + "HALLOCATE" + sp + exp.t + nl;
      p = new Pair(new_tmp, code);
      return p;
   }

   /**
    * f0 -> Operator()
    * f1 -> Exp()
    * f2 -> Exp()
    */
   public Object visit(BinOp n, Object argu) {
      Pair p = null;
      String new_tmp = newTemp();
      String code = null;
      Pair exp1 = (Pair)n.f1.accept(this, argu);
      Pair exp2 = (Pair)n.f2.accept(this, argu);
      code = exp1.c + exp2.c;
      code += "MOVE" + sp + new_tmp + sp + n.f0.accept(this, argu) + sp + exp1.t + sp + exp2.t + nl;
      p = new Pair(new_tmp, code);
      return p;
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
      String res = n.f0.choice.toString();
      return res;
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public Object visit(Temp n, Object argu) {
      Pair p = null;
      String curr_temp = null;
      //String new_tmp = newTemp();
      curr_temp = "TEMP ";
      curr_temp += n.f1.f0.toString();
      String code = "";
      p = new Pair(curr_temp, code);
      return p;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Object visit(IntegerLiteral n, Object argu) {
      Pair p = null;
      String int_lit = n.f0.toString();
      String new_tmp = newTemp();
      String code = "MOVE" + sp + new_tmp + sp + int_lit + nl;
      p = new Pair(new_tmp, code);
      return p;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Object visit(Label n, Object argu) {
      Pair p = null;
      String name = n.f0.toString();
      String new_tmp = newTemp();
      String code = "MOVE" + sp + new_tmp + sp + name + nl;
      p = new Pair(new_tmp, code);
      return p;
   }
    
}
