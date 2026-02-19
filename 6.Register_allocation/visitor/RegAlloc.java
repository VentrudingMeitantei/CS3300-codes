package visitor;
import syntaxtree.*;

import java.util.*;

class AddFuns {
    
    public void find_in_out(ArrayList<ArrayList<Integer>> cfg, ArrayList<BasicBlock> bbs) {
       int n = bbs.size();
       do {
           boolean changed = false;
           for (int i = 0; i < n; i++) {
               HashSet<String> prev_in = new HashSet<>(bbs.get(i).live_in);
               HashSet<String> prev_out = new HashSet<>(bbs.get(i).live_out);

               HashSet<String> new_out = new HashSet<>();
               for (int s : cfg.get(i)) {
                   if (s < 0) continue;
                   new_out.addAll(bbs.get(s).live_in);
               }

               HashSet<String> new_in = new HashSet<>(new_out);
               new_in.removeAll(bbs.get(i).def);
               new_in.addAll(bbs.get(i).used);

               bbs.get(i).live_in = new_in;
               bbs.get(i).live_out = new_out;

               if (!new_in.equals(prev_in) || !new_out.equals(prev_out))
                   changed = true;
           }

           if (!changed) break;

       } while (true);
   }


    public void make_graph(Proc proc) {
        int n = proc.bbs.size();
        for (int i = 0; i < n; i++) {
            ArrayList<Integer> curr_bb = new ArrayList<>();
            if (proc.bbs.get(i).is_cj) {
                if (i != n - 1) curr_bb.add(i + 1);
                else curr_bb.add(-1);
                curr_bb.add(proc.lbl_map.get(proc.bbs.get(i).fls_jmp));
            }
            else if (proc.bbs.get(i).is_j) {
                curr_bb.add(proc.lbl_map.get(proc.bbs.get(i).target));
            }
            else {
                if (i != n - 1) curr_bb.add(i + 1);
                else curr_bb.add(-1);
            }
            proc.cfg.add(curr_bb);
        }
    }

    public void print(ArrayList<ArrayList<Integer>> cfg) {
        int n = cfg.size();
        for (int i = 0; i < n; i++) {
            System.err.print(i + ":");
            for (var v: cfg.get(i)) {
                System.err.print(v + " ");
            }
            System.err.println();
        }
    }
    
    public void find_live_ranges(HashMap<String, Pair> live_ranges, HashSet<String> vars, ArrayList<BasicBlock> bbs) {
         int n = bbs.size();
         for (var v: vars) {
            live_ranges.put(v, new Pair());
         }
         for (int i = 0; i < n; i++) {
            System.err.println("======" + i + "======");
            for (var v: live_ranges.entrySet()) {
               String temp_name = v.getKey();
               if (!(bbs.get(i).live_in.contains(temp_name) || bbs.get(i).live_out.contains(temp_name))) continue;
               v.getValue().st = Integer.min(v.getValue().st, i);
               v.getValue().en = Integer.max(v.getValue().en, i);
               System.err.println(temp_name + " " + v.getValue().st + " " + v.getValue().en);
            }
            System.err.println("==============");
         }
    }

    int extract_int(String temp) {
         String[] parts = temp.split(" ", 2);
         return Integer.parseInt(parts[1]);
    }

    public void make_live_range_array(HashMap<String, Pair> live_ranges, ArrayList<WrapperPair> var_data, int param) {
         for (var v: live_ranges.entrySet()) {
            if (extract_int(v.getKey()) < param) continue;
            var_data.add(new WrapperPair(v.getValue().st, v.getValue().en, v.getKey()));
         }
         var_data.sort((a, b) -> {
                                     if (a.range.st != b.range.st) return Integer.compare(a.range.st, b.range.st);
                                     return Integer.compare(a.range.en, b.range.en);
                                 });
    }

    public void find_vars(HashSet<String> vars, ArrayList<BasicBlock> bbs) {
         for (var v: bbs) {
            vars.addAll(v.used);
            vars.addAll(v.def);  
         }
    }

    public void initializations(Proc p) {
         find_vars(p.vars, p.bbs);
         for (var v: p.vars) {
            int i = extract_int(v);
            if (i < p.params) {
               p.loc.put(v, "#" + i);
            }
            else p.loc.put(v, null);
         }
    }

