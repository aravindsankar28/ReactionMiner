package ctree.util;

import java.util.*;

/**
 * Parsing command line options.
 * Options are in the form of "-name[=value]".
 * E.g., -prefix=ucsb -k=10 file1.txt -max file2.txt
 * then getString("prefix")=="ucsb", getInt("k")==10, hasOpt("max")==true,
 * getArg(0)=="file1.txt", getArg(1)=="file2.txt"
 * args()==2, opts()==3
 *
 * Options are case sensitive.
 *
 * @author Huahai He
 * @version 1.0
 */
public class Opt {
  HashMap<String, String> opts = new HashMap<String, String> ();
  Vector<String> arguments = new Vector<String> (10);
  public Opt(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) { // options, e.g., -name=value
        int idx = args[i].indexOf('=');
        if (idx >= 0) {
          String name = args[i].substring(1, idx);
          String value = args[i].substring(idx + 1);
          opts.put(name, value);
        }
        else {
          opts.put(args[i].substring(1), null);
        }
      }
      else { // arguments, i.e., not proceded by -name
        arguments.addElement(args[i]);
      }
    }
  }

  /**
   * Return an argument.
   * @param index Start at 0
   * @return throw exception if index out of bound
   */
  public String getArg(int index) {
    if (index >= arguments.size()) {
      throw new RuntimeException("Short of arguments.");
    }
    return arguments.elementAt(index);
  }

  /**
   * Return the number of arguments.
   * @return int
   */
  public int args() {
    return arguments.size();
  }

  /**
   * Return the number of options
   * @return int
   */
  public int opts() {
    return opts.size();
  }

  /**
   * Return an option as a string.
   * @param name String
   * @return return null if the option name is not found.
   */
  public String getString(String name) {
    return opts.get(name);
  }

  /**
   * Return an option as a string, if not found, return the default value.
   * @param name String
   * @param def String
   * @return String
   */
  public String getString(String name, String def) {
    String v = opts.get(name);
    if (v == null) {
      return def;
    }
    else {
      return v;
    }
  }

  /**
   * Return an integer given the name
   * @param name String
   * @return int
   */
  public int getInt(String name) {
    return Integer.parseInt(opts.get(name));
  }

  /**
   * Return an integer given the name with a default value
   * @param name String
   * @param def int
   * @return int
   */
  public int getInt(String name, int def) {
    String v = opts.get(name);
    if (v == null) {
      return def;
    }
    else {
      return Integer.parseInt(v);
    }
  }

  /**
   * Return a double given the name
   * @param name String
   * @return double
   */
  public double getDouble(String name) {
    return Double.parseDouble(opts.get(name));
  }

  /**
   * Return a double given the name with a default value
   * @param name String
   * @param def double
   * @return double
   */
  public double getDouble(String name, double def) {
    String v = opts.get(name);
    if (v == null) {
      return def;
    }
    else {
      return Double.parseDouble(v);
    }
  }

  /**
   * Return 0 or 1
   * @param name String
   * @return int
   */
  public int get01(String name) {
      int i = Integer.parseInt(opts.get(name));
      if(i!=0||i!=1) {
          throw new RuntimeException("Option must be 0 or 1");
      }
      return i;
  }

  /**
   * Return 0 or 1 with default value
   * @param name String
   * @param def int
   * @return int
   */
  public int get01(String name, int def) {
      assert(def==0||def==1);
      String v = opts.get(name);
      if(v==null) {
          return def;
      } else {
          int i = Integer.parseInt(v);
          if (i != 0 && i != 1) {
              throw new RuntimeException("Option must be 0 or 1");
          }
          return i;
      }
  }

  /**
   * Whether an option is present.
   * @param name String
   * @return boolean
   */
  public boolean hasOpt(String name) {
    return opts.containsKey(name);
  }

  public static void main(String[] args) {
    String[] s = {"-prefix=ucsb", "-k=10", "file1.txt", "-max", "file2.txt"};
    Opt o = new Opt(s);
    System.out.println(o.getString("prefix"));
    System.out.println(o.getInt("k"));
    System.out.println(o.hasOpt("max"));
    System.out.println(o.getDouble("support", 0.2));
    System.out.printf("Number of arguments: %d\n", o.args());
    System.out.println(o.getArg(0));
    System.out.println(o.getArg(1));
  }
}
