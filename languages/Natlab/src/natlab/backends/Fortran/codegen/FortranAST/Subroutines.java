package natlab.backends.Fortran.codegen.FortranAST;


public class Subroutines extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 34

    public Subroutines() {
        super();

    }

    // Declared in FortranIR.ast line 34
    public Subroutines(String p0, String p1, String p2) {
        setFuncName(p0);
        setInputArgsList(p1);
        setOutputArgsList(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        Subroutines node = (Subroutines)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          Subroutines node = (Subroutines)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        Subroutines res = (Subroutines)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
    // Declared in FortranIR.ast line 34
    private String tokenString_FuncName;
    public void setFuncName(String value) {
        tokenString_FuncName = value;
    }
    public String getFuncName() {
        return tokenString_FuncName;
    }


    // Declared in FortranIR.ast line 34
    private String tokenString_InputArgsList;
    public void setInputArgsList(String value) {
        tokenString_InputArgsList = value;
    }
    public String getInputArgsList() {
        return tokenString_InputArgsList;
    }


    // Declared in FortranIR.ast line 34
    private String tokenString_OutputArgsList;
    public void setOutputArgsList(String value) {
        tokenString_OutputArgsList = value;
    }
    public String getOutputArgsList() {
        return tokenString_OutputArgsList;
    }


    // Declared in PrettyPrinter.jadd at line 162

    public void pp() {
    	System.out.print("call "+getFuncName()+"("+getInputArgsList()+", "+getOutputArgsList()+");");
    }

}
