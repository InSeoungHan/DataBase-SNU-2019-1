package custom;

import java.io.File;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class DatabaseHandler {
	  // Environment & Database define
	  public static Environment myDbEnvironment = null;
	  public static Database myDatabase = null;
	  public static Database myClassDb = null;
	  public static StoredClassCatalog classCatalog = null;
	  public static EntryBinding tableDataBinding = null;
	  
	  public DatabaseHandler() {
		  /* OPENING DB */
			// Open Database Environment or if not, create one.
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			myDbEnvironment = new Environment(new File("db/"), envConfig);
			
			// Open Table Database or if not, create one.
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setSortedDuplicates(false);
			myDatabase = myDbEnvironment.openDatabase(null, "tables", dbConfig);

			// Open Class Database for 'Table' class information or if not, create one.
			myClassDb = myDbEnvironment.openDatabase(null, "classDb", dbConfig);
		    classCatalog = new StoredClassCatalog(myClassDb);
			tableDataBinding = new SerialBinding(classCatalog, Table.class);
	  }
	  
	  /* Close Database and exit */
	  public static void close()
	  {
	    try { 
			if (myClassDb != null) myClassDb.close();
			if (myDatabase != null) myDatabase.close();
			if (myDbEnvironment != null) myDbEnvironment.close();
		} catch(Exception e)
		{
		  e.printStackTrace();
		}
	  }
}
