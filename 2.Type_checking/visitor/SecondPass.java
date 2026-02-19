package visitor;
import syntaxtree.*;

import java.util.*;

class Traverse {

   public FunInfo find_fun(String cls, String fun, HashMap<String, ClassInfo> classes) {
      if (cls == null) return null;
      if (classes.get(cls).methods.containsKey(fun)) {
         return classes.get(cls).methods.get(fun);
      }
      return find_fun(classes.get(cls).getP(), fun, classes);
   }

   //this method will be called whenever a method is declared, so that we don't override in the wrong way
   public boolean check_valid_decl(String cls, String method_name, FunInfo method, HashMap<String, ClassInfo> classes) {
      //here we send in the parent when called
      if (cls == null) return true;
      if (!classes.containsKey(cls)) {
         return false;
      }
      ArrayList<String> method_sig = method.signature;
      //method name and signature are those of the child
      if (classes.get(cls).methodPres(method_name, method_sig)) {
         FunInfo par_method = classes.get(cls).methods.get(method_name);
         if (!par_method.getRet().equals(method.getRet())) {
            return false;
         }
      }
      return check_valid_decl(classes.get(cls).parent, method_name, method, classes);
   }

   public String find_var(String var, String cls, HashMap<String, ClassInfo> classes) {
      //this function checks if the current class or any of the parents contain the required field
      if (cls == null) {
         return null;
      }
      if (!classes.containsKey(cls)) return null;
      else if (classes.get(cls).varPres(var)) {
         return classes.get(cls).varType(var);
      }
      else {
         return find_var(var, classes.get(cls).getP(), classes);
      }
   } 

   public boolean isAnc(String s1, String s2, HashMap<String, ClassInfo> classes) { //tells if c1 is ancestor of c2
      ClassInfo c1 = classes.get(s1);
      ClassInfo c2 = classes.get(s2);
      if (c1 == null || c2 == null) return false;
      return (c1.start_time < c2.start_time && c2.end_time < c1.end_time);
   }
}

public class SecondPass extends GJNoArguDepthFirst<String> {
    public HashMap<String, ClassInfo> classes = new HashMap<>();
    public HashMap<String, ArrayList<String>> inheritance_graph = new HashMap<>();
    boolean is_import_declared = false;
    int line = 0;
    //miscellaneous
    boolean misc = false;
    boolean alloc = false;
    boolean type_decl = false;
    boolean lam_decl = false;
    ////////////////////////////////////
    //for lambdas
    String curr_lam_var = null;
    String curr_lam_vt = null;
    boolean in_lam = false;
    ////////////////////////////////////
    //for message send -> basically id.fun() kind of stuff
    String msg_id_type = null;
    String msg_id_name = null;
    ArrayList<String> msg_sig = new ArrayList<>(); //this for checking signature
    String fun_called = null;
    FunInfo found_fun = null;
    String cls_called = null;
    boolean lam_type = false; // we hande differenly when the id is a lambda
    boolean is_fun_call = false;
    boolean is_call = false; //for telling identifier that this is a function
    ////////////////////////////////////
    Traverse tr = new Traverse();
    public boolean second_check = true;
    String class_name = null;
    String method_name = null;
    ClassInfo curr_class = null;
    FunInfo curr_method = null;
    String expected_type = null;

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
      is_import_declared = true;
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
      String _ret=null;
      ClassInfo old_class = curr_class;
      FunInfo old_method = curr_method;
      String oc_name = class_name;
      String om_name = method_name;

      class_name = n.f1.f0.toString();
      curr_class = classes.get(class_name);
      method_name = "main";
      curr_method = classes.get(class_name).methods.get(method_name);

      n.f0.accept(this);
      misc = true;
      n.f1.accept(this);
      misc = false;
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      n.f10.accept(this);
      misc = true;
      n.f11.accept(this);
      misc = false;
      n.f12.accept(this);
      n.f13.accept(this);
      n.f14.accept(this);
      n.f15.accept(this);
      n.f16.accept(this);

      curr_class = old_class;
      curr_method = old_method;
      class_name = oc_name;
      method_name = om_name;

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

      String oc_name = class_name;
      ClassInfo old_class = curr_class;

      class_name = n.f1.f0.toString();
      curr_class = classes.get(class_name);

