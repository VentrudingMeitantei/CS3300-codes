package visitor;
import syntaxtree.*;


import java.util.*;

class Pair {
    public String f;
    public String s;

    Pair(String first, String second) {
        this.f = first;
        this.s = second;
    }
}

class Traverser {
   public void do_the_thing(HashMap<String, ClassInfo> classes) { //does the thing
      for (var x: classes.entrySet()) {
         traverse_up_and_do_stuff_for_fun(x.getKey(), x.getKey(), classes);
         reindex_uvt(x.getValue());
         traverse_up_and_do_stuff_for_var(x.getKey(), x.getKey(), classes);
      }
   }
   public String find_overridable(String fun, String cls, HashMap<String, ClassInfo> classes) {
      String s = null;
      for (var x: classes.get(cls).u_vtable.entrySet()) {
         String[] parts = x.getKey().split("@", 2);
         if (parts[1].equals(fun)) return x.getKey();
      }
      return s;
   }

   public int traverse_up_and_do_stuff_for_fun(String target, String cls, HashMap<String, ClassInfo> classes) { //traverses up and does stuff
      if (cls == null) return 0; 
      int curr = traverse_up_and_do_stuff_for_fun(target, classes.get(cls).getP(), classes);
      
      for (var x: classes.get(cls).l_vtable.entrySet()) {
         String[] parts = x.getKey().split("@", 2);
         String rem = find_overridable(parts[1], target, classes); 
         ////System.out.println(parts[1] + " " + rem);
         if (rem != null)classes.get(target).u_vtable.remove(rem);
         classes.get(target).u_vtable.put(x.getKey(), curr + 4*x.getValue());
         classes.get(target).uvt_type.put(x.getKey(), classes.get(cls).lvt_type.get(x.getKey()));
        
      }

      return curr + 4*classes.get(target).u_vtable.size();
   }

   public void reindex_uvt(ClassInfo cls) {
      int i = 0;
      for (var x: cls.u_vtable.entrySet()) {
         x.setValue(4*i);
         i++;
      }
   }
   public int traverse_up_and_do_stuff_for_var(String target, String cls, HashMap<String, ClassInfo> classes) { //traverses up and does stuff
      if (cls == null) return 0; 
      int curr = traverse_up_and_do_stuff_for_var(target, classes.get(cls).getP(), classes);
      for (var x: classes.get(cls).l_contents.entrySet()) {
         classes.get(target).u_contents.put(x.getKey(), curr + 4*x.getValue());
         classes.get(target).uc_type.put(x.getKey(), classes.get(cls).lc_type.get(x.getKey()));
      }

      return curr + 4*classes.get(cls).l_contents.size();
   }
   
}

class MiniIR_generator {
   Integer l_count = 0; //keeps a count of the labels
   Integer g_count = 0; //keeps a track of the numbers used for temps
   String sp = " ";
   String nl = "\n";
   public int getL() {
      return l_count++;
   }
   public int getG() {
      return g_count++;
   }
   public void set_g_cnt(HashMap<String, FunInfo> fin) {
      for (var x: fin.values()) {
         g_count = Math.max(x.parameters.size() + x.variables.size(), g_count);
      }
      g_count++;
   }

   public String find_var_in_fun(String var, FunInfo mtd) {
      if (mtd.parameters.containsKey(mtd.toString() + "@" + var)) {
         return "TEMP" + sp + mtd.parameters.get(mtd.toString() + "@" + var)/4;
      }
      if (mtd.variables.containsKey(mtd.toString() + "@" + var)) {
         return "TEMP" + sp + mtd.variables.get(mtd.toString() + "@" + var)/4;
      }
      return null;
   }

   public String find_var_in_cls(String var, String curr_cls, String tar_cls, HashMap<String, ClassInfo> classes) { //this needs some major modifcation, in terms of HLOAD and blah
      if (curr_cls == null) return null;
      if (classes.get(tar_cls).u_contents.containsKey(curr_cls + "@" + var)) {
         return "TEMP" + sp + "0" + sp + Integer.toString(classes.get(tar_cls).u_contents.get(curr_cls + "@" + var));
      }
      return find_var_in_cls(var, classes.get(curr_cls).getP(), tar_cls, classes);
   }

   public String type_in_cls(String var, String curr_cls, String tar_cls, HashMap<String, ClassInfo> classes) {
      if (curr_cls == null) return null;
      if (classes.get(tar_cls).uc_type.containsKey(curr_cls + "@" + var)) {
         return classes.get(tar_cls).uc_type.get(curr_cls + "@" + var);
      }
      return type_in_cls(var, classes.get(curr_cls).getP(), tar_cls, classes);
   }

