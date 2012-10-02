package natlab.backends.Fortran.codegen.FortranAST;


public class BuiltinConstantExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 30

    public BuiltinConstantExpr() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 30
    public BuiltinConstantExpr(List p0, String p1) {
        setChild(p0, 0);
        setBuiltinFunc(p1);
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
    return 1;
  }
    // Declared in FortranIR.ast line 30
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


    // Declared in FortranIR.ast line 30
    private String tokenString_BuiltinFunc;
    public void setBuiltinFunc(String value) {
        tokenString_BuiltinFunc = value;
    }
    public String getBuiltinFunc() {
        return tokenString_BuiltinFunc;
    }


    // Declared in PrettyPrinter.jadd at line 139

    public void pp() {
    	int size = getNumVariable();
    	for(int i=0;i<size;i++) {
    		getVariable(i).pp();
    		if(i<size-1) {
        		System.out.print(",");
        	}
    	}
    	System.out.print(" = "+getBuiltinFunc()+";");
    }

}
