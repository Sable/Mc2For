package natlab.backends.Fortran.codegen.FortranAST;


public class AbstractAssignToListStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 23

    public AbstractAssignToListStmt() {
        super();

        setChild(new List(), 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 23
    public AbstractAssignToListStmt(String p0, List p1, Expression p2) {
        setRuntimeCheck(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        AbstractAssignToListStmt node = (AbstractAssignToListStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          AbstractAssignToListStmt node = (AbstractAssignToListStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AbstractAssignToListStmt res = (AbstractAssignToListStmt)copy();
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
    return 2;
  }
    // Declared in FortranIR.ast line 23
    private String tokenString_RuntimeCheck;
    public void setRuntimeCheck(String value) {
        tokenString_RuntimeCheck = value;
    }
    public String getRuntimeCheck() {
        return tokenString_RuntimeCheck;
    }


    // Declared in FortranIR.ast line 23
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


    // Declared in FortranIR.ast line 23
    public void setExpression(Expression node) {
        setChild(node, 1);
    }
    public Expression getExpression() {
        return (Expression)getChild(1);
    }

    public Expression getExpressionNoTransform() {
        return (Expression)getChildNoTransform(1);
    }


}
