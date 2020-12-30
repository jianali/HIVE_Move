import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConvertConf
{
    private List<String> databases;
    private Map<String, String> tables;

    public ConvertConf()
    {
        this.databases = new LinkedList();
        this.tables = new LinkedHashMap();
    }

    public List<String> getDatabases() {
        return this.databases;
    }

    public Map<String, String> getTables() {
        return this.tables;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.databases.toString() + "\n" + this.tables.toString());
        return sb.toString();
    }
}