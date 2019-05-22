package custom;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class DatabaseDefine {
	private static Database DB;
	private static EntryBinding TDB;
	public DatabaseDefine(DatabaseHandler DBH) {
		DB = DBH.myDatabase;
		TDB = DBH.tableDataBinding;
	}
	
	/* describe table */
	  public static void desc(String table_name)
	  {
	    Table table;
	    Cursor cursor = null;
	    DatabaseEntry foundKey;
		DatabaseEntry foundData;
		//Get the table which name is 'table_name' from database
		try {
		  	foundKey = new DatabaseEntry(table_name.getBytes("UTF-8"));
		  	foundData = new DatabaseEntry();
		    cursor = DB.openCursor(null, null);
		    //Check if there is at least one table in database
			if(cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS)
			{
			  errorMsg.printMessage(errorMsg.NO_SUCH_TABLE, "", true);
			}
			else
			{
			  //describe table
			  String keyString = new String(foundKey.getData(), "UTF-8");
			  table = (Table) TDB.entryToObject(foundData);
			  System.out.println("-------------------------------------------------");
			  System.out.println("table_name [" + table.table_name + "]");
			  System.out.println("column_name          type          null          key          ");
			  Iterator<Attribute> it = table.attributes.iterator();
			  while(it.hasNext())
			  {
			    Attribute A = (Attribute) it.next();
			    String name = A.get_name();
			    String keydesc = "";
				String type = A.type_toString();
				char is_null = 'N';
				if(A.get_is_null())
					is_null = 'Y';

				if (type.equalsIgnoreCase("char"))
					type = type + "(" + A.get_len() + ")";
			    
			    if(table.is_primary_key(name))
			    { 
			    	keydesc += "PRI";
			    }
			    if(table.is_foreign_key(name))
			    {
			      if(keydesc.isEmpty())
			      	keydesc += "FOR";
			      else
			      	keydesc += "/FOR";
			    }
				System.out.printf("%-21s%-14s%-14c%-14s\n", name, type, is_null, keydesc);
			  }
			  System.out.println("-------------------------------------------------");
		  	}
		} catch (Exception e) {
		  e.printStackTrace();
		} finally { 
			cursor.close();
	    }
		
	  }
	/* show all table's names in database */
	  public static void showTables()
	  {
	    Table table;
	    Cursor cursor = null;
	    DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();
		
		try {
		    cursor = DB.openCursor(null, null);
		    //Check if there is at least one table in database
		    if(cursor.getFirst(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS)
		   	{
		   	  errorMsg.printMessage(errorMsg.SHOW_TABLES_NO_TABLE, "", true);
		   	  
		   	}
		   	else
		   	{
		   	   //Show tables
			    System.out.println("----------------");
			    do {
			      String keyString = new String(foundKey.getData(), "UTF-8");
				  System.out.println(keyString);
				} while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
				System.out.println("----------------");
			}
		} catch (DatabaseException de) {
		  System.err.println("Error accessing database." + de);
		} catch (UnsupportedEncodingException e) {
		} finally {
		  cursor.close();
		}
	  }
	    /* remove Table from database */
		public static void dropTable(String table_name)
		  {
		    Table table;
		    Cursor cursor = null;
		    DatabaseEntry foundKey, foundData;
			try {
			    foundKey = new DatabaseEntry(table_name.getBytes("UTF-8"));
				foundData = new DatabaseEntry();
			    cursor = DB.openCursor(null, null);
			    //Check if target table is exist
				if(cursor.getSearchKey(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS)
				{
				  table = (Table) TDB.entryToObject(foundData); //table that you want to remove
	  			  //Check if any table doesn't reference this table, So it can be removed
				  if(table.referenced.isEmpty())
				  {
				    //remove references that target table is referencing
				    Iterator<Reference> it = table.referencing.iterator();
					while(it.hasNext())
					{
					  	String referenced_table_name = ((Reference) it.next()).refer_table;
					  	DatabaseEntry key = new DatabaseEntry(referenced_table_name.getBytes("UTF-8"));
					  	DatabaseEntry data = new DatabaseEntry();
					    if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
					    { 
					  	  Table referenced_table = (Table) TDB.entryToObject(data);
					  	  referenced_table.referenced.remove(table_name);
					  	  TDB.objectToEntry(referenced_table, data);
					  	  cursor.putCurrent(data);
					    } 
					}
					//remove target table
				  	DB.delete(null, foundKey);
				  	errorMsg.printMessage(errorMsg.DROP_SUCCESS, table_name, true);
				  }
				  else
				  {
					  errorMsg.printMessage(errorMsg.DROP_REFERENCED_TABLE_ERROR, table_name, true);
				  }
				}
				else
				{
					errorMsg.printMessage(errorMsg.NO_SUCH_TABLE, "", true);
				}
			} catch (DatabaseException de) {
		   	  System.err.println("Error accessing database." + de);
			} catch (UnsupportedEncodingException e) {
			} finally {
		  	  cursor.close();
		    }
			
		}  
	  /* Create Table to database */
	  public static void createTable(Table table)
	  {
		Cursor cursor = null;
		DatabaseEntry key;
		DatabaseEntry data;
		ArrayList<String> referenced_tables = new ArrayList();
		try {
		  cursor = DB.openCursor(null, null);
		  key = new DatabaseEntry(table.table_name.getBytes("UTF-8"));
		  data = new DatabaseEntry();
		
		  //check if target table is already exist
		  if(cursor.getSearchKey(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
		  {
			  errorMsg.printMessage(errorMsg.TABLE_EXISTENCE_ERROR, "", true);
		    cursor.close();
		    return;
		  }

		  
		  //check constraints according to columns(attributes)
		  Iterator it;
		  it = table.attributes.iterator();
	      while(it.hasNext())
	      {
	        Attribute A = (Attribute) it.next();
	        //check if column are not duplicated
	        if(table.is_attribute(A.get_name()) > 1)
		    {
	        	errorMsg.printMessage(errorMsg.DUPLICATE_COLUMN_DEF_ERROR, "", true);
		      cursor.close();
		      return;
	        }
	        //type check(check 'char' type which length is smaller than 1)
			if(A.type_toString().equalsIgnoreCase("char") && (A.get_len() < 1))
			{
				errorMsg.printMessage(errorMsg.CHAR_LENGTH_ERROR, "", true);
		      cursor.close();
		      return;
			}
	      }
		    
		  //check if primary keys is actually in attributes
		  it = table.primary_keys.iterator();
		  while(it.hasNext())
		  {
		    String primary_key = (String) it.next();
		    if(table.is_attribute(primary_key) < 1)
		    { 
		    	errorMsg.printMessage(errorMsg.NON_EXISTING_COLUMN_DEF_ERROR, primary_key, true);
		      cursor.close();
	          return;
	        }
	        //if it is correct primary key, then corresponding attribute should be "not null"
	        Attribute A = table.get_attribute(primary_key);
	        A.set_is_null(false);
		  }

		  /* check for foreign key constraints */
		  it = table.referencing.iterator();
		  while(it.hasNext())
		  {
		    Reference Reference = (Reference) it.next();
		    //check if foreign keys is actually in attributes
		    Iterator it2 = Reference.refer_chaser_names.iterator();
		    while(it2.hasNext())
		    {
		      String chaser = (String) it2.next();
		      if(table.is_attribute(chaser) <  1)
		      {
		    	  errorMsg.printMessage(errorMsg.NON_EXISTING_COLUMN_DEF_ERROR, chaser, true);
		        cursor.close();
	            return;
		      }
		    }
			//check if the referenced table is actually exist
		    key = new DatabaseEntry(Reference.refer_table.getBytes("UTF-8"));
		    data = new DatabaseEntry();

		    if(cursor.getSearchKey(key, data, LockMode.DEFAULT) != OperationStatus.SUCCESS)
		    {
		    	errorMsg.printMessage(errorMsg.REFERENCE_TABLE_EXISTENCE_ERROR, "", true);
		      cursor.close();
		      return;
		    }

			//Check if reference column is actually exist and primary key
			Table referenced_table = (Table) TDB.entryToObject(data);
			
			//collect table names of referenced table for later purpose(A)
			referenced_tables.add(referenced_table.table_name);

		    it2 = Reference.refer_target_names.iterator();
		    while(it2.hasNext())
		    {
		      String target = (String) it2.next();
		      if(referenced_table.is_attribute(target) <  1)
		      {
		    	  errorMsg.printMessage(errorMsg.REFERENCE_COLUMN_EXISTENCE_ERROR, "", true);
		        cursor.close();
	            return;
		      }
		      if(!referenced_table.is_primary_key(target))
		      {
		    	  errorMsg.printMessage(errorMsg.REFERENCE_NON_PRIMARYKEY_ERROR, "", true);
		        cursor.close();
	            return;
		      }
		    }

		    //check if arities of referenced and referencing attributes are the same 
		    int size = Reference.refer_target_names.size();

			//check if foreign keys reference all attributes in primary key
			if(size != referenced_table.primary_keys.size())
			{
				errorMsg.printMessage(errorMsg.REFERENCE_NON_PRIMARYKEY_ERROR, "", true);
		        cursor.close();
	            return;
		    }
		    
		    if(size != Reference.refer_chaser_names.size())
		    {
		    	errorMsg.printMessage(errorMsg.REFERENCE_TYPE_ERROR, "", true);
		      cursor.close();
	          return;
	        }

	        for(int i = 0; i < size; i++)
	        {
	          Attribute temp1 = table.get_attribute(Reference.refer_chaser_names.get(i));
	          Attribute temp2 = referenced_table.get_attribute(Reference.refer_target_names.get(i));
	          if(temp1.type_toString().equalsIgnoreCase(temp2.type_toString()) && temp1.get_len() == temp2.get_len())
	          	continue;
	          else
	          {
	        	  errorMsg.printMessage(errorMsg.REFERENCE_TYPE_ERROR, "", true);
		        cursor.close();
	            return;
	          }
	          
	        }
	        
				   
		  }

		  /*(A) adding table_name for referencing table to referenced table */ 
		  it = referenced_tables.iterator();
		  while(it.hasNext())
		  {
		  	String table_name = (String) it.next();
		  	key = new DatabaseEntry(table_name.getBytes("UTF-8"));
		  	data = new DatabaseEntry();
		  	cursor.getSearchKey(key, data, LockMode.DEFAULT);
		  	Table referenced_table = (Table) TDB.entryToObject(data);
		  	referenced_table.referenced.add(table.table_name);
		  	TDB.objectToEntry(referenced_table, data);
		  	cursor.putCurrent(data); 
		  }

		  /* create table */
		  key = new DatabaseEntry(table.table_name.getBytes("UTF-8"));
		  data = new DatabaseEntry();
	      TDB.objectToEntry(table, data);
		  cursor.put(key, data);
		  errorMsg.printMessage(errorMsg.CREATE_TABLE_SUCCESS, table.table_name, true);
		  cursor.close();
		} catch (DatabaseException de) {
		  de.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		  e.printStackTrace();
		} catch (Exception g) {
		  g.printStackTrace();
		}

	  }
}
