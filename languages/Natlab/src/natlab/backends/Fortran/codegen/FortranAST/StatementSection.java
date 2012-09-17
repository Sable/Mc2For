package natlab.backends.Fortran.codegen.FortranAST;


public class StatementSection extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 16

    public StatementSection() {
        super();

        setChild(new List(), 0);
    }

    // Declared in FortranIR.ast line 16
    public StatementSection(List p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        StatementSection node = (StatementSection)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          StatementSection node = (StatementSection)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        StatementSection res = (StatementSection)copy();
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
    // Declared in FortranIR.ast line 16
    public void setStatementList(List list) {
        setChild(list, 0);
    }

    public int getNumStatement() {
        return getStatementList().getNumChild();
    }

    public Statement getStatement(int i) {
        return (Statement)getStatementList().getChild(i);
    }

    public void addStatement(Statement node) {
        List list = getStatementList();
        list.setChild(node, list.getNumChild());
    }

    public void setStatement(Statement node, int i) {
        List list = getStatementList();
        list.setChild(node, i);
    }
    public List getStatementList() {
        return (List)getChild(0);
    }

    public List getStatementListNoTransform() {
        return (List)getChildNoTransform(0);
    }


    // Declared in PrettyPrinter.jadd at line 77

    public void pp() {
    	int size = getNumStatement();
    	for(int i=0;i<size;i++) {
    		getStatement(i).pp();
    		System.out.println("");
    	}
    }

}
