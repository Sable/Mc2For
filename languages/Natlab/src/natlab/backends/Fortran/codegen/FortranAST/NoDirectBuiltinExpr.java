package natlab.backends.Fortran.codegen.FortranAST;


public class NoDirectBuiltinExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 29

    public NoDirectBuiltinExpr() {
        super();

    }

    // Declared in FortranIR.ast line 29
    public NoDirectBuiltinExpr(String p0) {
        setCodeInline(p0);
    }

    public Object clone() throws CloneNotSupportedException {
        NoDirectBuiltinExpr node = (NoDirectBuiltinExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          NoDirectBuiltinExpr node = (NoDirectBuiltinExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        NoDirectBuiltinExpr res = (NoDirectBuiltinExpr)copy();
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
    private String tokenString_CodeInline;
    public void setCodeInline(String value) {
        tokenString_CodeInline = value;
    }
    public String getCodeInline() {
        return tokenString_CodeInline;
    }


    // Declared in PrettyPrinter.jadd at line 136

    public void pp() {
    	System.out.print(getCodeInline());
    }

}
