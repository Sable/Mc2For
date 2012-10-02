package natlab.backends.Fortran.codegen.FortranAST;


public class ArraySetStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 46

    public ArraySetStmt() {
        super();

    }

    // Declared in FortranIR.ast line 46
    public ArraySetStmt(String p0, String p1, String p2) {
        setlhsVariable(p0);
        setlhsIndex(p1);
        setrhsVariable(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        ArraySetStmt node = (ArraySetStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ArraySetStmt node = (ArraySetStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ArraySetStmt res = (ArraySetStmt)copy();
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
    // Declared in FortranIR.ast line 46
    private String tokenString_lhsVariable;
    public void setlhsVariable(String value) {
        tokenString_lhsVariable = value;
    }
    public String getlhsVariable() {
        return tokenString_lhsVariable;
    }


    // Declared in FortranIR.ast line 46
    private String tokenString_lhsIndex;
    public void setlhsIndex(String value) {
        tokenString_lhsIndex = value;
    }
    public String getlhsIndex() {
        return tokenString_lhsIndex;
    }


    // Declared in FortranIR.ast line 46
    private String tokenString_rhsVariable;
    public void setrhsVariable(String value) {
        tokenString_rhsVariable = value;
    }
    public String getrhsVariable() {
        return tokenString_rhsVariable;
    }


    // Declared in PrettyPrinter.jadd at line 207

    public void pp() {
    	System.out.print(getlhsVariable()+"("+getlhsIndex()+") = "+getrhsVariable());
    }

}
