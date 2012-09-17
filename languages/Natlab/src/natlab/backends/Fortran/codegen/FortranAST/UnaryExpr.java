package natlab.backends.Fortran.codegen.FortranAST;


public class UnaryExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 27

    public UnaryExpr() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 27
    public UnaryExpr(List p0, String p1, String p2) {
        setChild(p0, 0);
        setOperator(p1);
        setOperand(p2);
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
    return 1;
  }
    // Declared in FortranIR.ast line 27
    public void setVariableList(List list) {
        setChild(list, 0);
    }

    public int getNumVariable() {
        return getVariableList().getNumChild();
    }

    public Variable getVariable(int i) {
        return (Variable)getVariableList().getChild(i);
    }

    public void addVariable(Variable node) {
        List list = getVariableList();
        list.setChild(node, list.getNumChild());
    }

    public void setVariable(Variable node, int i) {
        List list = getVariableList();
        list.setChild(node, i);
    }
    public List getVariableList() {
        return (List)getChild(0);
    }

    public List getVariableListNoTransform() {
        return (List)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 27
    private String tokenString_Operator;
    public void setOperator(String value) {
        tokenString_Operator = value;
    }
    public String getOperator() {
        return tokenString_Operator;
    }


    // Declared in FortranIR.ast line 27
    private String tokenString_Operand;
    public void setOperand(String value) {
        tokenString_Operand = value;
    }
    public String getOperand() {
        return tokenString_Operand;
    }


}