    public void linear_scan_algo(Proc p) {
         //System.err.println(p.proc_name);
         TreeSet<WrapperPair> active = new TreeSet<>(
                                                      (a, b) -> {
                                                          if (a.range.en != b.range.en) return Integer.compare(a.range.en, b.range.en);
                                                          return Integer.compare(a.range.st, b.range.st);
                                                      }
                                                    );
         int n = p.var_data.size();
         for (int i = 0; i < n; i++) {
            //System.err.println(active.size());
            expire_old_intervals(i, p, active);
            //System.err.println(active.size());
            if (active.size() >= 18) {
               spill_at_intervals(i, p, active);
            }
            else {
               int idx = p.find_reg();
               //System.err.println(p.var_data.get(i).temp + " " + idx);
               p.put_in_reg(idx, p.var_data.get(i).temp);
               //System.err.println("Putting in reg " + p.var_data.get(i).temp);
               active.add(p.var_data.get(i));
            }
         }
         
    }

    public void expire_old_intervals(int i, Proc p, TreeSet<WrapperPair> active) {
         Iterator<WrapperPair> it = active.iterator();
         while (it.hasNext()) {
            WrapperPair curr = it.next();
            if (curr.range.en >= p.var_data.get(i).range.st) {
               return;
            }
            String temp = curr.temp;
            it.remove();
            //System.err.println("Removing " + temp);
            p.restore_reg(temp);
         }
    }
    
    public void spill_at_intervals(int i, Proc p, TreeSet<WrapperPair> active) {
        WrapperPair spill = active.last();
        p.is_spilled = true;
        if (spill.range.en > p.var_data.get(i).range.en) {
            String reg_name = p.loc.get(spill.temp);
            p.restore_reg(spill.temp);
            //System.err.print("Spilling " + spill.temp);
            p.spill(spill.temp);
            active.remove(spill);
            p.put_in_reg(reg_name, p.var_data.get(i).temp);
            active.add(p.var_data.get(i));
        }
        else {
            //System.err.print("Spilling " + p.var_data.get(i).temp);
            p.spill(p.var_data.get(i).temp);
        }
    }
}

public class RegAlloc extends GJDepthFirst<Object, Object> {
   ArrayList<Proc> procedures = new ArrayList<>();
   AddFuns madhav = new AddFuns();
   public void print() {
      for (var v: procedures) {
        System.err.println("########## " + v.proc_name + " ##########");
        v.print();
        System.err.println();
      }
   } 
   public void make_graph() {
      for (var v: procedures) {
        madhav.make_graph(v);
      }
   }

   public void gen_io() {
      for (var v: procedures) {
         madhav.find_in_out(v.cfg, v.bbs);
      }
   }

   public void init() {
      for (var v: procedures) {
         madhav.initializations(v);;
      }
   }

   public void find_live_ranges() {
      for (var v: procedures) {
         System.err.println("++++++" + v.proc_name + "++++++");
         madhav.find_live_ranges(v.live_ranges, v.vars, v.bbs);
         madhav.make_live_range_array(v.live_ranges, v.var_data, v.params);
         System.err.println("++++++++++++++++++");
      }
   }

   public void linear_scan() {
      for (var v: procedures) {
         madhav.linear_scan_algo(v);
      }
   }

