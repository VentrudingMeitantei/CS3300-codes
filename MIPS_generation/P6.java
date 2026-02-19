import syntaxtree.*;
import visitor.*;

public class P6 {
   public static void main(String [] args) {
      try {
         Node root = new MiniRAParser(System.in).Goal();
         MIPSemitter mips = new MIPSemitter();
         System.out.println(root.accept(mips, null));
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}