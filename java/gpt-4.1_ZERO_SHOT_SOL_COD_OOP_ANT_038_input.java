public class SQLDropTableStatement extends SQLStatementImpl implements SQLDropStatement {
    private List<SQLCommentHint> hints;

    protected List<SQLExprTableSource> tableSources = new ArrayList<SQLExprTableSource>();

    private boolean                    purge;

    protected boolean                  cascade      = false;
    protected boolean                  restrict     = false;
    protected boolean                  ifExists     = false;
    private boolean                    temporary    = false;

    public SQLDropTableStatement(){

    }
    
    public SQLDropTableStatement(String dbType){
        super (dbType);
    }

    public SQLDropTableStatement(SQLName name, String dbType){
        this(new SQLExprTableSource(name), dbType);
    }
    
    public SQLDropTableStatement(SQLName name){
        this (name, null);
    }
    
    public SQLDropTableStatement(SQLExprTableSource tableSource){
        this (tableSource, null);
    }

    public SQLDropTableStatement(SQLExprTableSource tableSource, String dbType){
        this (dbType);
        this.tableSources.add(tableSource);
    }

    public List<SQLExprTableSource> getTableSources() {
        return tableSources;
    }
    
    public void addPartition(SQLExprTableSource tableSource) {
        if (tableSource != null) {
            tableSource.setParent(this);
        }
        this.tableSources.add(tableSource);
    }

    public void setName(SQLName name) {
        this.addTableSource(new SQLExprTableSource(name));
    }

    public void addTableSource(SQLName name) {
        this.addTableSource(new SQLExprTableSource(name));
    }

    public void addTableSource(SQLExprTableSource tableSource) {
        tableSources.add(tableSource);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            this.acceptChild(visitor, tableSources);
        }
        visitor.endVisit(this);
    }

    @Override
    public List getChildren() {
        return this.tableSources;
    }

    public boolean isPurge() {
        return purge;
    }

    public void setPurge(boolean purge) {
        this.purge = purge;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public boolean isCascade() {
        return cascade;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    public boolean isRestrict() {
        return restrict;
    }

    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public List<SQLCommentHint> getHints() {
        return hints;
    }

    public void setHints(List<SQLCommentHint> hints) {
        this.hints = hints;
    }
}
