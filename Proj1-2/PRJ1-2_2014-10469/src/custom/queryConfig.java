package custom;

public class queryConfig {
	  public Table table;
	  public String table_name;
	  public int primary_def_count;
	  
	  public queryConfig()
	  {
		  table = new Table();
		  table_name = "";
		  primary_def_count = 0;
	  }
}
