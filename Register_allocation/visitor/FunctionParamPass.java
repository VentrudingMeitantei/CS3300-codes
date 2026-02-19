package visitor;
import syntaxtree.*;

import java.util.*;
public class FunctionParamPass extends DepthFirstVisitor {
    ArrayList<Proc> procedures;
    int proc_num = 0;
    public FunctionParamPass(RegAlloc ra) {
        procedures = ra.procedures;
    }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public void visit(Procedure n) {
      proc_num++; //starts from 1, bcz we are not really concerned about main
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public void visit(Call n) {
      n.f0.accept(this);
      n.f1.accept(this);
      int count = 0;
      if (n.f3.present()) {
         for ( Enumeration<Node> e = n.f3.elements(); e.hasMoreElements(); e.nextElement()) {
            count++;
         }
         procedures.get(proc_num).max_arg = Integer.max(count, procedures.get(proc_num).max_arg);
      }
   }
}
