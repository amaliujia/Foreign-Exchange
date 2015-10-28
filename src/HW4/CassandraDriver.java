package HW4;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.io.*;


/**
 * @author amaliujia
 */
public class CassandraDriver {

    private Cluster cluster;
    private Session session;

    public void setup(){
        // Connect to the cluster and keyspace "demo"
        this.cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        this.session = this.cluster.connect("demo");
    }

    /**
     * Read CSV file and insert all records in table. The schema of table is formed by
     * the attributes names in the first line of CSV file.
     * @param CSVPath
     * @throws IOException
     */
    public void insertData(String CSVPath) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(new File(CSVPath)));
        String line = reader.readLine();
        String[] attributes = line.split(",");

        String sql = "INSERT INTO data (" + attributes[0];
        for (int i = 1; i < attributes.length; i++){
            sql += ", ";
            sql +=  attributes[i];
        }

        sql += " ) VALUES ";

        while ((line = reader.readLine()) != null){
            line = reader.readLine();
            String[] cols = line.split(",");
            session.execute(sql + this.produceSQL(cols));
        }
    }

    /**
     * Generate Cassandra statement format given strings.
     * @param cols
     * @return
     *      Cassandra statement.
     */
    private String produceSQL(String[] cols) {
        StringBuffer buffer = new StringBuffer();
        buffer.append('(');
        buffer.append(cols[0]);

        for (int i = 1; i < cols.length; i++){
            buffer.append(',');
            buffer.append(cols[i]);
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * Given cassandra table name and schema of table, print all the data line by line.
     * @param tablename
     * @param schema
     */
    public void queryData(String tablename, String[] schema){
        ResultSet resultSet = session.execute("SELECT * FROM " + tablename);
        for (Row row : resultSet) {
            for (String s : schema) {
                System.out.print(row.getString(s));
                System.out.print(" ");
            }

            System.out.println();
        }
    }

    public static void main(String[] args){
        CassandraDriver solution = new CassandraDriver();
        solution.setup();
        try {
            solution.insertData(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

