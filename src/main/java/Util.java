import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class Util
{
    public static boolean checkFileReadable(String path)
    {
        File file = new File(path);

        return (file.exists()) && (file.isFile()) && (file.canRead());
    }

    public static Connection getConn(String url, String user, String passwd)
            throws ClassNotFoundException, SQLException
    {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, user, passwd);
        return connection;
    }

    public static ConvertConf getMapFromJson(String path) throws IOException {
        File file = new File(path);
        StringBuffer sb = new StringBuffer();
        ConvertConf conf = new ConvertConf();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while (true)
        {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (!line.isEmpty()) {
                sb.append(line);
            }
        }
        br.close();
        JSONObject jsonObject = new JSONObject(sb.toString());
        if (!jsonObject.has("databases")) {
            throw new IOException("bad json confile, missing databases !!");
        }
        Object databases = jsonObject.get("databases");
        if ((databases instanceof JSONArray)) {
            Iterator it = ((JSONArray)databases).iterator();
            while (it.hasNext())
                conf.getDatabases().add(it.next().toString());
        }
        else {
            throw new IOException("bad json confile, databases should be array!!");
        }
        if (!jsonObject.has("tables")) {
            throw new IOException("bad json confile, missing tables !!");
        }
        Object tables = jsonObject.get("tables");

        if (!jsonObject.has("tablelist")) {
            throw new IOException("bad json confile, missing tablelist !!");
        }
        Object tablist = jsonObject.get("tablelist");
        List tablelist = new ArrayList();
        if ((databases instanceof JSONArray)) {
            Iterator it = ((JSONArray)tablist).iterator();
            while (it.hasNext())
                tablelist.add(it.next().toString());
        }
        else {
            throw new IOException("bad json confile, tablelist should be array!!");
        }

        if ((tables instanceof JSONObject)) {
            Iterator it = tablelist.iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                conf.getTables().put(key, ((JSONObject)tables).get(key).toString());
            }
        } else {
            throw new IOException("bad json confile, tables should be object!!");
        }
        return conf;
    }

    public static String getStringByType() {
        return null;
    }
}