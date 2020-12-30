import java.util.LinkedList;
import java.util.List;

public class ParseInfo
{
    String tableName;
    List<String> list;

    public ParseInfo(String tableName)
    {
        this.tableName = tableName;
        this.list = new LinkedList();
    }

    public String getTableName() {
        return this.tableName;
    }
}