   public String find_fun(String cls, String fun, HashMap<String, ClassInfo> classes) {
      if (cls == null) return null;
      ////System.out.println("a " + fun);
      for (var x: classes.get(cls).u_vtable.entrySet()) {
         ////System.out.println(x.getKey() + " " + x.getValue());
         String [] parts = x.getKey().split("@", 2);
         if (parts[1].equals(fun)) return Integer.toString(x.getValue());
      }
      // if (classes.get(cls).u_vtable.containsKey(cls + "_" + fun)) {
      //    return Integer.toString(classes.get(cls).u_vtable.get(cls + "_" + fun));
      // }
      // return find_fun(classes.get(cls).getP(), fun, classes);
      return null;
   }

   public String fun_type(String cls, String fun, HashMap<String, ClassInfo> classes, HashMap<String, FunInfo> methods) {
      if (cls == null) return null;
      ////System.out.println(cls);
       for (var x: classes.get(cls).u_vtable.entrySet()) {
         ////System.out.println(x.getKey() + " " + x.getValue());
         String [] parts = x.getKey().split("@", 2);
         if (parts[1].equals(fun)) return methods.get(x.getKey()).ret;
      }
      return null;
   }

   public String class_expansion(String cls, HashMap<String, ClassInfo> classes) { //this gives the expasion for a class
      return class_expansion_helper(classes.get(cls));
   }

   String class_expansion_helper(ClassInfo cls) {
      String s = null;
      String return_array = "TEMP" + sp + Integer.toString(g_count++);
      String vtable_array = "TEMP" + sp + Integer.toString(g_count++);
      s = "BEGIN" + nl;
      s += "MOVE" + sp + return_array + sp  + "HALLOCATE" + sp + Integer.toString(4*(cls.u_contents.size() + 1)) + nl; //allocates the main array
      s += "MOVE" + sp + vtable_array + sp  + "HALLOCATE" + sp + Integer.toString(4*(cls.u_vtable.size())) + nl; //allocates vtable
      //loading function pointers
      for (var x: cls.u_vtable.entrySet()) {
         String [] parts = x.getKey().split("@", 2);
         String fun = parts[0] + "_" + parts[1];
         s += "HSTORE" + sp + vtable_array + sp + x.getValue() + sp + fun + nl;
      }

      String counter = "TEMP" + sp + Integer.toString(g_count++);
      s += "MOVE" + sp + counter + sp + Integer.toString(4) + nl;
      String while_label = "L_"  + Integer.toString(l_count++);
      String end_label = "L_"  + Integer.toString(l_count++);
      //initializing fields to zero (using while loop in MiniIR)
      s +=  while_label + nl; //this is the label
      s += "CJUMP LE" + sp + counter + sp + Integer.toString(4*(cls.u_contents.size() + 1) - 1) + sp + end_label + nl;
      //HSTORE  PLUS TEMP 56 TEMP 57  0  0 
      s += "HSTORE PLUS" + sp + return_array + sp + counter + sp + "0 0" + nl;
      //MOVE TEMP 57  PLUS TEMP 57  4 
      s += "MOVE" + sp + counter + sp + "PLUS" +  sp + counter + sp + "4" + nl;
	   //JUMP L2 
      s += "JUMP" + sp + while_label + nl;
      
      // L3 	HSTORE TEMP 56  0 TEMP 55 
      s += end_label + nl;
      s += "HSTORE" + sp + return_array + " 0 " + vtable_array + nl;
      // RETURN 
      s += "RETURN" + nl;
      // TEMP 56 
      s += return_array  +nl;
      // END
      s += "END" + nl;
      return s;
   }

   public String array_alloc(String sz_lit) { 
     
      String s = "BEGIN" + nl;
      String size = "TEMP" + sp + getG();
      String arr = "TEMP" + sp + getG();
      String counter = "TEMP" + sp + getG();
      s += "MOVE" + sp + size + sp + sz_lit + nl;
      s += "MOVE" + sp + arr + sp + "HALLOCATE PLUS TIMES" + sp + size+ sp + "4 4" + nl;
      s += "HSTORE" + sp + arr + sp + "0" + sp + size + nl; //store size at zero index
      s += "MOVE" + sp + counter + sp + "4" + nl;
      String st_label = "L_" + getL();
      String end_label = "L_" + getL();
      s += st_label + nl;
      s += "CJUMP LE" + sp + counter + sp + "PLUS TIMES" + sp + size + sp + "4 4" + sp + end_label + nl;
      s += "HSTORE PLUS" + sp + arr + sp + counter + sp + "0 0" + nl;
      s += "MOVE" + sp + counter + sp + "PLUS" + sp + counter + sp + "4" + nl;
      s += "JUMP" + sp + st_label + nl;
      
      s += end_label + sp + nl;
      s += "NOOP" + nl;
      s += "RETURN" + nl;
      s += arr + nl + "END" + nl;
      return s;
   }
      
}