      n.f0.accept(this);
      misc = true;
      n.f1.accept(this);
      misc = false;
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);

      class_name = oc_name;
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

      String oc_name = class_name;
      ClassInfo old_class = curr_class;

      class_name = n.f1.f0.toString(); 
      curr_class = classes.get(class_name);

      n.f0.accept(this);
      misc = true;
      n.f1.accept(this);
      misc = false;
      n.f2.accept(this);
      misc = true;
      n.f3.accept(this);
      misc = false;
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      n.f7.accept(this);

      class_name = oc_name;
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
      type_decl = true;
      n.f0.accept(this);
      type_decl = false;
      misc = true;
      n.f1.accept(this);
      misc = false;
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
      String _ret=null;
      String om_name = method_name;
      FunInfo old_method = curr_method;
      String old_type = expected_type;
      
     
      method_name = n.f2.f0.toString();
      curr_method = classes.get(class_name).methods.get(method_name);
      
      n.f0.accept(this);

      type_decl = true;
      expected_type = n.f1.accept(this);
      type_decl = false;

      misc = true;
      n.f2.accept(this);
      misc = false;
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      if(!tr.check_valid_decl(curr_class.parent, method_name, curr_method, classes)) {
         line = 318; //not this
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      n.f6.accept(this);
      n.f7.accept(this);
      n.f8.accept(this);
      n.f9.accept(this);
      
      String type = n.f10.accept(this);
      
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 329;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f11.accept(this);
      n.f12.accept(this);

      method_name = om_name;
      curr_method = old_method;
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

      type_decl = true;
      n.f0.accept(this);
      type_decl = false;

      misc = true;
      n.f1.accept(this);
      misc = false;

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

   //this is the type
   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    *       | LambdaType()
    */
   public String visit(Type n) {
      String _ret= n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(ArrayType n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      _ret = "Int_Array";
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = "int";
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
      if (!is_import_declared) {
         System.out.println("Symbol not found"); //1
         System.exit(1);
      }
      String _ret=null;
      String type1  = null;
      String type2 = null;
      // type1 = n.f2.f0.toString();
      // type2 = n.f4.f0.toString();

      n.f0.accept(this);
      n.f1.accept(this);
      lam_decl = true;
      type1 = n.f2.accept(this);
      lam_decl = false;
      n.f3.accept(this);
      lam_decl = true;
      type2 = n.f4.accept(this);
      lam_decl = false;
      n.f5.accept(this);

      _ret = type1 + "#" + type2;
      return _ret;
   }

   //this is statement
   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public String visit(Statement n) {
      String _ret=null;
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public String visit(Block n) {
      String _ret=null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }
   
   //only declarations are made in classes :)
   //every other operation will be done in the methods
   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n) {
      String _ret=null;
      String id_name = n.f0.f0.toString();
      String id_type = null;
      String old_type = expected_type;
      //check if the current variable is declared somewhere in the heirarchy
      if (!curr_method.isPres(id_name) ) {
         id_type = tr.find_var(id_name, class_name, classes);
         if (id_type == null) {
            System.out.println("Symbol not found"); //2
            System.exit(1);
         }
      }
      else {
         if (curr_method.isVar(id_name)) id_type = curr_method.variables.get(id_name);
         else id_type = curr_method.parameters.get(id_name);
      }
      
      n.f0.accept(this);
      n.f1.accept(this);
      expected_type = id_type;
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 522;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n) {
      String _ret=null;
      String id_name = n.f0.f0.toString();
      String id_type = null;
      String old_type = expected_type;
      //check if variable is present
      if (!curr_method.isPres(id_name) ) {
         id_type = tr.find_var(id_name, class_name, classes);
         if (id_type == null) {
            System.out.println("Symbol not found"); //3
            System.exit(1);
         }
      }
      else {
         if (curr_method.isVar(id_name)) id_type = curr_method.variables.get(id_name);
         else id_type = curr_method.parameters.get(id_name);
      }
      if (!id_type.equals("Int_Array")) {
         line = 558;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }

      n.f0.accept(this);
      n.f1.accept(this);
      expected_type = "int";
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 568;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      n.f4.accept(this);
      old_type = expected_type;
      expected_type = "int";
      n.f5.accept(this);
      expected_type = old_type;
      n.f6.accept(this);
      return _ret;
   }

   /**
    * f0 -> IfthenElseStatement()
    *       | IfthenStatement()
    */
   public String visit(IfStatement n) {
      String _ret=null;
      n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(IfthenStatement n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      n.f1.accept(this);
      old_type = expected_type;
      expected_type = "boolean";
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 608;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      n.f4.accept(this);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfthenElseStatement n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      n.f1.accept(this);
      old_type = expected_type;
      expected_type = "boolean";
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 637;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(WhileStatement n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      n.f1.accept(this);
      old_type = expected_type;
      expected_type = "boolean";
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 664;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      n.f4.accept(this);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      n.f1.accept(this);
      old_type = expected_type;
      expected_type = "int";
      String type = n.f2.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 691;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f3.accept(this);
      n.f4.accept(this);
      return _ret;
   }

   /**
    * f0 -> OrExpression()
    *       | AndExpression()
    *       | CompareExpression()
    *       | neqExpression()
    *       | AddExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | DivExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | LambdaExpression()
    *       | PrimaryExpression()
    */
   public String visit(Expression n) {
      String _ret = n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> "("
    * f1 -> Identifier()
    * f2 -> ")"
    * f3 -> "->"
    * f4 -> Expression()
    */
   public String visit(LambdaExpression n) {
      String _ret=null;
      String old_lam_var = curr_lam_var;
      String old_lam_vt = curr_lam_vt;
      boolean old_in_lam = in_lam;
      //we get the expected type when we either assign this to a variable or pass this as a parameter
      if(!expected_type.contains("#")) {
         line = 735;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      
      String[] parts = expected_type.split("#", 2);
      curr_lam_vt  = parts[0];
     
      String code_type = parts[1];
      curr_lam_var = n.f1.f0.toString();
      if (curr_method.isPres(curr_lam_var)) {
         line = 746;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      in_lam = true; //setting lambda flag
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      String old_type = expected_type;
      expected_type = code_type;
      String type = n.f4.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 759;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      //restoring
      expected_type = old_type;
      curr_lam_var = old_lam_var;
      curr_lam_vt = old_lam_vt;
      in_lam = old_in_lam;

      _ret = expected_type;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public String visit(AndExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("boolean")) {
         line = 782;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;

      expected_type = "boolean";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "boolean";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("boolean") || !type2.equals( "boolean")) {
         line = 801;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "boolean";
      return _ret;
   }

   /* *
    * f0 -> PrimaryExpression()
    * f1 -> "||"
    * f2 -> PrimaryExpression()
    */
   public String visit(OrExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("boolean")) {
         line = 818;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;

      expected_type = "boolean";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "boolean";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("boolean") || !type2.equals("boolean")) {
         line = 837;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<="
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("boolean")) {
         line = 853;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;

      expected_type = "int";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("int") || !type2.equals("int")) {
         line = 873;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "!="
    * f2 -> PrimaryExpression()
    */
   public String visit(neqExpression n) {
      String _ret=null;
      if (!expected_type.equals("boolean")) {
         line = 889;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;
      type1 = n.f0.accept(this);
      n.f1.accept(this);
      type2 = n.f2.accept(this);
      if (!type1.equals(type2)) {
         if (!tr.isAnc(type1, type2, classes) && !tr.isAnc(type2, type1, classes)) {
            line = 900;
            System.out.println("Type error" /*+ line*/);
            System.exit(1);
         }
      }
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(AddExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("int")) {
         line = 919;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;

      expected_type = "int";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;
     
      
      if (!type1.equals("int") || !type2.equals("int")) {
         line = 937;
        
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n) {
      String _ret=null;
      String old_type = expected_type;
     
      if (!expected_type.equals("int")) {
         line = 955;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;
      
      expected_type = "int";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("int") || !type2.equals("int")) {
         line = 974;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("int")) {
         line = 991;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;
      
      expected_type = "int";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("int") || !type2.equals("int")) {
         line = 1010;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "/"
    * f2 -> PrimaryExpression()
    */
   public String visit(DivExpression n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("int")) {
         line = 1027;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;
       
      expected_type = "int";
      type1 = n.f0.accept(this);
      expected_type = old_type;

      n.f1.accept(this);

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      if (!type1.equals("int") || !type2.equals("int")) {
         line = 1045;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n) {
      String _ret=null;
      String old_type = expected_type;
      if (!expected_type.equals("int")) {
         line = 1065;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      String type1 = null;
      String type2 = null;

      expected_type = "Int_Array";
      type1 = n.f0.accept(this);
      n.f1.accept(this);
      expected_type = old_type;

      old_type = expected_type;
      expected_type = "int";
      type2 = n.f2.accept(this);
      expected_type = old_type;

      n.f3.accept(this);
      if (!type1.equals("Int_Array") || !type2.equals("int")) {
         line = 1083;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n) {
      String _ret=null;
      if (!expected_type.equals("int")) {
         line = 1099;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }

      String old_type = expected_type;

      expected_type = "Int_Array";
      String type = n.f0.accept(this);
      expected_type = old_type;
      
      n.f1.accept(this);
      n.f2.accept(this);
      if (!type.contains("_Array")) {
         line = 1113;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public String visit(MessageSend n) {
      String _ret=null;
      String old_fc = fun_called;
      String old_cc = cls_called;
      boolean old_is_fc = is_fun_call;

      is_fun_call = true;
      cls_called = n.f0.accept(this);
      is_fun_call = old_is_fc;

      n.f1.accept(this);

      fun_called = n.f2.f0.toString();

      misc = true;
      n.f2.accept(this);
      misc = false;
      
      n.f3.accept(this);
     
      if (cls_called.contains("#")) { //this is only for <lambda>.apply(<Something>)...it returns the second part
         
         if (!fun_called.equals("apply")) {
            System.out.println("Symbol not found"); //5
            System.exit(1);
         }
         
         boolean old_lam_type = lam_type;
         String old_type = expected_type;

         String[] parts = cls_called.split("#", 2);
         expected_type = parts[0];
         lam_type = true;
         n.f4.accept(this);
         expected_type = old_type;
         lam_type = old_lam_type;

         n.f5.accept(this);

         _ret = parts[1];
         
      }
      else {
         if (cls_called.equals("int") || cls_called.equals("boolean")) {
            line = 1173;
            System.out.println("Type error" /*+ line*/);
            System.exit(1);
         }
         if (!classes.containsKey(cls_called)) {
            //we always return declared types from primexp unless they are identifiers
            System.out.println("Symbol not found");
            System.exit(1);
         }

         //now since overloading is not allowed, and overriding doesn't really affect what we are doing, we just find the most recent declaration of this function
         FunInfo old_found_fun = found_fun;
         found_fun = tr.find_fun(cls_called, fun_called, classes);
         if (found_fun == null) {
            System.out.println("Symbol not found"); //6
            System.exit(1);
         }
         ArrayList<String> old_sig = msg_sig;
         msg_sig = new ArrayList<>();
         n.f4.accept(this);
         if (msg_sig.size() != found_fun.signature.size()) {
            System.out.println("Type error");
            System.exit(1);
         }
         msg_sig = old_sig;
         n.f5.accept(this);
         _ret = found_fun.getRet(); //but this is only if the method is present in the class directly
         found_fun = old_found_fun;
      }
     
       cls_called = old_cc;
      fun_called = old_fc;
      return _ret;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public String visit(ExpressionList n) {
      String _ret=null;
      if (lam_type) { //we already know what the expectation value is
         String type = n.f0.accept(this);
        
         if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
            line = 1220;
            System.out.println("Type error" /*+ line*/);
            System.exit(1);
         }
         n.f1.accept(this);
      }
      else {
         String old_type = expected_type;
         int i = msg_sig.size();
         if (i == found_fun.signature.size()) {
            System.out.println("Type error");
            System.exit(1);
         }
         expected_type = found_fun.signature.get(i);
         String type = n.f0.accept(this);
         if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
            line = 1236;
            System.out.println("Type error" /*+ line*/);
            System.exit(1);
         }
         msg_sig.add(expected_type);
         expected_type = old_type;
         n.f1.accept(this);
      }
      
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionRest n) {
      String _ret=null;
      if (lam_type) {
         //we have more that one argument for apply, which is not valid
         System.out.println("Symbol not found"); //7 //check whether or not this is symbol not found
         System.exit(1);
      }
      //this is when we are not in a lambda
      String old_type = expected_type;
      n.f0.accept(this);
      int i = msg_sig.size();
      if (i == found_fun.signature.size()) {
         System.out.println("Symbol not found");
         System.exit(1);
      }
      expected_type = found_fun.signature.get(i);
      String type = n.f1.accept(this); 
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 1270;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      msg_sig.add(expected_type);
      expected_type = old_type;
      return _ret;
   }

   //these are primary expressions
   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
   public String visit(PrimaryExpression n) {
      String _ret=null;
      _ret = n.f0.accept(this);
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = "int";
      return _ret;
   }

   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n) {
      String _ret=null;
      n.f0.accept(this);

      if (lam_decl) {
         String id_name = n.f0.toString();
         if (id_name.equals("Integer")) {
           _ret = "int";
         }
         else if (id_name.equals("Boolean")) {
           _ret = "boolean";
         }
         else if (!classes.containsKey(id_name)) {
            System.out.println("Symbol not found"); //8
            System.exit(1);
         }
         else _ret = id_name;
      }
      else if (type_decl) {
         String id_name = n.f0.toString();
         if (id_name.equals("Integer")) id_name = "int";
         else if (id_name.equals("Boolean")) id_name = "boolean";
         else if (!classes.containsKey(id_name) && !id_name.equals("int") && !id_name.equals("boolean")) {
            System.out.println("Symbol not found"); //sf
            System.exit(1);
         }
         else _ret = id_name;
      }
      else if (misc) {
         _ret = null; //these are mostly declarations
      }
      else {
         String id_name = n.f0.toString();
         if (id_name.equals("Integer")) id_name = "int";
         else if (id_name.equals("Boolean")) id_name = "boolean";
         else if (in_lam && id_name.equals(curr_lam_var)) {
            if (curr_lam_vt.equals("Integer")) _ret = "int";
            if (curr_lam_vt.equals("Boolean")) _ret = "boolean";
            else _ret = curr_lam_vt;
         }
         else if (curr_method.isPres(id_name)) {
            if (curr_method.isVar(id_name)) _ret = curr_method.variables.get(id_name);
            if (curr_method.isPar(id_name)) _ret = curr_method.parameters.get(id_name); 
         }
         else if (alloc) {
            if (!classes.containsKey(id_name)) {
               System.out.println("Symbol not found");  //nc
               System.exit(1);
            }
            _ret = id_name;
         }
         else {
            _ret = tr.find_var(id_name, class_name, classes); //but identifier can be used in the declaration as well!
            if (_ret == null) { //this variable wasn't declared anywhere
               System.out.println("Symbol not found"); //sf
               System.exit(1);
            }
         }
      }
      
      return _ret;
   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = class_name; //in case of classes the class name if the 
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      old_type = expected_type;
      expected_type = "int";
      String type = n.f3.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 1418;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      n.f4.accept(this);
      _ret = "Int_Array";
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n) {
      String _ret=null;
      String id_name = null; //in case of classes, this is the same as type
      n.f0.accept(this);
      alloc = true;
      id_name = n.f1.f0.toString();
      n.f1.accept(this);
      alloc = false;
      n.f2.accept(this);
      n.f3.accept(this);
      if (!classes.containsKey(id_name)) {
         System.out.println("Symbol not found");
         System.exit(1);
      }
      else if (is_fun_call) {
         _ret = id_name;
      }
      else if (!expected_type.equals(id_name) && !tr.isAnc(expected_type, id_name, classes)) {
         line = 1452;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      else {
         _ret = expected_type;
      }
      return _ret;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   public String visit(NotExpression n) {
      String _ret=null;
      String old_type = null;
      n.f0.accept(this);
      old_type = expected_type;
      expected_type = "boolean";
      String type = n.f1.accept(this);
      if (!expected_type.equals(type) && !tr.isAnc(expected_type, type, classes)) {
         line = 1473;
         System.out.println("Type error" /*+ line*/);
         System.exit(1);
      }
      expected_type = old_type;
      _ret = "boolean";
      return _ret;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n) {
      String _ret=null;
      n.f0.accept(this);
      _ret = n.f1.accept(this);
      n.f2.accept(this);
      return _ret;
   }
}

