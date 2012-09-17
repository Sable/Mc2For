package natlab.backends.Fortran.codegen.FortranAST;


public class UserDefinedFunction extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 32

    public UserDefinedFunction() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 32
    public UserDefinedFunction(List p0, String p1, String p2) {
        setChild(p0, 0);
        setFuncName(p1);
        setArgsList(p2);
    }

    public Object clone() throws CloneNotSupportedException {
        UserDefinedFunction node = (UserDefinedFunction)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          UserDefinedFunction node = (UserDefinedFunction)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        UserDefinedFunction res = (UserDefinedFunction)copy();
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
    // Declared in FortranIR.ast line 32
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


    // Declared in FortranIR.ast line 32
    private String tokenString_FuncName;
    public void setFuncName(String value) {
        tokenString_FuncName = value;
    }
    public String getFuncName() {
        return tokenString_FuncName;
    }


    // Declared in FortranIR.ast line 32
    private String tokenString_ArgsList;
    public void setArgsList(String value) {
        tokenString_ArgsList = value;
    }
    public String getArgsList() {
        return tokenString_ArgsList;
    }


    // Declared in PrettyPrinter.jadd at line 151

    public void pp() {
    	int size = getNumVariable();
    	for(int i=0;i<size;i++) {
    		getVariable(i).pp();
    		if(i<size-1) {
        		System.out.print(",");
        	}
    	}
    	System.out.print(" = "+getFuncName()+"("+getArgsList()+");");
    }

}
