package natlab.backends.Fortran.codegen.FortranAST;


public class NotDirectBuiltinExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 28

    public NotDirectBuiltinExpr() {
        super();

    }

    // Declared in FortranIR.ast line 28
    public NotDirectBuiltinExpr(String p0) {
        setCodeInline(p0);
    }

    public Object clone() throws CloneNotSupportedException {
        NotDirectBuiltinExpr node = (NotDirectBuiltinExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          NotDirectBuiltinExpr node = (NotDirectBuiltinExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        NotDirectBuiltinExpr res = (NotDirectBuiltinExpr)copy();
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
    // Declared in FortranIR.ast line 28
    private String tokenString_CodeInline;
    public void setCodeInline(String value) {
        tokenString_CodeInline = value;
    }
    public String getCodeInline() {
        return tokenString_CodeInline;
    }


}
