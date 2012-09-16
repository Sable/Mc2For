package natlab.backends.Fortran.codegen.FortranAST;


public class BinaryExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 25

    public BinaryExpr() {
        super();

    }

    // Declared in FortranIR.ast line 25
    public BinaryExpr(String p0, String p1, String p2) {
        setOperand1(p0);
        setOperation(p1);
        setOperand2(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        BinaryExpr node = (BinaryExpr)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          BinaryExpr node = (BinaryExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BinaryExpr res = (BinaryExpr)copy();
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
    // Declared in FortranIR.ast line 25
    private String tokenString_Operand1;
    public void setOperand1(String value) {
        tokenString_Operand1 = value;
    }
    public String getOperand1() {
        return tokenString_Operand1;
    }


    // Declared in FortranIR.ast line 25
    private String tokenString_Operation;
    public void setOperation(String value) {
        tokenString_Operation = value;
    }
    public String getOperation() {
        return tokenString_Operation;
    }


    // Declared in FortranIR.ast line 25
    private String tokenString_Operand2;
    public void setOperand2(String value) {
        tokenString_Operand2 = value;
    }
    public String getOperand2() {
        return tokenString_Operand2;
    }


}
