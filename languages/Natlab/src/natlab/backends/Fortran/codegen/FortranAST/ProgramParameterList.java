package natlab.backends.Fortran.codegen.FortranAST;


public class ProgramParameterList extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 5

    public ProgramParameterList() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 5
    public ProgramParameterList(List p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        ProgramParameterList node = (ProgramParameterList)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ProgramParameterList node = (ProgramParameterList)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ProgramParameterList res = (ProgramParameterList)copy();
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
    // Declared in FortranIR.ast line 5
    public void setParameterList(List list) {
        setChild(list, 0);
    }

    public int getNumParameter() {
        return getParameterList().getNumChild();
    }

    public Parameter getParameter(int i) {
        return (Parameter)getParameterList().getChild(i);
    }

    public void addParameter(Parameter node) {
        List list = getParameterList();
        list.setChild(node, list.getNumChild());
    }

    public void setParameter(Parameter node, int i) {
        List list = getParameterList();
        list.setChild(node, i);
    }
    public List getParameterList() {
        return (List)getChild(0);
    }

    public List getParameterListNoTransform() {
        return (List)getChildNoTransform(0);
    }


    // Declared in PrettyPrinter.jadd at line 19

    public void pp() {
        int size = getNumParameter();
        for(int i=0;i<size;i++) {
        	getParameter(i).pp();
        	if(i<size-1) {
        		System.out.print(",");
        	}
        }
    }

}
