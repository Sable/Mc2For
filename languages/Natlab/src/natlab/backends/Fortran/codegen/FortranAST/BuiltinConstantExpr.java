package natlab.backends.Fortran.codegen.FortranAST;


public class BuiltinConstantExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 29

    public BuiltinConstantExpr() {
        super();

    }

    // Declared in FortranIR.ast line 29
    public BuiltinConstantExpr(String p0) {
        setBuiltinFunc(p0);
    }

    public Object clone() throws CloneNotSupportedException {
        BuiltinConstantExpr node = (BuiltinConstantExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          BuiltinConstantExpr node = (BuiltinConstantExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BuiltinConstantExpr res = (BuiltinConstantExpr)copy();
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
    // Declared in FortranIR.ast line 29
    private String tokenString_BuiltinFunc;
    public void setBuiltinFunc(String value) {
        tokenString_BuiltinFunc = value;
    }
    public String getBuiltinFunc() {
        return tokenString_BuiltinFunc;
    }


}
