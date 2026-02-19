import syntaxtree.*;
import visitor.*;

public class P5 {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         
         RegAlloc ra = new RegAlloc();
         FunctionParamPass fpp = new FunctionParamPass(ra);
         CodeEmitter ce = new CodeEmitter(ra);
         root.accept(ra, null);
         root.accept(fpp);
         ra.print();
         System.out.println(root.accept(ce, null));
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}