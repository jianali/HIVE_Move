import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConvertByJDBC
{
    public static Set<String> neededTrncateTable = new HashSet();
    public static Set<String> replaceInsertTable = new HashSet();
    public static Set<String> ignoreInsertTable = new HashSet();

    private static void writeInsertToFile(BufferedWriter bw, Connection connection, String sql, String tb) throws SQLException, IOException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql, 1003, 1007);
        preparedStatement.setFetchSize(10000);
        preparedStatement.setFetchDirection(1000);
        ResultSet rs = preparedStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columns = metaData.getColumnCount();
        boolean isFirst = true;
        while (rs.next()) {
            if (!isFirst) {
                bw.write(",");
            } else {
                if (neededTrncateTable.contains(tb)) {
                    bw.write(String.format("truncate table %s;\n", new Object[] { tb }));
                }
                if (replaceInsertTable.contains(tb))
                    bw.write(String.format("replace into %s values ", new Object[] { tb }));
                else if (ignoreInsertTable.contains(tb))
                    bw.write(String.format("insert ignore into %s values ", new Object[] { tb }));
                else {
                    bw.write(String.format("insert into %s values ", new Object[] { tb }));
                }
                isFirst = false;
            }
            bw.write("(");
            for (int i = 0; i < columns; i++) {
                String icol = rs.getString(i + 1);
                int type = metaData.getColumnType(i + 1);
                boolean needQuatation = true;
                switch (type) {
                    case -7:
                    case -6:
                    case -5:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 8:
                        needQuatation = false;
                        break;
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 0:
                    case 1:
                    case 7:
                    default:
                        needQuatation = true;
                }
                if (rs.wasNull())
                    icol = "NULL";
                else if (needQuatation) {
                    icol = "'" + icol + "'";
                }
                if (i == 0)
                    bw.write(icol);
                else {
                    bw.write("," + icol);
                }
            }
            bw.write(")");
        }
        preparedStatement.close();
        if (!isFirst) {
            bw.write(";");
        }
        bw.newLine();
        bw.flush();
    }

    public static void doProcess(ConvertConf conf, Connection connection, JdbcOpt opt) throws IOException, SQLException
    {
        String outDir = opt.getOutDir();
        File d = new File(outDir);
        d.mkdirs();
        for (String tmp : conf.getDatabases()) {
            String[] sts = tmp.split(":");
            if (sts.length != 2) {
                throw new IOException("bad conf file");
            }
            String db = sts[0];
            String db1 = sts[1];
            if ((db.isEmpty()) && (db1.isEmpty())) {
                throw new IOException("bad conf file");
            }
            Path dbPath = Paths.get(outDir, new String[] { db + "2" + db1 });
            connection.setCatalog(db);
            BufferedWriter bw = new BufferedWriter(new FileWriter(dbPath.toFile(), false));
            bw.write("use " + db1 + ";");
            bw.newLine();
            for (String tb : conf.getTables().keySet()) {
                System.out.println("Processing: " + db + "." + tb);
                String sql = (String)conf.getTables().get(tb);
                if (sql.isEmpty()) {
                    continue;
                }
                sql = String.format(sql, new Object[] { db });
                writeInsertToFile(bw, connection, sql, tb);
                bw.newLine();
            }
            bw.close();
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        JdbcOpt jdbcOpt = new JdbcOpt();
        JCommander jCommander = JCommander.newBuilder().addObject(jdbcOpt).build();
        jCommander.parse(args);
        if (jdbcOpt.isHelp()) {
            jCommander.usage();
            return;
        }

        boolean confExists = Util.checkFileReadable(jdbcOpt.getJsonFile());
        if (!confExists) {
            System.err.println("the conf file is not readable or not exist: " + jdbcOpt.getJsonFile());
            System.exit(-1);
        }
        ConvertConf conf = Util.getMapFromJson(jdbcOpt.getJsonFile());
        Connection connection = Util.getConn(jdbcOpt.getJurl(), jdbcOpt.getUser(), jdbcOpt.getPasswd());

        neededTrncateTable.add("NEXT_COMPACTION_QUEUE_ID");
        neededTrncateTable.add("NEXT_LOCK_ID");
        neededTrncateTable.add("NEXT_TXN_ID");

        replaceInsertTable.add("DBS");
        replaceInsertTable.add("GLOBAL_PRIVS");
        replaceInsertTable.add("SEQUENCE_TABLE");

        ignoreInsertTable.add("NUCLEUS_TABLES");
        ignoreInsertTable.add("ROLES");

        doProcess(conf, connection, jdbcOpt);
        connection.close();
        System.out.println("Generate SQL finished");
    }
}