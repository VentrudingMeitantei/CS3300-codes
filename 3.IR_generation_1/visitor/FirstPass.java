package visitor;
import syntaxtree.*;
import java.util.*;

public class FirstPass extends GJDepthFirst<Object, Object> {
    HashMap<String, ClassInfo> classes = new HashMap<>();
    HashMap<String, FunInfo> methods = new HashMap<>();

    public void printfp() {
        System.out.println("Classes: ");
        for (var v: classes.entrySet()) {
            System.out.println("    "+v.getKey());
            System.out.println("        methods: ");
            for (var x: v.getValue().l_vtable.entrySet()) {
                System.out.println("            Function name: " + x.getKey() + " & index: " + x.getValue());
            }
            System.out.println("        fields: ");
            for (var x: v.getValue().l_contents.entrySet()) {
                System.out.println("            Variable name: " + x.getKey() + " & index: " + x.getValue());
            }
        }

        System.out.println("------------");

        System.out.println("Functions: ");
        for (var v: methods.entrySet()) {
            System.out.println("    "+v.getKey());
            System.out.println("        parameters: ");
            for (var x: v.getValue().parameters.entrySet()) {
                System.out.println("            Parameter name: " + x.getKey() + " & index: " + x.getValue());
            }
            System.out.println("        variables: ");
            for (var x: v.getValue().variables.entrySet()) {
                System.out.println("            Variable name: " + x.getKey() + " & index: " + x.getValue());
            }
        }
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
   public Object visit(MainClass n, Object argu) {
      Object _ret=null;

      ClassInfo curr_class = new ClassInfo();
      String name = n.f1.f0.toString();
      curr_class.name = name;
      FunInfo curr_method = new FunInfo(name, "main");
      //System.out.println(curr_method.toString() + " a");
      curr_method.add_param(n.f11.f0.toString(), "str_array");
      curr_method.ret = "void";
      curr_class.add_method(curr_method.toString(), "void");
      classes.put(curr_class.toString(), curr_class);
      methods.put(curr_method.toString(), curr_method);
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
   public Object visit(ClassDeclaration n, Object argu) {
      Object _ret=null;

      ClassInfo curr_class = new ClassInfo();
      curr_class.name = n.f1.f0.toString();
      classes.put(curr_class.toString(), curr_class);

      argu = curr_class.toString();
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      
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
   public Object visit(ClassExtendsDeclaration n, Object argu) {
      Object _ret=null;

      ClassInfo curr_class = new ClassInfo();
      curr_class.name = n.f1.f0.toString();
      curr_class.parent = n.f3.f0.toString();
      classes.put(curr_class.toString(), curr_class);
      argu = curr_class.toString();

      n.f5.accept(this, argu);
      n.f6.accept(this, argu);

      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public Object visit(VarDeclaration n, Object argu) {
      Object _ret=null;
      if (!((String)argu).contains("#")) { //this is a variable declared in the class body
        ClassInfo cls_obj = classes.get((String)argu);
        cls_obj.add_field(n.f1.f0.toString(), (String)n.f0.accept(this, argu));
      }
      else { //if it has a #, it is within a method in the class
        String[] parts = ((String)argu).split("#", 2);
        String fun = parts[1];
        FunInfo fun_obj = methods.get(fun);
        fun_obj.add_var(n.f1.f0.toString(), (String)n.f0.accept(this, argu));
      }
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
   public Object visit(MethodDeclaration n, Object argu) {
      Object _ret=null;
      String ret_type = (String)n.f1.accept(this, argu);
      //System.out.println(ret_type);
      FunInfo curr_fun = new FunInfo((String)(argu), n.f2.f0.toString());
      curr_fun.ret = ret_type;
      classes.get((String)argu).add_method(n.f2.f0.toString(), ret_type);
      methods.put(curr_fun.toString(), curr_fun);
      argu = (String)argu + "#" + curr_fun.toString();

      n.f4.accept(this, argu);
      n.f7.accept(this, argu);

      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public Object visit(FormalParameterList n, Object argu) {
      Object _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public Object visit(FormalParameter n, Object argu) {
      Object _ret=null;
      String type = (String)n.f0.accept(this, argu);
      String[] parts = ((String)argu).split("#", 2);
      String fun = parts[1];
      FunInfo fun_obj = methods.get(fun);
      fun_obj.addSig(type);
      fun_obj.add_param(n.f1.f0.toString(), type);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public Object visit(FormalParameterRest n, Object argu) {
      Object _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Object visit(Identifier n, Object argu) {
      Object _ret=null;
      n.f0.accept(this, argu);
      _ret = n.f0.toString();
      if (_ret.equals("Integer")) _ret = "int";
      if (_ret.equals("Boolean")) _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    *       | LambdaType()
    */
   public Object visit(Type n, Object argu) {
      Object _ret=null;
      _ret = n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public Object visit(ArrayType n, Object argu) {
     
      return "int_array";
   }

   /**
    * f0 -> "boolean"
    */
   public Object visit(BooleanType n, Object argu) {

      n.f0.accept(this, argu);
      return "boolean";
   }

   /**
    * f0 -> "int"
    */
   public Object visit(IntegerType n, Object argu) {
     
      n.f0.accept(this, argu);
      return "int";
   }

   /**
    * f0 -> "Function"
    * f1 -> "<"
    * f2 -> Identifier()
    * f3 -> ","
    * f4 -> Identifier()
    * f5 -> ">"
    */
   public Object visit(LambdaType n, Object argu) {
      Object _ret=null;
      _ret = (String)n.f2.accept(this, argu);
      _ret += "#";
      _ret += (String)n.f4.accept(this, argu);
      return _ret;
   }
}
