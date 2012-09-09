package natlab.backends.Fortran.codegen.FortranAST;
public class LHSVariable extends Variable implements Cloneable {
    // Declared in FortranIR.ast line 19

    public LHSVariable() {
        super();

    }

    // Declared in FortranIR.ast line 19
    public LHSVariable(String p0) {
        setName(p0);
    }

    public Object clone() throws CloneNotSupportedException {
        LHSVariable node = (LHSVariable)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          LHSVariable node = (LHSVariable)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        LHSVariable res = (LHSVariable)copy();
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
    // Declared in FortranIR.ast line 11
    private String tokenString_Name;
    public void setName(String value) {
        tokenString_Name = value;
    }
    public String getName() {
        return tokenString_Name;
    }


}