public class SecondPass extends GJDepthFirst<Object, Object> {
   HashMap<String, ClassInfo> classes = new HashMap<>();
   HashMap<String, FunInfo> methods = new HashMap<>();
   ArrayList<String> l_vars = new ArrayList<>();
   String sp = " ";
   String nl = "\n";
   String lambda_decl = "";
   int lambda_cnt = -1; //for lambda definitions
   public Traverser zhuli = new Traverser();
   public MiniIR_generator doof = new MiniIR_generator();

   public String arr_alc(String sz) {
      return doof.array_alloc(sz);
   }
   public String cls_exp(String cls) {
      return doof.class_expansion(cls, classes);
   }
   public String find_fun(String cls, String fun) {
      return doof.find_fun(cls, fun, classes);
   }

   public String fun_type(String cls, String fun) {
      return doof.fun_type(cls, fun, classes, methods);
   }

   public SecondPass(FirstPass fp) {
      classes = fp.classes;
      methods = fp.methods;
      zhuli.do_the_thing(classes);
      doof.set_g_cnt(methods);
   }
   public void printsp() {
       //System.out.println("Classes: ");
       for (var v: classes.entrySet()) {
           //System.out.println("    "+v.getKey());
           //System.out.println("        methods: ");
           for (var x: v.getValue().u_vtable.entrySet()) {
               //System.out.println("            Function name: " + x.getKey() + " & index: " + x.getValue() + " & type: " + v.getValue().uvt_type.get(x.getKey()));
           }
           //System.out.println("        fields: ");
           for (var x: v.getValue().u_contents.entrySet()) {
               //System.out.println("            Variable name: " + x.getKey() + " & index: " + x.getValue() + " & type: " +  v.getValue().uc_type.get(x.getKey()));
           }
       }

       //System.out.println("------------");
       //System.out.println("Functions: ");

       for (var v: methods.entrySet()) {
           //System.out.println("    "+v.getKey() + " ret type: " + v.getValue().ret);
           //System.out.println("        parameters: ");
           for (var x: v.getValue().parameters.entrySet()) {
               //System.out.println("            Parameter name: " + x.getKey() + " & index: " + x.getValue() + " & type: " + v.getValue().p_type.get(x.getKey()));
           }
           //System.out.println("        variables: ");
           for (var x: v.getValue().variables.entrySet()) {
               //System.out.println("            Variable name: " + x.getKey() + " & index: " + x.getValue() + " & type: " + v.getValue().v_type.get(x.getKey()));
           }
       }
   }
   
