import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Value object for drop table options
final class DropTableOptions {
    private final boolean purge;
    private final boolean cascade;
    private final boolean restrict;
    private final boolean ifExists;
    private final boolean temporary;

    public DropTableOptions(boolean purge, boolean cascade, boolean restrict, boolean ifExists, boolean temporary) {
        this.purge = purge;
        this.cascade = cascade;
        this.restrict = restrict;
        this.ifExists = ifExists;
        this.temporary = temporary;
    }

    public boolean isPurge() { return purge; }
    public boolean isCascade() { return cascade; }
    public boolean isRestrict() { return restrict; }
    public boolean isIfExists() { return ifExists; }
    public boolean isTemporary() { return temporary; }

    public DropTableOptions withPurge(boolean purge) {
        return new DropTableOptions(purge, this.cascade, this.restrict, this.ifExists, this.temporary);
    }
    public DropTableOptions withCascade(boolean cascade) {
        return new DropTableOptions(this.purge, cascade, this.restrict, this.ifExists, this.temporary);
    }
    public DropTableOptions withRestrict(boolean restrict) {
        return new DropTableOptions(this.purge, this.cascade, restrict, this.ifExists, this.temporary);
    }
    public DropTableOptions withIfExists(boolean ifExists) {
        return new DropTableOptions(this.purge, this.cascade, this.restrict, ifExists, this.temporary);
    }
    public DropTableOptions withTemporary(boolean temporary) {
        return new DropTableOptions(this.purge, this.cascade, this.restrict, this.ifExists, temporary);
    }
}

// Manages table sources for the DROP TABLE statement
final class TableSourceManager {
    private final List<SQLExprTableSource> tableSources = new ArrayList<>();

    public void addTableSource(SQLExprTableSource tableSource) {
        if (tableSource != null) {
            tableSources.add(tableSource);
        }
    }

    public void addTableSource(SQLName name) {
        if (name != null) {
            addTableSource(new SQLExprTableSource(name));
        }
    }

    public List<SQLExprTableSource> getTableSources() {
        return Collections.unmodifiableList(tableSources);
    }
}

// Main DROP TABLE statement class
public class SQLDropTableStatement extends SQLStatementImpl implements SQLDropStatement {
    private final TableSourceManager tableSourceManager = new TableSourceManager();
    private DropTableOptions options = new DropTableOptions(false, false, false, false, false);
    private List<SQLCommentHint> hints = new ArrayList<>();

    public SQLDropTableStatement() {
        super();
    }

    public SQLDropTableStatement(String dbType) {
        super(dbType);
    }

    public SQLDropTableStatement(SQLName name, String dbType) {
        this(dbType);
        addTableSource(name);
    }

    public SQLDropTableStatement(SQLName name) {
        this(name, null);
    }

    public SQLDropTableStatement(SQLExprTableSource tableSource) {
        this();
        addTableSource(tableSource);
    }

    public void addTableSource(SQLExprTableSource tableSource) {
        tableSourceManager.addTableSource(tableSource);
    }

    public void addTableSource(SQLName name) {
        tableSourceManager.addTableSource(name);
    }

    public List<SQLExprTableSource> getTableSources() {
        return tableSourceManager.getTableSources();
    }

    public DropTableOptions getOptions() {
        return options;
    }

    public void setOptions(DropTableOptions options) {
        this.options = Objects.requireNonNull(options, "DropTableOptions cannot be null");
    }

    public void setPurge(boolean purge) {
        this.options = this.options.withPurge(purge);
    }

    public boolean isPurge() {
        return options.isPurge();
    }

    public void setCascade(boolean cascade) {
        this.options = this.options.withCascade(cascade);
    }

    public boolean isCascade() {
        return options.isCascade();
    }

    public void setRestrict(boolean restrict) {
        this.options = this.options.withRestrict(restrict);
    }

    public boolean isRestrict() {
        return options.isRestrict();
    }

    public void setIfExists(boolean ifExists) {
        this.options = this.options.withIfExists(ifExists);
    }

    public boolean isIfExists() {
        return options.isIfExists();
    }

    public void setTemporary(boolean temporary) {
        this.options = this.options.withTemporary(temporary);
    }

    public boolean isTemporary() {
        return options.isTemporary();
    }

    public List<SQLCommentHint> getHints() {
        return Collections.unmodifiableList(hints);
    }

    public void setHints(List<SQLCommentHint> hints) {
        this.hints = (hints == null) ? new ArrayList<>() : new ArrayList<>(hints);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            this.acceptChild(visitor, getTableSources());
        }
        visitor.endVisit(this);
    }

    @Override
    public List<SQLExprTableSource> getChildren() {
        return getTableSources();
    }
}