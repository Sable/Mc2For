package natlab.backends.Fortran.codegen.FortranAST;
public class VariableList extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 9

    public VariableList() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 9
    public VariableList(List p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        VariableList node = (VariableList)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          VariableList node = (VariableList)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        VariableList res = (VariableList)copy();
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
    // Declared in FortranIR.ast line 9
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


}
