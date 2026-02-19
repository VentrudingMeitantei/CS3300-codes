import syntaxtree.*;
import visitor.*;

public class P3 {
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         
	      FirstPass fp = new FirstPass();
	       //create a visitor.
         root.accept(fp, null);

         SecondPass sp = new SecondPass(fp);
         //fp.printfp();
         //sp.printsp();
         //System.out.println(sp.arr_alc("TEMP 67"));
         //System.out.println(sp.cls_exp("Tree"));
         System.out.println(root.accept(sp, null));
        
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}