   //useful part
   public Object visit(NodeListOptional n, Object argu) {
      if ( n.present() ) {
         String _ret="";
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            if (_ret == null) _ret = (String)e.nextElement().accept(this,argu);
            else _ret += (String)e.nextElement().accept(this,argu);
         }
         return _ret;
      }
      else
         return "";
   }
   //each one returns what to print
   /**
    * f0 -> ( ImportFunction() )?
    * f1 -> MainClass()
    * f2 -> ( TypeDeclaration() )*
    * f3 -> <EOF>
    */
   public Object visit(Goal n, Object argu) {
      String res = null;
      n.f0.accept(this, argu);
      res = "MAIN" + sp + (String)n.f1.accept(this, argu);
      res += "END" + nl;
      argu = null;
      res += n.f2.accept(this, argu) + "\n";
      if (lambda_cnt >= 0) res += lambda_decl;
      n.f3.accept(this, argu);
      return res;
   }

    /**
    * f0 -> "import"
    * f1 -> "java.util.function.Function"
    * f2 -> ";"
    */
   public Object visit(ImportFunction n, Object argu) {
      Object _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
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
   public Object visit(MainClass n, Object argu) {
      Object res =null;
      argu = methods.get(n.f1.f0.toString() + "@main");
      res = (String) n.f14.accept(this, argu) + nl; //this is the only part we need as of now
     
      return res ;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public Object visit(TypeDeclaration n, Object argu) {
      Object res =null;
      res = (String)n.f0.accept(this, argu);
      return res ;
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
      Object res ="";
      argu = n.f1.f0.toString();
      n.f0.accept(this, argu);
      
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      res = n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      return res ;
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
      Object res ="";
      argu = n.f1.f0.toString();
      n.f0.accept(this, argu);
     
      n.f2.accept(this, argu);
     
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      res = n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public Object visit(VarDeclaration n, Object argu) {
      Object res ="";
      
      return res ;
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
      Object res ="";
      String m_name = (String)argu + "@" + n.f2.f0.toString();
      FunInfo m_obj = methods.get(m_name);
      argu = m_obj;
      String [] parts = m_obj.toString().split("@", 2);
      res = parts[0] + "_" + parts[1] + sp + "[" + (m_obj.parameters.size() + 1) + "]" + nl; 
      res += "BEGIN" + nl;
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      String null_check = (String)n.f8.accept(this, argu);
      res += (null_check == null) ? "NOOP\n" : null_check;
      res += "RETURN" + nl;
      res += ((Pair)n.f10.accept(this, argu)).f;
      res += "END" + nl;
      return res ;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public Object visit(FormalParameterList n, Object argu) {
      Object res ="";
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public Object visit(FormalParameter n, Object argu) {
      Object res ="";
     
      
      return res ;
   }

//    there is an issue with ur identifier in return statement
// and also you did some /4 in find_fun, check that part

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public Object visit(FormalParameterRest n, Object argu) {
      Object res ="";
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    *       | LambdaType()
    */
   public Object visit(Type n, Object argu) {
      Object res ="";
      n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public Object visit(ArrayType n, Object argu) {
      Object res =null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "boolean"
    */
   public Object visit(BooleanType n, Object argu) {
      Object res =null;
      n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "int"
    */
   public Object visit(IntegerType n, Object argu) {
      Object res =null;
      n.f0.accept(this, argu);
      return res ;
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
      Object res =null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
     
      n.f3.accept(this, argu);
      
      n.f5.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public Object visit(Statement n, Object argu) {
      Object res ="";
      res = (String)n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public Object visit(Block n, Object argu) {
      Object res ="";
      n.f0.accept(this, argu);
      res = (String)n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public Object visit(AssignmentStatement n, Object argu) {
      Object res ="";
      String id = null;
      id = n.f0.f0.toString();
      String var_ind = doof.find_var_in_fun(id, (FunInfo)argu);
      if (var_ind != null) { //basically we found the variable in the method itself
         res = "MOVE" + sp + var_ind + sp;
      }
      else {
         res = "HSTORE" + sp + doof.find_var_in_cls(id, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes) + sp;
      }
      Pair temp = (Pair)n.f2.accept(this, argu);
      res += temp.f;
      String type = temp.s;
      if (var_ind != null) { //basically we found the variable in the method itself
         ////System.out.println(type + " " + ((FunInfo)argu).toString());
         methods.get(((FunInfo)argu).toString()).setType(id, type);
         ////System.out.println(((FunInfo)argu).type(id));
      }
      else {
         classes.get(((FunInfo)argu).getC()).setType(id, type);
      }
      return res ;
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
   public Object visit(ArrayAssignmentStatement n, Object argu) {
      Object res ="";
      String id_name = null;
      id_name = n.f0.f0.toString();
      String dest_temp = doof.find_var_in_fun(id_name, (FunInfo)argu);
      String off_temp = "TEMP" + sp + doof.getG();
      String value = "TEMP" + sp + doof.getG();
      String temp = "TEMP" + sp + doof.getG(); //to ensure the actual value doesn't get damaged
      res = "MOVE" + sp + off_temp + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "MOVE" + sp + off_temp + sp + "TIMES" + sp + off_temp + sp + 4 + nl; 
      res += "MOVE" + sp + off_temp + sp + "PLUS" + sp + off_temp + sp + "4" + nl;
      res += "MOVE" + sp + value + sp + ((Pair)n.f5.accept(this, argu)).f; //this holds what we have to store
      if (dest_temp != null) { //if we find the identifier in the method itself
         res += "MOVE" + sp + temp + sp + dest_temp + nl;
         res += "MOVE" + sp + temp + sp + "PLUS"  + sp + temp + sp + off_temp + nl;
      }
      else {
         res += "HLOAD" + sp + temp + sp + doof.find_var_in_cls(id_name, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes) + nl; //now dest_temp has the pointer to the first element of the array
         res += "MOVE" + sp + temp + sp + "PLUS" + sp + temp + sp + off_temp + nl; //now temp has the pointer to index we need
      }
      res += "HSTORE" + sp + temp + sp + "0" + sp + value + nl;
      return res;
   }

   /**
    * f0 -> IfthenElseStatement()
    *       | IfthenStatement()
    */
   public Object visit(IfStatement n, Object argu) {
      Object res ="";
      res = n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public Object visit(IfthenStatement n, Object argu) {
      Object res ="";
      res = "CJUMP" + sp;
      String else_lbl = "L_" + doof.getL();
      res += ((Pair)n.f2.accept(this, argu)).f + sp + else_lbl + nl;
      res += (String)n.f4.accept(this, argu);
      res += else_lbl + sp + "NOOP" + nl;
      return res ;
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
   public Object visit(IfthenElseStatement n, Object argu) {
      Object res ="";
      res = "CJUMP" + sp;
      String else_lbl = "L_" + doof.getL();
      String fall_through = "L_" + doof.getL();
      res += ((Pair)n.f2.accept(this, argu)).f + sp + else_lbl + nl;
      res += (String)n.f4.accept(this, argu) + nl;
      res += "JUMP" + sp + fall_through + nl;
      res += else_lbl + sp + "NOOP" + nl + (String)n.f6.accept(this, argu);
      res += fall_through + sp + "NOOP" + nl;
      return res ;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public Object visit(WhileStatement n, Object argu) {
      Object res ="";
      
      String s_lab = "L_" + doof.getL();
      String e_lab = "L_" + doof.getL();
      res = s_lab + sp + "CJUMP" + sp;
      res += ((Pair)n.f2.accept(this, argu)).f; //assuming all expressions have a default endline
      res += sp + e_lab + nl;
      res += (String)n.f4.accept(this, argu);
      res += "JUMP" + sp + s_lab + nl;
      res += e_lab + sp + "NOOP" + nl;
      return res ;
   }

   /**
    * f0 -> "//System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public Object visit(PrintStatement n, Object argu) {
      Object res ="";
      res = "PRINT" + nl;
      res += ((Pair)n.f2.accept(this, argu)).f;
      return res ;
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
   public Object visit(Expression n, Object argu) {
      Object res =null;
      res = n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> "("
    * f1 -> Identifier()
    * f2 -> ")"
    * f3 -> "->"
    * f4 -> Expression()
    */
   boolean in_lam = false;
   //String lam_var = null;
   public Object visit(LambdaExpression n, Object argu) {
      Pair p = null;
      String res = null;
      String l_decl = "";
      String type = "int#"; //this ia all we need currently, bcz we assume it is typechecked
      //this stuff is for lambdas alone
      lambda_cnt++;
      String apply_name = "apply_" + lambda_cnt;
     
      boolean prev_lam = in_lam;
      in_lam = true;
      String lam_var = n.f1.f0.toString();

      l_vars.add(lam_var);
      
      l_decl += apply_name + sp + "[2]" + nl;
      l_decl += "BEGIN" + nl;
      //you send in the value of the apply parameter into "this" first
      String this_obj = "TEMP" + sp + doof.getG();
      //String arr_obj = "TEMP" + sp + doof.getG();
      l_decl += "HLOAD" + sp + this_obj + sp + "TEMP 0" + sp + "4" + nl;
      l_decl += "HSTORE" + sp + this_obj + sp + 4*(l_vars.size() - 1) + sp + "TEMP 1" + nl;
      l_decl += "RETURN" + nl;
      Pair temp = (Pair)n.f4.accept(this, argu);
      l_decl += temp.f; //this returns the exp
      type += temp.s; //this is the return type of the lambda apply
      l_decl += "END" + nl;
      lambda_decl += l_decl;
      in_lam = prev_lam;

      String arr = "TEMP" + sp + doof.getG();
      String vt = "TEMP" + sp + doof.getG();
      String prev_arr = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      int count = ((FunInfo)argu).parameters.size() + ((FunInfo)argu).variables.size();
      res += "MOVE" + sp + arr + sp + "HALLOCATE" + sp + 4*(count + 3) + nl; //0th is the vtable, 1st is the prev var, 2nd is class, 3rd onwards it's the function methods
      res += "MOVE" + sp + vt + sp + "HALLOCATE" + sp + "4" + nl;
      //res += "HSTORE" + sp + arr + sp + "4" + sp + "TEMP 0" + nl;
      for (int i = 0; i <= count; i++) {
         res += "HSTORE" + sp + arr + sp + 4*(i + 2) + sp + "TEMP" + sp + i + nl; 
      }
      res += "HSTORE" + sp + vt + sp + "0" + sp + apply_name + nl;
      res += "HSTORE" + sp + arr + sp + "0" + sp + vt + nl;
      // for (var x: l_vars) {
      //    //System.out.print(x + " ");
      // }
      // //System.out.println();
      res += "MOVE" + sp + prev_arr + sp + "HALLOCATE" + sp + 4*(l_vars.size()) + nl;
      int len = l_vars.size();
      if (len > 1) { //possible only when there is a lambda outside, in which case this will be in the apply body
         String pre_var_arr = "TEMP" + sp + doof.getG();
         res += "HLOAD" + sp + pre_var_arr + sp + "TEMP 0" + sp + "4" + nl;
         for (int i = 0; i < len - 1; i++) { //you store all the variables from outer-lambdas
            String temp_temp = "TEMP" + sp + doof.getG();
            String val = "BEGIN" + nl;
            val += "HLOAD" + sp + temp_temp + sp + pre_var_arr + sp + 4*i + nl;
            val += "RETURN" + nl;
            val += temp_temp + nl + "END" + nl;

            res += "HSTORE" + sp + prev_arr + sp + 4*i + sp + val + nl; 

         }
      }
      res += "HSTORE" + sp + prev_arr + sp + 4*(len - 1) + sp + 0 + nl; //null initialization
      res += "HSTORE" + sp + arr + sp + "4" + sp + prev_arr + nl;
      res += "RETURN" + nl;
      res += arr + nl;
      res += "END" + nl;

      // //System.out.println("====");
      // //System.out.println(res);
      // //System.out.println("====");
      
      l_vars.remove(lam_var);

      p = new Pair(res, type);
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public Object visit(AndExpression n, Object argu) {
      Pair p = null;
      String res = null;
      String exp1 = "TEMP" + sp + doof.getG();
      String exp2 = "TEMP" + sp + doof.getG();
      String fin_bool = "TEMP" + sp + doof.getG();
      String fls_lbl = "L_" + doof.getL();
      String fin_lbl = "L_" + doof.getL();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + exp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "CJUMP" + sp + exp1 + sp + fls_lbl + nl;
      res += "MOVE" + sp + exp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "CJUMP" + sp + exp2 + sp + fls_lbl + nl;
      res += "MOVE" + sp + fin_bool + sp + "1" + nl;
      res += "JUMP" + sp + fin_lbl + nl;
      res += fls_lbl + nl;
      res += "MOVE" + sp + fin_bool + sp + "0" + nl;
      res += fin_lbl + sp + "NOOP" + nl;
      res += "RETURN" + nl;
      res += fin_bool + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "||"
    * f2 -> PrimaryExpression()
    */
   public Object visit(OrExpression n, Object argu) {
      Pair p = null;
      String res = null;
      String exp1 = "TEMP" + sp + doof.getG();
      String exp2 = "TEMP" + sp + doof.getG();
      String fin_bool = "TEMP" + sp + doof.getG();
      String fls_lbl = "L_" + doof.getL();
      String nxt_lbl = "L_" + doof.getL();
      String fin_lbl = "L_" + doof.getL();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + exp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "CJUMP" + sp + exp1 + sp + nxt_lbl + nl;
      res += "MOVE" + sp + fin_bool + sp + 1 + nl;
      res += "JUMP" + sp + fin_lbl + nl;
      res += nxt_lbl + nl;
      res += "MOVE" + sp + exp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += sp + "CJUMP" + sp + exp2 + sp + fls_lbl + nl;
      res += "MOVE" + sp + fin_bool + sp + "1" + nl;
      res += "JUMP" + sp + fin_lbl + nl;
      res += fls_lbl + nl;
      res += "MOVE" + sp + fin_bool + sp + "0" + nl;
      res += fin_lbl + sp + "NOOP" + nl;
      res += "RETURN" + nl;
      res += fin_bool + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<="
    * f2 -> PrimaryExpression()
    */
   public Object visit(CompareExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "LE" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "!="
    * f2 -> PrimaryExpression()
    */
   public Object visit(neqExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "NE" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public Object visit(AddExpression n, Object argu) {
      Pair p = null;
      String res =null;
      ////System.out.println(lam_var);
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "PLUS" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p; 
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public Object visit(MinusExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "MINUS" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p; 
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public Object visit(TimesExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "TIMES" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;  
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "/"
    * f2 -> PrimaryExpression()
    */
   public Object visit(DivExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String temp1 = "TEMP" + sp + doof.getG();
      String temp2 = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp1 + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + temp2 + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "RETURN" + nl;
      res += "DIV" + sp + temp1 + sp + temp2 + nl; 
      res += "END" + nl;
      p = new Pair(res, "int");
      return p; 
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public Object visit(ArrayLookup n, Object argu) {
      Pair p =null;
      String arr_temp = "TEMP" + sp + doof.getG();
      String ind_temp = "TEMP" + sp + doof.getG();
      String fin_val = "TEMP" + sp + doof.getG();
      String res = "BEGIN" + nl;
      res += "MOVE" + sp + arr_temp + sp + ((Pair)n.f0.accept(this, argu)).f;
      res += "MOVE" + sp + ind_temp + sp + ((Pair)n.f2.accept(this, argu)).f;
      res += "MOVE" + sp + ind_temp + sp + "TIMES" + sp + ind_temp + sp + 4 + nl;
      res += "MOVE" + sp + ind_temp + sp + "PLUS" + sp + ind_temp + sp + "4" + nl;
      res += "MOVE" + sp + arr_temp + sp + "PLUS" + sp +  arr_temp + sp + ind_temp + nl;
      res += "HLOAD" + sp + fin_val + sp + arr_temp + sp + "0" + nl;
      res += "RETURN" + nl;
      res += fin_val + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public Object visit(ArrayLength n, Object argu) {
      Pair p = null;
      String res =null;
      String temp = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp + sp + ((Pair)n.f0.accept(this, argu)).f + nl;
      res += "HLOAD" + sp + temp + sp + temp + sp + "0" + nl;
      res += "RETURN" + nl;
      res += temp + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p; 
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public Object visit(MessageSend n, Object argu) {
      Pair p = null;
      String res = null;
      String obj_temp = "TEMP" + sp + doof.getG();
      String temp_temp = "TEMP" + sp + doof.getG();
      String mtd_temp = "TEMP" + sp + doof.getG();
      String fun_called = n.f2.f0.toString(); //this is the function called
      res = "BEGIN" + nl;
      Pair obj_ret = (Pair)n.f0.accept(this, argu); //this is the object returned by the primary expr
      res += "MOVE" + sp + obj_temp + sp + obj_ret.f;
      res += "HLOAD" + sp + temp_temp + sp + obj_temp + sp + "0" + nl;
      String type = null;
      // //System.out.println("type: " + obj_ret.s + " Function: " + n.f2.f0.toString());
      // //System.out.println();
      if (obj_ret.s.contains("#")) {
         res += "HLOAD" + sp + mtd_temp + sp + temp_temp + sp + "0" + nl;
         String[] parts = obj_ret.s.split("#", 2);
         type = parts[1];
      }
      else {
         //System.out.println(obj_ret.s);
         res += "HLOAD" + sp + mtd_temp + sp + temp_temp + sp + find_fun(obj_ret.s, fun_called) + nl;
         type = fun_type(obj_ret.s, fun_called);
      }
      res += "RETURN" + nl;
      res += "CALL" + sp + mtd_temp + sp + "(" + sp + obj_temp + nl;
      String t = (String)n.f4.accept(this, argu);
      res += (t == null) ? "" : t;
      res += " )" + nl;
      res += "END" + nl;
      p = new Pair(res, type);
      return p;
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public Object visit(ExpressionList n, Object argu) { //will just return a string here
      String res =null;
      String var = ((Pair)n.f0.accept(this, argu)).f;
      res = (var == null) ? "" : var;
      String t = (String)n.f1.accept(this, argu);
      res += (t == null) ? "" : t;
      return res;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public Object visit(ExpressionRest n, Object argu) {
      String res =null;
      n.f0.accept(this, argu);
      String var = ((Pair)n.f1.accept(this, argu)).f;
      res = (var == null) ? "" : var;
      return res ;
   }

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
   public Object visit(PrimaryExpression n, Object argu) {
      Object res =null;
      ////System.out.println(argu);
      ////System.out.println(lam_var + " a");
      res = n.f0.accept(this, argu);
      return res ;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Object visit(IntegerLiteral n, Object argu) {
      Pair p = null;
      String res =null;
      res = "BEGIN" + nl;
      res += "NOOP" + nl + "RETURN" + nl;
      n.f0.accept(this, argu);
      res += n.f0.toString() + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> "true"
    */
   public Object visit(TrueLiteral n, Object argu) {
      Pair p = null;
      String res =null;
      res = "BEGIN" + nl;
      res += "NOOP" + nl + "RETURN" + nl;
      n.f0.accept(this, argu);
      res += "1" + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> "false"
    */
   public Object visit(FalseLiteral n, Object argu) {
      Pair p = null;
      String res =null;
      res = "BEGIN" + nl;
      res += "NOOP" + nl + "RETURN" + nl;
      n.f0.accept(this, argu);
      res += "0" + nl;
      res += "END" + nl;
      p = new Pair(res, "int");
      return p;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   //we come here directly when it is not an assignment, this must be handled differently
   public Object visit(Identifier n, Object argu) {
      Pair p = null;
      String type = null;
      String res = null;
      String temp = "TEMP" + sp + doof.getG(); //holds my final value 
      String id = n.f0.toString();
      ////System.out.println(lam_var + " a");
      res = "BEGIN" + nl;
      if (in_lam) {
         if (l_vars.contains(id)) {
            String prev_arr = "TEMP" + sp + doof.getG();
            String req_obj = "TEMP" + sp + doof.getG();
            res += "HLOAD" + sp + prev_arr + sp + "TEMP 0" + sp + 4 + nl;
            res += "HLOAD" + sp + req_obj + sp + prev_arr + sp + 4*(l_vars.indexOf(id)) + nl;
            res += "MOVE" + sp + temp + sp + req_obj + nl; 
         }
         else if (argu == null) return null;
         else {
            String var_ind = doof.find_var_in_fun(id, (FunInfo)argu);
            if (var_ind != null) { //basically we found the variable in the method itself
               String[] parts = var_ind.split(" ", 2);
               ////System.out.println(parts[1]);
               parts[1] = Integer.toString(4*(Integer.parseInt(parts[1]) + 2));
               String this_obj = "TEMP" + sp + doof.getG();
               res += "MOVE" + sp + this_obj + sp + "TEMP 0" + nl;
               res += "HLOAD" + sp + temp + sp + this_obj + sp + parts[1] + nl;
               // type = doof.fun_type(((FunInfo)argu).getC(), (FunInfo)argu., classes, methods);
               type = ((FunInfo)argu).type(id);
            }
            else {
               ////System.out.println(id + " " + );
               String[] parts = doof.find_var_in_cls(id, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes).split(" ");
               String this_obj = "TEMP" + sp + doof.getG();
               String cls_obj_lam = "TEMP" + sp + doof.getG();
               res += "MOVE" + sp + this_obj + sp + "TEMP 0" + nl;
               res += "HLOAD" + sp + cls_obj_lam + sp + this_obj + sp + "8" + nl;
               res += "HLOAD" + sp + temp + sp + cls_obj_lam + sp + parts[2] + nl;
               type = doof.type_in_cls(id, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes);
            }
         }
      }
      else  if (argu == null) return null;
      else {
         String var_ind = doof.find_var_in_fun(id, (FunInfo)argu);
         if (var_ind != null) { //basically we found the variable in the method itself
            res += "MOVE" + sp + temp + sp + var_ind + nl;
            type = ((FunInfo)argu).type(id);
         }
         else {
            res += "HLOAD" + sp + temp + sp + doof.find_var_in_cls(id, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes) + nl;
            type = doof.type_in_cls(id, ((FunInfo)argu).getC(), ((FunInfo)argu).getC(), classes);
         }
      }
      ////System.out.println(argu + " aaaaa");
      res += "RETURN" + nl;
      res += temp + nl;
      res += "END" + nl;
      p = new Pair(res, type);
      return p;
   }

   /**
    * f0 -> "this"
    */
   public Object visit(ThisExpression n, Object argu) {
      Pair p = null;
      String type = ((FunInfo)argu).class_name;
      String res = null;
      String temp = "TEMP" + sp + doof.getG();
      res = "BEGIN" + nl;
      if (in_lam) {
         String this_obj = "TEMP" + sp + doof.getG();
         res += "MOVE" + sp + this_obj + sp + "TEMP 0" + nl;
         res += "HLOAD" + sp + temp + sp + this_obj + sp + "8" + nl;
      }
      else {
          res += "MOVE" + sp + temp + sp + "TEMP 0" + nl;
      }
      res += "RETURN" + nl;
      res += temp + nl;
      res += "END" + nl;
      p = new Pair(res, type);
      return p;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public Object visit(ArrayAllocationExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String arr = "TEMP" + sp + doof.getG();
      String size = "TEMP" + sp + doof.getG();

      res = "BEGIN" + nl;
      res += "MOVE" + sp  + size + sp + ((Pair)n.f3.accept(this, argu)).f + nl; //this puts value of size in size
      res += "MOVE" + sp + arr + sp;
      res += arr_alc(size);
      res += "RETURN" + nl;
      res += arr + nl;
      res += "END" + nl;
      p = new Pair(res, "int_array");
      return p;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public Object visit(AllocationExpression n, Object argu) {
      Pair p = null;
      String res =null;
      String cls = n.f1.f0.toString();
      res = cls_exp(cls);
      p = new Pair(res, n.f1.f0.toString());
      return p;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   public Object visit(NotExpression n, Object argu) {
      Pair p = null;
      String res =null;
      n.f0.accept(this, argu);
      String temp = "TEMP" + sp + doof.getG();
      String t_lab = "L_" + doof.getL();
      String fall_through = "L_" + doof.getL();
      res = "BEGIN" + nl;
      res += "MOVE" + sp + temp + sp + ((Pair)n.f1.accept(this, argu)).f;
      res += "CJUMP LE" + sp + temp + sp + "0" + sp + t_lab + nl; //if it is less than 0, it must be false...so we go to true label other wise
      res += "MOVE" + sp + temp + sp + "BEGIN" + nl  + "NOOP" + nl + "RETURN" + nl + "1" + nl + "END" + nl;
      res += "JUMP" + sp + fall_through + nl;
      res += t_lab + nl + "MOVE" + sp + temp + sp + "BEGIN" + nl  + "NOOP" + nl + "RETURN" + nl + "0" + nl + "END" + nl;
      res += fall_through + sp + "NOOP" + nl;
      res += "RETURN" + nl;
      res += temp + nl;
      res += "END" + nl;
     

      p = new Pair(res, "int"); //since boolean is same as int currently
      return p;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public Object visit(BracketExpression n, Object argu) {
      Object res =null;
      n.f0.accept(this, argu);
      res = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return res ;
   }
   
}
