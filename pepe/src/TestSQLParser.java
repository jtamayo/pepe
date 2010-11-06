import edu.stanford.pepe.postprocessing.SqlParse;



public class TestSQLParser {
    public static void main(String[] args) {
        new SqlParse("select * from accountejb, tableejb inner join mytableejb on a = b where tableejb.aField = (select first from otherejb)");
        
    }
}
