package custom;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;


public class JoinHandler {
	Database tableDB;
	EntryBinding tableBinding;
	ArrayList<Table> tables;
	int[] indexList;
	public ArrayList<Types> record;
	public ArrayList<SelectName> record_name;
	public ArrayList<Attribute> record_attributes;
	public int error_flag;
	
	public JoinHandler(ArrayList<SelectName> stnl, Database tableDB, EntryBinding tableDataBinding) throws UnsupportedEncodingException {
		this.error_flag = 0;
		this.tableDB = tableDB;
		this.tableBinding = tableDataBinding;
		this.record = new ArrayList<Types>(); 
		this.record_name = new ArrayList<SelectName>();
		this.record_attributes = new ArrayList<Attribute>();
		this.tables = new ArrayList<Table>();
		this.indexList = new int[stnl.size()];
		
		int i;
		// select *을 대비해서 먼저 record_name을 만들어준다.
		for(i = 0; i < stnl.size(); i++) {
			DatabaseEntry tempKey = new DatabaseEntry(stnl.get(i).tname.getBytes("UTF-8"));
			DatabaseEntry tempData = new DatabaseEntry();
			tableDB.get(null, tempKey, tempData, LockMode.DEFAULT);
			Table temp_table = (Table) tableBinding.entryToObject(tempData);
			Iterator it;
			
			it = temp_table.attributes.iterator();
			while(it.hasNext()) {
				Attribute a = (Attribute) it.next();
				record_attributes.add(a);
				record_name.add(new SelectName(a.get_name(), (stnl.get(i).dname == null)?stnl.get(i).tname : stnl.get(i).dname, null)); 
			}
		}
		
		for(i = 0; i < stnl.size(); i++) {
			indexList[i] = 0;
			ArrayList<Types> temp_record;
			Table temp_table;
			Iterator it;
			DatabaseEntry tempKey = new DatabaseEntry(stnl.get(i).tname.getBytes("UTF-8"));
			DatabaseEntry tempData = new DatabaseEntry();
			
			//we don't need to check Status value because tables check is done before this statement.
			tableDB.get(null, tempKey, tempData, LockMode.DEFAULT);
			temp_table = (Table) tableBinding.entryToObject(tempData);
			
			if(temp_table.records.size() == 0) {
				error_flag = -2;
				return;
			}
			tables.add(temp_table);
			//initiate first joined record and record name
			record.addAll(temp_table.records.get(0));
			
		}
		
	}
	/* get next joined record */
	public void getNext() { 
		int i;
		int j;
		for (i = 0; i < this.tables.size(); i++) {
			indexList[i] += 1;
			if(indexList[i] < this.tables.get(i).records.size()) {
				break;
			}
		}
		if(i != this.tables.size()) {
			for(j = 0; j < i; j++) {
				indexList[j] = 0;
			}
			makeRecord(i);
			return;
		}
		this.error_flag = -1;
		return;
	}
	
	/* make joined record */
	public void makeRecord(int index) {
		int i;
		int k = 0;
		for (i = 0; i <= index; i++) {
			ArrayList<Types> temp = this.tables.get(i).records.get(indexList[i]);
			int j;
			for(j = 0; j < temp.size(); j++) {
				record.set(k, temp.get(j));
				k++;
			}
		}
	}
	
}
