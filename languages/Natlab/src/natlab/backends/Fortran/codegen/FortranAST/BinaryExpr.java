package natlab.backends.Fortran.codegen.FortranAST;


public class BinaryExpr extends Expression implements Cloneable {
    // Declared in FortranIR.ast line 26

    public BinaryExpr() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 26
    public BinaryExpr(List p0, String p1, String p2, String p3) {
        setChild(p0, 0);
        setOperand1(p1);
        setOperator(p2);
        setOperand2(p3);
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
    return 1;
  }
    // Declared in FortranIR.ast line 26
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


    // Declared in FortranIR.ast line 26
    private String tokenString_Operand1;
    public void setOperand1(String value) {
        tokenString_Operand1 = value;
    }
    public String getOperand1() {
        return tokenString_Operand1;
    }


    // Declared in FortranIR.ast line 26
    private String tokenString_Operator;
    public void setOperator(String value) {
        tokenString_Operator = value;
    }
    public String getOperator() {
        return tokenString_Operator;
    }


    // Declared in FortranIR.ast line 26
    private String tokenString_Operand2;
    public void setOperand2(String value) {
        tokenString_Operand2 = value;
    }
    public String getOperand2() {
        return tokenString_Operand2;
    }


    // Declared in PrettyPrinter.jadd at line 106

    public void pp() {
    	int size = getNumVariable();
    	for(int i=0;i<size;i++) {
    		getVariable(i).pp();
    		if(i<size-1) {
        		System.out.print(",");
        	}
    	}
    	System.out.print(" = "+getOperand1()+" "+getOperator()+" "+getOperand2()+";");
    }

}
