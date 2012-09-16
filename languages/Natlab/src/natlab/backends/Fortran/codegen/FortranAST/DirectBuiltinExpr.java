package natlab.backends.Fortran.codegen.FortranAST;


public class DirectBuiltinExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 27

    public DirectBuiltinExpr() {
        super();

    }

    // Declared in FortranIR.ast line 27
    public DirectBuiltinExpr(String p0, String p1) {
        setBuiltinFunc(p0);
        setArgsList(p1);
    }

    public Object clone() throws CloneNotSupportedException {
        DirectBuiltinExpr node = (DirectBuiltinExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          DirectBuiltinExpr node = (DirectBuiltinExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        DirectBuiltinExpr res = (DirectBuiltinExpr)copy();
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
    // Declared in FortranIR.ast line 27
    private String tokenString_BuiltinFunc;
    public void setBuiltinFunc(String value) {
        tokenString_BuiltinFunc = value;
    }
    public String getBuiltinFunc() {
        return tokenString_BuiltinFunc;
    }


    // Declared in FortranIR.ast line 27
    private String tokenString_ArgsList;
    public void setArgsList(String value) {
        tokenString_ArgsList = value;
    }
    public String getArgsList() {
        return tokenString_ArgsList;
    }


}
