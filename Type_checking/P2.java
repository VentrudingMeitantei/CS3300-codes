import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import syntaxtree.*;
import visitor.*;

public class P2 {
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         FirstPass fp = new FirstPass();
         Graph gr = new Graph();
         Helper h = new Helper();
         root.accept(fp); // Your assignment part is invoked here.
         
         SecondPass sp = new SecondPass();
         sp.classes = fp.classes;
         
         if(!gr.makeGraph(sp.inheritance_graph, sp.classes)) {
            System.out.println("Symbol not found");
            return;
         }
         
         if (gr.dfs(sp.inheritance_graph , sp.classes)) {
            System.out.println("Type error");
            return;
         } 
         
         if (gr.check_ze_ovrld(sp.classes)) {
            System.out.println("Type error");
            return;
         }

         root.accept(sp);
         System.out.println("Program type checked successfully");
      }
      catch (ParseException e) {
         System.out.println("Type error");
      }
   }

}

class Helper { //only for printing stuff
   public void printClasses(ClassInfo cls) {
      //this prints classes
      System.out.println("====Class opens====");
      System.out.println("Class fields: ");
      for (var variables: cls.fields.entrySet()) {
         System.out.println("type: " + variables.getValue() + " name: " + variables.getKey());
      }
      System.out.println("Class methods: ");
      for (var methods: cls.methods.entrySet()) {
         System.out.println(methods.getKey());
         printMethods(methods.getValue());
      }
      System.out.println("====Class closes====");
      
   }

   public void printMethods(FunInfo fun) {
      System.out.println("---Method opens----");
      System.out.println("return type: " + fun.getRet());
      System.out.println("Signature");
      for (var entry: fun.signature) {
         System.out.print(entry + " ");
      }
      System.out.println();
      System.out.println("Method parameters: ");
      for (var params: fun.parameters.entrySet()) {
         System.out.println("type: " + params.getValue() + " name: " + params.getKey());
      }
      System.out.println("Method Variables: "); 
      for (var vars: fun.variables.entrySet()) {
         System.out.println("type: " + vars.getValue() + " name: " + vars.getKey());
      }
      System.out.println("---Method closes----");
   }
   
}

class Graph { //for making graph and traversing it
   public int t = 0;
   public boolean makeGraph(HashMap<String, ArrayList<String>> adj, HashMap<String, ClassInfo> classes) {
      for (var entry: classes.entrySet()) {
         String class_name = entry.getKey();
         ArrayList<String> list = new ArrayList<>();
         adj.put(class_name, list);
      }
      for (var entry: classes.entrySet()) {
         String class_name = entry.getKey();
         String parent = entry.getValue().getP();
         if (parent != null) {
            if (!adj.containsKey(parent)) {
               return false;
            }
            adj.get(parent).add(class_name);
         }
      }
      return true;
   }

   public boolean dfs(HashMap<String, ArrayList<String>> adj, HashMap<String, ClassInfo> classes) {
      HashSet<String> found_classes = new HashSet<>();
      int count = adj.size();
      for (var entry: adj.keySet()) {
         if (classes.get(entry).getP() != null) continue;
         dfs_helper(entry, adj, classes, found_classes);
      }
      //System.out.println(count + " " + found_classes.size());

      return (count != found_classes.size());
   }

   public void dfs_helper(String v, HashMap<String, ArrayList<String>> adj, HashMap<String, ClassInfo> classes, HashSet<String> f) {
      if (v == null) return;
      //System.out.println(v);
      if (f.contains(v)) return;
      f.add(v);
      t++;
      classes.get(v).start_time = t;
      for (var x: adj.get(v)) {
         dfs_helper(x, adj, classes, f);
      }
      t++;
      classes.get(v).end_time = t;
   }

   public void printAdj(HashMap<String, ArrayList<String>> adj) {
      for (var entry: adj.entrySet()) {
         System.out.print(entry.getKey() + ": ");
         for (var mem: entry.getValue()) {
            System.out.print(mem + " ");
         }
         System.out.println("");
      }
   }

   public boolean check_ze_ovrld(HashMap<String, ClassInfo> classes) {
      for (var cls: classes.entrySet()) {
         String par = cls.getValue().getP();
         for (var mts: cls.getValue().methods.entrySet()) {
            String name = mts.getKey();
            ArrayList<String> sig = mts.getValue().signature;
            String ret = mts.getValue().getRet();
            if(check_ovrld(par, name, ret, sig, classes)) return true;
         }
      }
      return false;
   }
   public boolean check_ovrld(String cls, String method, String exp_ret, ArrayList<String> sig, HashMap<String, ClassInfo> classes) {
      if (cls == null) return false;
      if (classes.get(cls).methods.containsKey(method) && !classes.get(cls).methods.get(method).getRet().equals(exp_ret)) return true;
      if (classes.get(cls).methods.containsKey(method) && !classes.get(cls).methods.get(method).signature.equals(sig)) return true;
      return check_ovrld(classes.get(cls).getP(), exp_ret, method, sig, classes);
   }
}

//for printing heirarchy class-field-method-variable-parameter blah blah
// for (var cls: sp.classes.entrySet()) {
         //    System.out.println(cls.getKey());
         //    if (cls.getValue().getP() != null) {
         //       System.out.print("Parent: ");
         //       System.out.println(cls.getValue().getP());
         //    }
            
         //    h.printClasses(cls.getValue());
         // }

//for printing the inheritance graph
//   gr.printAdj(sp.inheritance_graph);
