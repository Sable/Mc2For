package natlab.backends.Fortran.codegen.FortranAST;


public class DirectBuiltinExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 28

    public DirectBuiltinExpr() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 28
    public DirectBuiltinExpr(List p0, String p1, String p2) {
        setChild(p0, 0);
        setBuiltinFunc(p1);
        setArgsList(p2);
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
    return 1;
  }
    // Declared in FortranIR.ast line 28
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


    // Declared in FortranIR.ast line 28
    private String tokenString_BuiltinFunc;
    public void setBuiltinFunc(String value) {
        tokenString_BuiltinFunc = value;
    }
    public String getBuiltinFunc() {
        return tokenString_BuiltinFunc;
    }


    // Declared in FortranIR.ast line 28
    private String tokenString_ArgsList;
    public void setArgsList(String value) {
        tokenString_ArgsList = value;
    }
    public String getArgsList() {
        return tokenString_ArgsList;
    }


}
