package visitor;
import syntaxtree.*;
import java.util.*;

public class FirstPass extends GJNoArguDepthFirst<String> {
   public HashMap<String, ClassInfo> classes = new HashMap<>();
   public boolean first_check = true;
   String curr_ret = null;
   String curr_class = null;
   String method_name = null;
   FunInfo curr_fun = null;
  
   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> ( ImportFunction() )?
    * f1 -> MainClass()
    * f2 -> ( TypeDeclaration() )*
    * f3 -> <EOF>
    */
   public String visit(Goal n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      //n.f3.accept(this);
      return _ret;
   }

   /**
    * f0 -> "import"
    * f1 -> "java.util.function.Function"
    * f2 -> ";"
    */
   public String visit(ImportFunction n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> PrintStatement()
    * f15 -> "}"
    * f16 -> "}"
    */
   public String visit(MainClass n) {
      //save old
      String old_class = curr_class;
      String old_ret = curr_ret;
      FunInfo old_fun = curr_fun;
      String old_mn = method_name;
      
      curr_class = n.f1.f0.toString();
      //check if the same class was declared earlier and add object if it wasn't declared
      if (!classes.containsKey(curr_class)) {
         curr_ret = "void";
         method_name = "main"; 
         ClassInfo temp_ci = new ClassInfo();
         curr_fun = new FunInfo();
         curr_fun.addPar(n.f11.f0.toString(), "String_Array");
         curr_fun.setRet(curr_ret);
         if(!temp_ci.addFun(method_name, curr_fun)) {
            //method of the same name already exists, and overloading is not allowed anyways
            System.out.println(/*"Redeclaration of method " + method_name*/ "Type error");
            System.exit(1);
         }
         classes.put(curr_class, temp_ci);
      }
      else {
         //class already exists
         System.out.println(/* "Redeclaration of class " + curr_class*/ "Type error");
         System.exit(1); 
      }
      
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f11.accept(this);
      n.f12.accept(this);
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);
      
      //restore old
      method_name = old_mn;
      curr_fun = old_fun;
      curr_class = old_class;
      curr_ret = old_ret;
      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public String visit(TypeDeclaration n) {
      String _ret=null;
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public String visit(ClassDeclaration n) {
      String _ret=null;
      //save old
      String old_class = curr_class;
      curr_class = n.f1.f0.toString();
      if (!classes.containsKey(curr_class)) {
         ClassInfo temp = new ClassInfo();
         classes.put(curr_class, temp);
      }
      else {
         //redeclaration of same class name
         System.out.println(/*"Redeclaration of class " + curr_class*/ "Type error");
         System.exit(1);
      }

      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);

      //restore
      curr_class = old_class;
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public String visit(ClassExtendsDeclaration n) {
      String _ret=null;
      //save
      String old_class = curr_class;
      curr_class = n.f1.f0.toString();
      if (!classes.containsKey(curr_class)) {
         curr_class = n.f1.f0.toString();
         String extends_from = n.f3.f0.toString();
         ClassInfo temp = new ClassInfo();
         temp.setP(extends_from);
         classes.put(curr_class, temp);
         //class_extends_from.put(curr_class, extends_from);
      }
      else {
         //redeclaration of class
         System.out.println(/*"Redeclaration of class " + curr_class*/ "Type error");
         System.exit(1);
      }
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);

      //restore
      curr_class = old_class;
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n) {
      String _ret=null;
      //if the var declaration is not in a function
      String type = n.f0.accept(this);
      if (method_name == null) {
         if(classes.get(curr_class).varPres(n.f1.f0.toString())) {
            //redeclaration of variable in a class
            System.out.println(/* "Redeclaration of variable " + n.f1.f0.toString() + " in class " + curr_class*/ "Type error");
            System.exit(1);
         }
         else {
            classes.get(curr_class).addVar(n.f1.f0.toString(), type);
         }
      }
      //if var declaration is within a function
      else {
         if (!curr_fun.addVar(n.f1.f0.toString(), type)) {
            //variable redeclaration
            System.out.println(/*"Redeclaration of a variable " + n.f1.f0.toString() + " inside method " + method_name */"Type error");
            System.exit(1);
         }
      }
      n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public String visit(MethodDeclaration n) {
      FunInfo old_fun = curr_fun;
      String old_ret = curr_ret;
      String old_method = method_name;
      
      method_name = n.f2.f0.toString();
      curr_fun = new FunInfo();
      
      String _ret=null;
      n.f0.accept(this);
      curr_ret = n.f1.accept(this);
      curr_fun.setRet(curr_ret);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      n.f11.accept(this);
      n.f12.accept(this);

      if (!classes.get(curr_class).addFun(method_name, curr_fun)) {
         //method redeclaration
         System.out.println(/*"Redeclaration of method " + method_name + " in class " + curr_class*/ "Type error");
         System.exit(1);
      }

      curr_fun = old_fun;
      method_name = old_method;
      curr_ret = old_ret;
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public String visit(FormalParameterList n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n) {
      String _ret=null;
      String type = n.f0.accept(this);
      if(!curr_fun.addPar(n.f1.f0.toString(), type)) {
         //redeclaration of parameter
         System.out.println(/*"Redeclaration of parameter " + n.f1.f0.toString() + " in method " + method_name*/ "Type error");
         System.exit(1);
      }
      n.f1.accept(this);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public String visit(FormalParameterRest n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    *       | LambdaType()
    */
   public String visit(Type n) {
      String _ret = n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(ArrayType n) {
      String _ret = "Int_Array";
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n) {
      String _ret = "boolean";
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n) {
      String _ret = "int";
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n) {
      String _ret = n.f0.toString();
      if (_ret.equals("Integer")) _ret = "int";
      if (_ret.equals("Boolean")) _ret = "boolean";
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "Function"
    * f1 -> "<"
    * f2 -> Identifier()
    * f3 -> ","
    * f4 -> Identifier()
    * f5 -> ">"
    */
   public String visit(LambdaType n) {
     
      n.f0.accept(this);
      n.f1.accept(this);
      String type1 = n.f2.accept(this);
      n.f3.accept(this);
      String type2 = n.f4.accept(this);
      n.f5.accept(this);
      String _ret = type1 + "#" + type2;
      return _ret;
   }

}
