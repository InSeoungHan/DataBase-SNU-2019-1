package custom;

public class errorMsg {
	  public static final int PRINT_SYNTAX_ERROR = -1;
	  public static final int CREATE_TABLE_SUCCESS = 0;
	  public static final int DUPLICATE_COLUMN_DEF_ERROR = 1;
	  public static final int DUPLICATE_PRIMARYKEY_DEF_ERROR = 2;
	  public static final int REFERENCE_TYPE_ERROR = 3;
	  public static final int REFERENCE_NON_PRIMARYKEY_ERROR = 4;
	  public static final int REFERENCE_COLUMN_EXISTENCE_ERROR = 5;
	  public static final int REFERENCE_TABLE_EXISTENCE_ERROR = 6;
	  public static final int NON_EXISTING_COLUMN_DEF_ERROR = 7;
	  public static final int TABLE_EXISTENCE_ERROR = 8;
	  public static final int DROP_SUCCESS = 9;
	  public static final int DROP_REFERENCED_TABLE_ERROR = 10;
	  public static final int SHOW_TABLES_NO_TABLE = 11;
	  public static final int NO_SUCH_TABLE = 12;
	  public static final int CHAR_LENGTH_ERROR = 13;
	  public static final int INSERT_RESULT = 14;
	  public static final int INSERT_DUPLICATE_PRIMARYKEY_ERROR = 15;
	  public static final int INSERT_REFERENTIAL_INTEGRITY_ERROR = 16;
	  public static final int INSERT_TYPE_MISMATCH_ERROR = 17;
	  public static final int INSERT_COLUMN_EXISTENCE_ERROR = 18;
	  public static final int INSERT_COLUMN_NON_NULLABLE_ERROR = 19;
	  public static final int DELETE_RESULT = 20;
	  public static final int DELETE_REFERENTIAL_INTEGRITY_PASSED = 21;
	  public static final int SELECT_TABLE_EXISTENCE_ERROR = 22;
	  public static final int SELECT_COLUMN_RESOLVE_ERROR = 23;
	  public static final int WHERE_INCOMPARABLE_ERROR = 24;
	  public static final int WHERE_TABLE_NOT_SPECIFIED = 25;
	  public static final int WHERE_COLUMN_NOT_EXIST = 26;
	  public static final int WHERE_AMBIGUOUS_REFERENCE = 27;
	  /* print Messages and Errors */
		public static void printMessage(int q, String n, boolean prompt)
		{
		    switch(q)
		    {
		      case PRINT_SYNTAX_ERROR:
		      	System.out.println("Syntex Error");
		      	break;
			  case INSERT_RESULT:
		      	System.out.println("The row is inserted");
		      	break;
		      case CREATE_TABLE_SUCCESS:
		      	System.out.println("\'" + n + "\' table is created");
		      	break;
		      case DUPLICATE_COLUMN_DEF_ERROR:
		      	System.out.println("Create table has failed: column definition is duplicated");
		      	break;
		      case DUPLICATE_PRIMARYKEY_DEF_ERROR:
		      	System.out.println("Create table has failed: primary key definition is duplicated");
		      	break;
		  	  case REFERENCE_TYPE_ERROR:
		  	  	System.out.println("Create table has failed: foreign key references wrong type");
		  	  	break;
		  	  case REFERENCE_NON_PRIMARYKEY_ERROR:
		  	  	System.out.println("Create table has failed: foreign key references non primary key column");
		  	  	break;
		  	  case REFERENCE_COLUMN_EXISTENCE_ERROR:
		  	  	System.out.println("Create table has failed: foreign key references non existing column");
		  	  	break;
		  	  case REFERENCE_TABLE_EXISTENCE_ERROR:
		  	  	System.out.println("Create table has failed: foreign key references non existing table");
		  	  	break;
		  	  case NON_EXISTING_COLUMN_DEF_ERROR:
		  	  	System.out.println("Create table has failed: \'" + n + "\' does not exists in column definition");
		  	  	break;
		  	  case TABLE_EXISTENCE_ERROR:
		  	  	System.out.println("Create table has failed: table with the same name already exists");
		  	  	break;
		  	  case DROP_SUCCESS:
		  	  	System.out.println("\'" + n + "\' table is dropped");
		  	  	break;
		  	  case DROP_REFERENCED_TABLE_ERROR:
		  	  	System.out.println("Drop table has failed: \'" + n + "\' is referenced by other table");
		  	  	break;
		  	  case SHOW_TABLES_NO_TABLE:
		  	  	System.out.println("There is no table");
		  	  	break;
		  	  case NO_SUCH_TABLE:
		  	  	System.out.println("No such table");
		  	  	break;
		  	  case CHAR_LENGTH_ERROR:
		  	  	System.out.println("Char length should be over 0");
		  	  	break;
		  	  case INSERT_TYPE_MISMATCH_ERROR:
	  	  	  	System.out.println("Insertion has failed: Types are not matched");
		  	  	break;
		  	  case INSERT_DUPLICATE_PRIMARYKEY_ERROR:
	  	  	  	System.out.println("Insertion has failed: Primary key duplication");
		  	  	break;	  	  
	  		  case INSERT_REFERENTIAL_INTEGRITY_ERROR:
	  	  	  	System.out.println("Insertion has failed: Referential integrity violation");
		  	  	break;
	  		  case INSERT_COLUMN_EXISTENCE_ERROR:
	  	  	  	System.out.println("Insertion has failed: \'" + n + "\' does not exist");
		  	  	break;
	  		  case INSERT_COLUMN_NON_NULLABLE_ERROR:
	  	  	  	System.out.println("Insertion has failed: \'" + n + "\' is not nullable");
		  	  	break;  		  
			  case DELETE_RESULT:
	  	  	  	System.out.println(n + " row(s) are deleted");
		  	  	break;
	  		  case DELETE_REFERENTIAL_INTEGRITY_PASSED:
	  	  	  	System.out.println(n + " row(s) are not deleted due to referential integrity");
		  	  	break;
	  		  case SELECT_TABLE_EXISTENCE_ERROR:
	  	  	  	System.out.println("Selection has failed: \'" + n + "\' does not exist");
		  	  	break;  		  
	  		  case SELECT_COLUMN_RESOLVE_ERROR:
	  	  	  	System.out.println("Selection has failed: fail to resolve \'" + n + "\'");
		  	  	break;  		  
	  		  case WHERE_INCOMPARABLE_ERROR:
	  	  	  	System.out.println("Where clause try to compare incomparable values");
		  	  	break;
			  case WHERE_TABLE_NOT_SPECIFIED:
	  	  	  	System.out.println("Where clause try to reference tables which are not specified");
		  	  	break;
			  case WHERE_COLUMN_NOT_EXIST:
	  	  	  	System.out.println("Where clause try to reference non existing column");
		  	  	break;
	  		  case WHERE_AMBIGUOUS_REFERENCE:
	  	  	  	System.out.println("Where clause contains ambiguous reference");
		  	  	break;
	  		  
		  	  	
		  	  
		    }
		    if(prompt) System.out.print("DB_2019-12345> ");
		}
}
