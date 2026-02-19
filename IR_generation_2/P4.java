import syntaxtree.*;
import visitor.*;

public class P4 {
   public static void main(String [] args) {
      try {
         Node root = new MiniIRParser(System.in).Goal();
         
         Mini_to_Micro m2m = new Mini_to_Micro();
         System.out.println(root.accept(m2m, null));
        
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}