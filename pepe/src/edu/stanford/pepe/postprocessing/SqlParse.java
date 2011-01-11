package edu.stanford.pepe.postprocessing;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntoTableVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Keeps track of all the tables that are updated and selected in
 * an SQL statement.
 * 
 * @author jtamayo
 */
class SqlParse {
    
    public static class NotYetImplementedException extends RuntimeException  {
        private static final long serialVersionUID = 2712514900638900287L;

        public NotYetImplementedException() {
            super();
        }

        public NotYetImplementedException(String message, Throwable cause) {
            super(message, cause);
        }

        public NotYetImplementedException(String message) {
            super(message);
        }

        public NotYetImplementedException(Throwable cause) {
            super(cause);
        }
        
    }
    
    private Set<String> updatedTables = new HashSet<String>();
    
    private Set<String> selectedTables = new HashSet<String>();
    
    private final class IntoTableVisitorImplementation implements IntoTableVisitor {
        public void visit(Table arg0) {
            updatedTables.add(arg0.getName());
        }
    }
    
    private final class ExpressionVisitorImplementation implements ExpressionVisitor {
        public void visit(AnyComparisonExpression arg0) {
            throw new NotYetImplementedException();
        }
        
        public void visit(AllComparisonExpression arg0) {
            throw new NotYetImplementedException();
        }
        
        public void visit(ExistsExpression arg0) {
            throw new NotYetImplementedException();
        }
        
        public void visit(WhenClause arg0) {
            throw new NotYetImplementedException();
        }
        
        public void visit(CaseExpression arg0) {
            throw new NotYetImplementedException();            
        }
        
        public void visit(SubSelect arg0) {
            arg0.getSelectBody().accept(new SelectVisitorImplementation());
        }
        
        public void visit(Column arg0) {
            // Not really interested in what columns are compared.
        }
        
        public void visit(NotEqualsTo arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(MinorThanEquals arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(MinorThan arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(LikeExpression arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(IsNullExpression arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(InExpression arg0) {
            arg0.getItemsList().accept(new ItemsListVisitorImplementation());
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(GreaterThanEquals arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(GreaterThan arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(EqualsTo arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Between arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getBetweenExpressionEnd().accept(new ExpressionVisitorImplementation());
            arg0.getBetweenExpressionStart().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(OrExpression arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(AndExpression arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Subtraction arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Multiplication arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Division arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Addition arg0) {
            arg0.getLeftExpression().accept(new ExpressionVisitorImplementation());
            arg0.getRightExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(StringValue arg0) {
            // Ignore string values
        }
        
        public void visit(Parenthesis arg0) {
            arg0.getExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(TimestampValue arg0) {
            // Ignore literals
        }
        
        public void visit(TimeValue arg0) {
            // Ignore literals
        }
        
        public void visit(DateValue arg0) {
            // Ignore literals
        }
        
        public void visit(LongValue arg0) {
            // Ignore literals
        }
        
        public void visit(DoubleValue arg0) {
            // Ignore literals
        }
        
        public void visit(JdbcParameter arg0) {
            // Ignore jdbc parameters
        }
        
        public void visit(InverseExpression arg0) {
            arg0.getExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(Function arg0) {
            arg0.getParameters().accept(new ItemsListVisitorImplementation());
        }
        
        public void visit(NullValue arg0) {
            // Ignore null literals
        }
    }
    

    private final class ItemsListVisitorImplementation implements ItemsListVisitor {
        @SuppressWarnings("unchecked")
        public void visit(ExpressionList arg0) {
            List<Expression> expressions = arg0.getExpressions();
            for (Expression e : expressions) {
                e.accept(new ExpressionVisitorImplementation());
            }
        }

        public void visit(SubSelect arg0) {
            arg0.getSelectBody().accept(new SelectVisitorImplementation());
        }
    }

    private final class FromItemVisitorImplementation implements FromItemVisitor {
        public void visit(SubJoin arg0) {
            arg0.getLeft().accept(new FromItemVisitorImplementation());
            arg0.getJoin().getRightItem().accept(new FromItemVisitorImplementation());
            arg0.getJoin().getOnExpression().accept(new ExpressionVisitorImplementation());
        }
        
        public void visit(SubSelect arg0) {
            arg0.getSelectBody().accept(new SelectVisitorImplementation());
        }
        
        public void visit(Table arg0) {
            selectedTables.add(arg0.getName());
        }
    }
    
    private final class SelectVisitorImplementation implements SelectVisitor {


        @SuppressWarnings("unchecked")
        public void visit(Union arg0) {
            List<PlainSelect> plainSelects = arg0.getPlainSelects();
            for (PlainSelect select : plainSelects) {
                select.accept(new SelectVisitorImplementation());
            }
        }

        public void visit(PlainSelect arg0) {
            arg0.getFromItem().accept(new FromItemVisitorImplementation());
            List<Join> joins = arg0.getJoins();
            if (joins != null) {
                for (Join join : joins) {
                    final Expression onExpression = join.getOnExpression();
                    if (onExpression != null){
                        onExpression.accept(new ExpressionVisitorImplementation());
                    }
                    join.getRightItem().accept(new FromItemVisitorImplementation());
                }
            }
            final Expression where = arg0.getWhere();
            if (where != null) {
                where.accept(new ExpressionVisitorImplementation());
            }
        }
    }
    
    private final class StatementVisitorImplementation implements StatementVisitor {

        public void visit(CreateTable arg0) {
            throw new RuntimeException("Not implementing create_table yet");
        }

        public void visit(Truncate arg0) {
            throw new RuntimeException("Not implementing truncate yet");
        }

        public void visit(Drop arg0) {
            throw new RuntimeException("Not implementing drop yet");
        }

        public void visit(Replace arg0) {
            throw new RuntimeException("Not implementing replace yet");
        }

        public void visit(Insert arg0) {
            updatedTables.add(arg0.getTable().getName());
            arg0.getItemsList().accept(new ItemsListVisitorImplementation());
        }

        public void visit(Update arg0) {
            updatedTables.add(arg0.getTable().getName());
            arg0.getWhere().accept(new ExpressionVisitorImplementation());
        }

        public void visit(Delete arg0) {
            updatedTables.add(arg0.getTable().getName());
            final Expression where = arg0.getWhere();
            if (where != null) {
                where.accept(new ExpressionVisitorImplementation());
            }
        }

        public void visit(Select arg0) {
            arg0.getSelectBody().accept(new SelectVisitorImplementation());
        }
    }

    public SqlParse(String sql) {
        // HACK: JSqlParser does not understand for update
        sql = sql.replaceAll("for update", "");
        
        Reader reader = new StringReader(sql);
        Statement parse;
        try {
            parse = new CCJSqlParserManager().parse(reader);
        } catch (JSQLParserException e) {
            throw new RuntimeException("Error while parsing SQL statement " + sql, e);
        }
        parse.accept(new StatementVisitorImplementation());
        
        // Just for convenience, "fix" the result
        updatedTables = Collections.unmodifiableSet(updatedTables);
        selectedTables = Collections.unmodifiableSet(selectedTables);
    }

    /**
     * Returns the modified tables, either through an insert or an update.
     */
    public Set<String> getUpdatedTables() {
        return updatedTables;
    }
    
    public Set<String> getSelectedTables() {
        return selectedTables;
    }
}