   public void print_graph() {
      for (var v: procedures) {
        System.err.println("####");
        madhav.print(v.cfg);
        System.err.println("####");
      }
   }
   /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public Object visit(Goal n, Object argu) {
      Object res = null;

      Proc curr_proc = new Proc();
      procedures.add(curr_proc);
      curr_proc.set_nm("MAIN");
      curr_proc.set_prm(0);

      n.f1.accept(this, curr_proc);
      
      n.f3.accept(this, curr_proc);
    
      make_graph();
      gen_io();
      init();
      find_live_ranges();
      linear_scan();

      return res;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public Object visit(StmtList n, Object argu) {
      Object res = null;

      if (n.f0 != null) {
           for (Enumeration<Node> e = n.f0.elements(); e.hasMoreElements();) {
               Node node = e.nextElement();        // each is (Label()? Stmt())  
               NodeSequence seq = (NodeSequence) node;  
               // Label (optional)
               NodeOptional maybeLabel = (NodeOptional) seq.elementAt(0);
               if (maybeLabel.present()) {
                   Label label = (Label) maybeLabel.node;
                   BasicBlock curr_bb = new BasicBlock();
                   ((Proc)argu).add_bb(curr_bb);
                   curr_bb.is_lbl = true;
                   int idx = ((Proc)argu).bbs.size() - 1; //this is where the label is gonna go
                   String lam_name = (String)label.accept(this, argu);        // or call your logic directly
                   ((Proc)argu).lbl_map.put(lam_name, idx);
               }  
               
               Stmt stmt = (Stmt) seq.elementAt(1);
               stmt.accept(this, argu);             // or perform your operation
           }
      }

      return res;
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
   public Object visit(Procedure n, Object argu) {
      Object res = null;
      Proc curr_proc = new Proc();
      procedures.add(curr_proc);

      String name = (String)n.f0.accept(this, curr_proc);
      curr_proc.set_nm(name);
      int params = Integer.parseInt((String)n.f2.accept(this, curr_proc));
      curr_proc.set_prm(params);

      n.f4.accept(this, curr_proc);

      
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
    */
   public Object visit(Stmt n, Object argu) {
      Object res = null;
      
      n.f0.accept(this, argu);

      return res;
   }

   /**
    * f0 -> "NOOP"
    */
   public Object visit(NoOpStmt n, Object argu) {
      Object res = null;
      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb);
      curr_bb.is_np = true;
      return res;
   }

   /**
    * f0 -> "ERROR"
    */
   public Object visit(ErrorStmt n, Object argu) {
      Object res = null;
      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb);
      curr_bb.is_er = true;
      return res;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
   public Object visit(CJumpStmt n, Object argu) {
      Object res = null;

      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb);
      curr_bb.is_cj = true;
      curr_bb.put_u((String)n.f1.accept(this, argu));
      curr_bb.fls_jmp = (String)n.f2.accept(this, argu);

      return res;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public Object visit(JumpStmt n, Object argu) {
      Object res = null;

      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb); 
      curr_bb.is_j = true;
      curr_bb.target = (String)n.f1.accept(this, argu);

      return res;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
   public Object visit(HStoreStmt n, Object argu) {
      Object res = null;
    
      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb); 
      
      curr_bb.put_u((String)n.f1.accept(this, argu));
      curr_bb.put_u((String)n.f3.accept(this, argu));

      return res;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
   public Object visit(HLoadStmt n, Object argu) {
      Object res = null;
      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb); 
      
      curr_bb.put_d((String)n.f1.accept(this, argu));
      curr_bb.put_u((String)n.f2.accept(this, argu));

      return res;
   }

   //should see what happens with move, there' more than what I wrote
   /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public Object visit(MoveStmt n, Object argu) {
      Object res = null;

      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb); 
      
      curr_bb.put_d((String)n.f1.accept(this, argu));
      n.f2.accept(this, curr_bb);

      return res;
   }

   //this also has some weird stuff
   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public Object visit(PrintStmt n, Object argu) {
      Object res = null;

      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb); 

      n.f1.accept(this, curr_bb);

      return res;
   }

   /**
    * f0 -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    */
   public Object visit(Exp n, Object argu) {
      Object res = null;
      n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
   public Object visit(StmtExp n, Object argu) {
      Object res = null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      BasicBlock curr_bb = new BasicBlock();
      ((Proc)argu).add_bb(curr_bb);
      n.f3.accept(this, curr_bb);
      n.f4.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public Object visit(Call n, Object argu) {
      Object res = null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public Object visit(HAllocate n, Object argu) {
      Object res = null;

      n.f1.accept(this, argu);
      
      return res;
   }

   /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public Object visit(BinOp n, Object argu) {
      Object res = null;
      
      ((BasicBlock)argu).put_u((String)n.f1.accept(this, argu));
      n.f2.accept(this, argu);
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
      n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
   public Object visit(SimpleExp n, Object argu) {
      Object res = null;
      res = n.f0.accept(this, argu);
      return res;
   }

   /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public Object visit(Temp n, Object argu) {
      Object res = null;
      res = "TEMP ";
      res += (String) n.f1.accept(this, argu);
      if (argu.toString().equals("BasicBlock")) {
        ((BasicBlock)argu).put_u((String)res);
      }
      return res;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public Object visit(IntegerLiteral n, Object argu) {
      Object res = null;
      res = n.f0.toString();
      return res;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public Object visit(Label n, Object argu) {
      Object res = null;
      res = n.f0.toString();
      return res;
   }
}

//  //
//    // Auto class visitors--probably don't need to be overridden.
//    //
//    public Object visit(NodeList n, Object argu) {
//       Object res = null;
//       int _count = 0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeListOptional n, Object argu) {
//       if ( n.present() ) {
//          Object res = null;
//          int _count = 0;
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
//       int _count = 0;
//       for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
//          e.nextElement().accept(this,argu);
//          _count++;
//       }
//       return res;
//    }

//    public Object visit(NodeToken n, Object argu) { return null; }

