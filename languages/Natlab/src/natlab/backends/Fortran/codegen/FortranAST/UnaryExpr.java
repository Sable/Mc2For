package natlab.backends.Fortran.codegen.FortranAST;


public class UnaryExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 26

    public UnaryExpr() {
        super();

    }

    // Declared in FortranIR.ast line 26
    public UnaryExpr(String p0, String p1) {
        setOperation(p0);
        setOperand(p1);
    }

    public Object clone() throws CloneNotSupportedException {
        UnaryExpr node = (UnaryExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          UnaryExpr node = (UnaryExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        UnaryExpr res = (UnaryExpr)copy();
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
    // Declared in FortranIR.ast line 26
    private String tokenString_Operation;
    public void setOperation(String value) {
        tokenString_Operation = value;
    }
    public String getOperation() {
        return tokenString_Operation;
    }


    // Declared in FortranIR.ast line 26
    private String tokenString_Operand;
    public void setOperand(String value) {
        tokenString_Operand = value;
    }
    public String getOperand() {
        return tokenString_Operand;
    }


}
