package custom;

import java.util.ArrayList;
/* this calss is for storing query information, because all error printed after syntax check */
public class queryConfig {
	  public Table table;
	  public String table_name;
	  public int primary_def_count;
	  
	  //for insert
	  public ArrayList<Types> record;
	  public ArrayList<String> record_match_attributes;
	  
	  //for select
	  public ArrayList<SelectName> select_name_list;
	  public ArrayList<SelectName> select_table_name_list;
	  public ExpressionType where_expression;
	  
	  public queryConfig()
	  {
		  table = new Table();
		  record = new ArrayList<Types>();
		  where_expression = new ExpressionType();
		  select_name_list = new ArrayList<SelectName>();
		  select_table_name_list = new ArrayList<SelectName>();

		  
		  table_name = "";
		  primary_def_count = 0;
	  }
}
