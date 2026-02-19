package visitor;
import syntaxtree.*;
import java.util.*;

class Simplex {
   String type;
   String val;
   
   public Simplex(String t, String v) {
      type = t;
      val = v;
   }
}

class Binop {
   String op;
   String t1, t2;
   String precode, postcode;
   String stored;
   boolean reg_flag; //if we are doing operation between registers, we don't need too much drama
   public Binop() {
      this.op = "";
      this.t1 = "";
      this.t2 = "";
      this.stored = "";
      precode = "";
      postcode = "";
      reg_flag = false;
   }

   void set_prc(Simplex sx) {
      if (!reg_flag) return;
      precode += "sw " + stored + ", " + "-4($sp)\n";
      if (sx.type.equals("id")) precode += "la " + stored + ", " + sx.val + "\n";
      if (sx.type.equals("int")) precode += "li " + stored + ", " + sx.val + "\n";
      t2 = stored;
   }

   String pre() {
      return precode;
   }

   String pos(String reg) {
      if (!reg_flag) return "";
      if (!reg.equals(stored)) postcode += "lw " + stored + ", " + "-4($sp)\n";
      return postcode;
   }
}

class Halloc {
   String reg;
   Simplex p;

   public Halloc(Simplex p) {
      this.p = p;
   }

   String code() {
      String code = "";
      code += "sw $a0, -4($sp)\n";
      code += "sw $v0, -8($sp)\n";
      if (p.type.equals("int")) {
         code += "li $a0, " + p.val + "\n";
      }
      else if (p.type.equals("reg")) {
         code += "move $a0, " + p.val + "\n";
      }
      else if (p.type.equals("id")) {
         code += "la $a0, " + p.val + "\n"; 
      }
      code += "li $v0, 9\n" + //
              "syscall\n"  +
              "move " + reg + ", $v0" + "\n";
      if (!reg.equals("$a0"))code += "lw $a0, -4($sp)\n";
      if (!reg.equals("$v0")) code += "lw $v0, -8($sp)\n";
      return code;
   }
}

class Functions {
   String op_conv(String op) {
       if (op.equals("LE")) {
         op = "sle";
      }
      else if (op.equals("NE")) {
         op = "sne";
      }
      else if (op.equals("PLUS")) {
         op = "add";
      }
      else if (op.equals("MINUS")) {
         op = "sub";
      }
      else if (op.equals("TIMES")) {
         op = "mul";
      }
      else if (op.equals("DIV")) {
         op = "div";
      }
      return op;
   }

   String end_fns() {
      String res = remaining();
      return res;
   }

   String remaining() {
      String res = "\n" + //
                  ".data\n" + //
                  ".align 0\n" + //
                  "newl: .asciiz \"\\n" + //
                  "\"\n" + //
                  ".data\n" + //
                  ".align 0\n" + //
                  "str_er: .asciiz \"ERROR: abnormal termination\\n" + //
                  "\"";
      return res;
   }
}

public class MIPSemitter extends GJDepthFirst<Object, Object> {
   Functions fns = new Functions();
   
