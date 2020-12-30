import com.beust.jcommander.Parameter;

public class JdbcOpt
{

    @Parameter(names={"-jdbc"}, description="the connection URL of JDBC")
    private String jurl;

    @Parameter(names={"-user"}, description="jdbc user name")
    private String user;

    @Parameter(names={"-passwd"}, description="jdbc password")
    private String passwd;

    @Parameter(names={"-conf"}, description="the json configuration file")
    private String jsonFile;

    @Parameter(names={"-outDir"}, description="output sql dir")
    private String outDir;

    @Parameter(names={"-help"}, description="help")
    private boolean ishelp;

    public boolean isHelp()
    {
        return this.ishelp;
    }

    public String getJurl() {
        return this.jurl;
    }

    public String getUser() {
        return this.user;
    }

    public String getPasswd() {
        return this.passwd;
    }

    public String getJsonFile() {
        return this.jsonFile;
    }

    public String getOutDir() {
        return this.outDir;
    }
}