   /**
    * f0 -> "MAIN"
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    * f13 -> ( Procedure() )*
    * f14 -> <EOF>
    */
   public Object visit(Goal n, Object argu) {
      Object res = "";
      res += ".text\n" + //
             ".globl main\n" + //
             "main:\n";

      res += "move $fp, $sp\n" + //
             "sw $ra, -4($fp)\n";
   
      String i1 = ((Simplex)n.f2.accept(this, argu)).val;
      String i2 = ((Simplex)n.f5.accept(this, argu)).val;
      int stack_alloc = Integer.parseInt(i1) + Integer.parseInt(i2) + 2;

      res += "subu $sp, $sp, " + 4*stack_alloc + "\n";
      res += (String)n.f10.accept(this, argu);
      res += "addu $sp, $sp, " + 4*stack_alloc + "\n" + //
             "lw $ra, -4($fp)\n"; 
       res +=      "li $v0, 10\n syscall\n";
      //res += (String)n.f13.accept(this, argu);
       if (n.f13 != null) {
        for (Enumeration<Node> e = n.f13.elements(); e.hasMoreElements();) {
            Node node = e.nextElement();
            res += (String)node.accept(this, argu);
            //System.out.println("A");
        }
      }
      res += fns.end_fns();
      return res;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public Object visit(StmtList n, Object argu) {
      Object res = "";
      
      if (n.f0 != null) {
           for (Enumeration<Node> e = n.f0.elements(); e.hasMoreElements();) {
               Node node = e.nextElement();        // each is (Label()? Stmt())  
               NodeSequence seq = (NodeSequence) node;  
               NodeOptional maybeLabel = (NodeOptional) seq.elementAt(0);
               if (maybeLabel.present()) {
                   Label label = (Label) maybeLabel.node;
                   Simplex lam_name = (Simplex)label.accept(this, argu);        // or call your logic directly
                   res += lam_name.val + ":\n";
               }  
               Stmt stmt = (Stmt) seq.elementAt(1);
               res += (String)stmt.accept(this, argu);
           }
      }

      return res;

   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    */
   public Object visit(Procedure n, Object argu) {
      Object res = "";
      String lbl = ((Simplex)n.f0.accept(this, argu)).val;
      String i1 = ((Simplex)n.f2.accept(this, argu)).val;
      String i2 = ((Simplex)n.f5.accept(this, argu)).val;
      int stack_alloc = Integer.parseInt(i1) + Integer.parseInt(i2) + 2;


      res += ".text\n" + //
             ".globl " + lbl + "\n";
      res += lbl + ":\n";
      res += "sw $fp, -8($sp)\n" + //
             "move $fp, $sp\n" + //
             "sw $ra, -4($fp)\n" + //
             "subu $sp, $sp, "  + 4*stack_alloc + "\n";


      

      for (int i = 0; i < Integer.parseInt(i1) - 4; i++) {
         res += "lw $v0, -" + 4*(i + 3) + "($fp)\n";
         res += "sw $v0, " + 4*(i) + "($sp)\n";
      }
      
      res += (String)n.f10.accept(this, argu);

      res += "addu $sp, $sp, " + 4*stack_alloc + "\n" + "lw $ra, -4($sp)\n" +
             "lw $fp, -8($sp)\n" +     
            
             "jr $ra\n";
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
    *       | ALoadStmt()
    *       | AStoreStmt()
    *       | PassArgStmt()
    *       | CallStmt()
    */
   public Object visit(Stmt n, Object argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "NOOP"
    */
   public Object visit(NoOpStmt n, Object argu) {
      Object res = null;
      res = "nop\n";
      return res;
   }

   /**
    * f0 -> "ERROR"
    */
   public Object visit(ErrorStmt n, Object argu) {
      Object res = null;
      res = "li $v0, 4\n" +
            "la $a0, str_er\n" + 
            "syscall\n" + 
            "li $v0, 10\n" + 
            "syscall\n";
      return res;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Reg()
    * f2 -> Label()
    */
   public Object visit(CJumpStmt n, Object argu) {
      Object res = null;
      String reg = ((Simplex)n.f1.accept(this, argu)).val;
      String lbl = ((Simplex)n.f2.accept(this, argu)).val;
      res = "beqz " + reg + ", " + lbl + "\n";
      return res;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public Object visit(JumpStmt n, Object argu) {
      Object res = null;
      String lbl = ((Simplex)n.f1.accept(this, argu)).val;
      res = "b " + lbl + "\n";
      return res;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Reg()
    * f2 -> IntegerLiteral()
    * f3 -> Reg()
    */
   public Object visit(HStoreStmt n, Object argu) {
      Object res = null;

      String des = ((Simplex)n.f1.accept(this, argu)).val;
      String off = ((Simplex)n.f2.accept(this, argu)).val;
      String src = ((Simplex)n.f3.accept(this, argu)).val;
      
      res = "sw " + src + ", " + (Integer.parseInt(off)) + "(" + des + ")\n"; 
      return res;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Reg()
    * f2 -> Reg()
    * f3 -> IntegerLiteral()
    */
   public Object visit(HLoadStmt n, Object argu) {
      Object res = null;

      String des = ((Simplex)n.f1.accept(this, argu)).val;
      String src = ((Simplex)n.f2.accept(this, argu)).val;
      String off = ((Simplex)n.f3.accept(this, argu)).val;
      
      res = "lw " + des + ", " + (Integer.parseInt(off)) + "(" + src + ")\n";   
      return res;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Reg()
    * f2 -> Exp()
    */
   public Object visit(MoveStmt n, Object argu) {
      Object res = "";
     
      String reg = ((Simplex)n.f1.accept(this, argu)).val;
      int which = n.f2.f0.which;

      if (which == 0) { //hallocate
         Halloc h = (Halloc)n.f2.accept(this, argu);
         h.reg = reg;
         res += h.code();
      }

      else if (which == 1) { //binop
         Binop b = (Binop)n.f2.accept(this, argu);
         res += b.pre();
         res += b.op + " " + reg + ", " + b.t1 + ", " + b.t2 + "\n";
         res += b.pos(reg);
      }

      else { //simplex
         Simplex p = (Simplex)n.f2.accept(this, argu);
         if (p.type.equals("int")) {
            res += "li " + reg + ", " + p.val + "\n";
         }
         else if (p.type.equals("reg")) {
            res += "move " + reg + ", " + p.val + "\n";
         }
         else if (p.type.equals("id")) {
            res += "la " + reg + ", " + p.val + "\n"; 
         }

      }
      
      return res;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public Object visit(PrintStmt n, Object argu) {
      Object res = "";
      Simplex p = (Simplex)n.f1.accept(this, argu);
      res += "sw $a0, -4($sp)\n";
      res += "sw $v0, -8($sp)\n";
      if (p.type.equals("int")) {
         res += "li $a0, " + p.val + "\n";
      }
      else if (p.type.equals("reg")) {
         res += "move $a0, " + p.val + "\n";
      }
      else if (p.type.equals("id")) {
         res += "la $a0, " + p.val + "\n"; 
      }
      //res += "jal _print\n";
      res += "li $v0, 1\n" + //
                  "syscall\n" + //
                  "la $a0, newl\n" + //
                  "li $v0, 4\n" + //
                  "syscall\n";
      res += "lw $a0, -4($sp)\n";
      res += "lw $v0, -8($sp)\n";
      return res;
   }

   /**
    * f0 -> "ALOAD"
    * f1 -> Reg()
    * f2 -> SpilledArg()
    */
   public Object visit(ALoadStmt n, Object argu) {
      Object res = null;
     
      String reg = ((Simplex)n.f1.accept(this, argu)).val;
      String off = ((Simplex)n.f2.accept(this, argu)).val;

      res = "lw " + reg + ", " + (Integer.parseInt(off)*4) + "($sp)\n";
      return res;
   }

   /**
    * f0 -> "ASTORE"
    * f1 -> SpilledArg()
    * f2 -> Reg()
    */
   public Object visit(AStoreStmt n, Object argu) {
      Object res = null;

      String off = ((Simplex)n.f1.accept(this, argu)).val;
      String reg = ((Simplex)n.f2.accept(this, argu)).val;

      res = "sw " + reg + ", " + (Integer.parseInt(off)*4) + "($sp)\n";
      return res;
   }

   /**
    * f0 -> "PASSARG"
    * f1 -> IntegerLiteral()
    * f2 -> Reg()
    */
   public Object visit(PassArgStmt n, Object argu) {
      Object res = null;
      
      String off = ((Simplex)n.f1.accept(this, argu)).val;
      String reg = ((Simplex)n.f2.accept(this, argu)).val;

      res = "sw " + reg + ", -" + (Integer.parseInt(off) + 2)*4 + "($sp)\n"; //this is one indexed, and in first two locations we'll be storing fp and ra
      return res;
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    */
   public Object visit(CallStmt n, Object argu) {
      Object res = "";
      Simplex p = (Simplex)n.f1.accept(this, argu);
      if (p.type.equals("int")) {
         res += "li $v0, " + p.val + "\n";
      }
      else if (p.type.equals("reg")) {
         res += "move $v0, " + p.val + "\n";
      }
      else if (p.type.equals("id")) {
         res += "la $v0, " + p.val + "\n"; 
      }
      res += "jalr $v0\n";
      return res;
   }

   /**
    * f0 -> HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public Object visit(Exp n, Object argu) { 
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public Object visit(HAllocate n, Object argu) {
      Object res = null;
      Simplex p = (Simplex)n.f1.accept(this, argu);
      res = new Halloc(p);
      return res;
   }

   /**
    * f0 -> Operator()
    * f1 -> Reg()
    * f2 -> SimpleExp()
    */
   public Object visit(BinOp n, Object argu) {
      Object res = new Binop();
      String op = (String)n.f0.accept(this, argu);
      String t1 = ((Simplex)n.f1.accept(this, argu)).val;
      Simplex t2 = (Simplex)n.f2.accept(this, argu);
      String temp = (t1.equals("$v0")) ? "$v1" : "$v0";

      op = fns.op_conv(op);
      ((Binop)res).stored = temp; 
      ((Binop)res).op = op;
      ((Binop)res).t1 = t1; 
      ((Binop)res).t2 = t2.val; 
      if (!t2.type.equals("reg")) {
         ((Binop)res).reg_flag = true;
         ((Binop)res).set_prc(t2);
      }  
     
      return res;
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
      Object res = null;
      String op = ((NodeToken)n.f0.choice).tokenImage;
      res = op;
      return res;
   }

   /**
    * f0 -> "SPILLEDARG"
    * f1 -> IntegerLiteral()
    */
   public Object visit(SpilledArg n, Object argu) {
      return n.f1.accept(this, argu);
   }

   /**
    * f0 -> Reg()
    *       | IntegerLiteral()
    *       | Label()
    */
   public Object visit(SimpleExp n, Object argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "a0"
    *       | "a1"
    *       | "a2"
    *       | "a3"
    *       | "t0"
    *       | "t1"
    *       | "t2"
    *       | "t3"
    *       | "t4"
    *       | "t5"
    *       | "t6"
    *       | "t7"
    *       | "s0"
    *       | "s1"
    *       | "s2"
    *       | "s3"
    *       | "s4"
    *       | "s5"
    *       | "s6"
    *       | "s7"
    *       | "t8"
    *       | "t9"
    *       | "v0"
    *       | "v1"
    */
   public Object visit(Reg n, Object argu) {
      Object res = null;
      String reg = "$" + ((NodeToken)n.f0.choice).tokenImage;
      res = new Simplex("reg", reg);
      return res;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Object visit(IntegerLiteral n, Object argu) {
      Object res = null;
      String id = n.f0.toString();
      res = new Simplex("int", id);
      return res;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Object visit(Label n, Object argu) {
      Object res = null;
      String id = n.f0.toString();
      res = new Simplex("id", id);
      return res;
   }

   // /**
   //  * f0 -> "//"
   //  * f1 -> SpillStatus()
   //  */
   // public Object visit(SpillInfo n, Object argu) {
   //    Object res = null;
   //    n.f0.accept(this, argu);
   //    n.f1.accept(this, argu);
   //    return res;
   // }

   // /**
   //  * f0 -> <SPILLED>
   //  *       | <NOTSPILLED>
   //  */
   // public Object visit(SpillStatus n, Object argu) {
   //    Object res = null;
   //    n.f0.accept(this, argu);
   //    return res;
   // }

}

//   //
//    // Auto class visitors--probably don't need to be overridden.
//    //
//    public Object visit(NodeList n, Object argu) {
//       Object res = null;
//       int _count=0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeListOptional n, Object argu) {
//       if ( n.present() ) {
//          Object res = null;
//          int _count=0;
//          for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//             e.nextElement().accept(this,argu);
//             _count++;
//          }
//          return res;
//       }
//       else
//          return null;
//    }

//    public Object visit(NodeOptional n, Object argu) {
//       if ( n.present() )
//          return n.node.accept(this,argu);
//       else
//          return null;
//    }

//    public Object visit(NodeSequence n, Object argu) {
//       Object res = null;
//       int _count=0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeToken n, Object argu) { return null; }

