package custom;

import java.util.ArrayList;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class DatabaseManage {
	private static Database DB;
	private static EntryBinding TDB;
	
	public DatabaseManage(DatabaseHandler DBH) {
		DB = DBH.myDatabase;
		TDB = DBH.tableDataBinding;
	}
	
	  //delete record from table
	public static void delete(String table_name, ExpressionType EXP) {
	  try { 
			  DatabaseEntry Key = new DatabaseEntry(table_name.getBytes("UTF-8"));
		      DatabaseEntry Data = new DatabaseEntry();
		      Table table;
		      ArrayList<Types> record;
		      ArrayList<SelectName> record_name = new ArrayList<SelectName>();
		      
		      int deleteCount = 0;
		      int cancelCount = 0;

		      //check if the query try to delete from the table which is not exist
			  if(DB.get(null, Key, Data, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
				errorMsg.printMessage(errorMsg.NO_SUCH_TABLE, "", true);
				return;
			  }
			  table = (Table) TDB.entryToObject(Data);
			  //record_name will be arguments for solving where clause 
			  for(int i = 0; i < table.attributes.size(); i++) {
			    record_name.add(new SelectName(table.attributes.get(i).get_name(), table_name, null));
			  }
			  
			  ArrayList<Table> refTables = new ArrayList<Table>();
			  for(int i = 0; i < table.referenced.size(); i++) {
	        	     DatabaseEntry tempKey = new DatabaseEntry(table.referenced.get(i).getBytes("UTF-8"));
	      		     DatabaseEntry tempData = new DatabaseEntry();
	      		     DB.get(null, tempKey, tempData, LockMode.DEFAULT);
	      		     Table refTable = (Table) TDB.entryToObject(tempData);
	      		     refTables.add(refTable);
			  }
			  //delete record by record
			  //First, each record is tested by where clause. if where clause return true, then delete the record if other error conditions is not exist
		      for(int r = 0; r < table.records.size(); r++) {
		        	 record = table.records.get(r);
		        	 int result = Types.F;
		        	 try {
		        	   		//solve where clause by using record and its name list.
		        	   		//below exceptions are errors which can occur during solving the where clause.
		        	  		result = EXP.solve(record, record_name, table.attributes);
						} catch (WhereIncomparableException e) {
							errorMsg.printMessage(errorMsg.WHERE_INCOMPARABLE_ERROR, "", true);

						  return;
						} catch (WhereTableNotSpecifiedException e) {
							errorMsg.printMessage(errorMsg.WHERE_TABLE_NOT_SPECIFIED, "", true);
						  return;
						} catch (WhereColumnNotExistException e) {
							errorMsg.printMessage(errorMsg.WHERE_COLUMN_NOT_EXIST, "", true);
						  return;
						} catch (WhereAmbiguousReference e) {
							errorMsg.printMessage(errorMsg.WHERE_AMBIGUOUS_REFERENCE, "", true);
						  return;
						}

					// where clause returns TRUE
		        	 if(result == Types.T) {
		        	   boolean deleteOk = true;
		        	   //check Integrity constraints hold after deletion the record.
		        	   //with traversing all tables which reference the table from which the record is deleted 
		        	   for(int i = 0; i < refTables.size(); i++) {
		        		   		
		      		     Table refTable = refTables.get(i);
						 
						 //if there is no other records which are same as the record for all other tables or
						 // other records can be set by null, then "deleteOk" will be true. OtherWise, false.
						 for(int t = 0; t < refTable.records.size(); t++) {
								boolean equal = true;
							    ArrayList<Types> refRecord = refTable.records.get(t);
							    int referIndex = refTable.indexOfReference(table_name);
							    //check if there are records same as the record
							    //if yes, then equal = true
							    for(int k = 0; k < refTable.referencing.get(referIndex).refer_chaser_names.size(); k++) {
							      int n = table.indexOfAttribute(refTable.referencing.get(referIndex).refer_target_names.get(k));
							      int m = refTable.indexOfAttribute(refTable.referencing.get(referIndex).refer_chaser_names.get(k));
							      if(record.get(n).equals(refRecord.get(m)) != Types.T) { 
							      	equal = false;
							      	break;
							     }
							    }
							    //Even if same records are exist, if these record can be set by null, then deletOk is true;
							    if(equal) {
							      	for(int k = 0; k < refTable.referencing.get(referIndex).refer_chaser_names.size(); k++) {
								    	if(!refTable.get_attribute(refTable.referencing.get(referIndex).refer_chaser_names.get(k)).get_is_null()) {
								    	  deleteOk = false;
								    	  break;
								    	}
								    }
							      	//
								    break;
								}
						 }
						 
						//if deleteOk is false until here, then the record can't be deleted. Find other records to be deleted.
						 if(deleteOk == false) break;

		        	   }
						//Even if same records are exist, set these record to null for only primary key part of the table from which the record is deleted. 
		        	   if(deleteOk) {
						   for(int i = 0; i < refTables.size(); i++) {
						     Table refTable = refTables.get(i);
						     int referIndex = refTable.indexOfReference(table_name);
						     for(int j = 0; j < refTable.records.size(); j++) {
						    	   ArrayList<Types> old_record = refTable.records.get(j);
							       
							       boolean equal = true;
							       for(int k = 0; k < refTable.referencing.get(referIndex).refer_chaser_names.size(); k++) {
								      int n = table.indexOfAttribute(refTable.referencing.get(referIndex).refer_target_names.get(k));
								      int m = refTable.indexOfAttribute(refTable.referencing.get(referIndex).refer_chaser_names.get(k));
								      if(record.get(n).equals(old_record.get(m)) != Types.T) { 
								      	equal = false;
								      	break;
								      }
							       }
								    if(equal) {
								      for(int k = 0; k < refTable.referencing.get(referIndex).refer_chaser_names.size(); k++) {
								    	  int m = refTable.indexOfAttribute(refTable.referencing.get(referIndex).refer_chaser_names.get(k));
								    	  old_record.set(m, new Types(Types.nullT));
								      }
								      //delete old one and recreate the same record except that the foreign key part is filled with null value.
								      //setnullRecord append it until all works have done with the referencing table.
									}
						     }
						     //add records with its foreign key parts set by null
						   }
						   table.records.remove(r);
						   r--;
						   
			        	   //delete success
			        	   deleteCount += 1;
			        	   
			           } else {
			             //delete fail
			             cancelCount += 1;
			           }
		        	}
		      }
		      
		      for(int i = 0; i < refTables.size(); i++) {
		    	  TDB.objectToEntry(refTables.get(i), Data);
		    	  DB.put(null, new DatabaseEntry(refTables.get(i).table_name.getBytes("UTF-8")), Data);
		      }
		      TDB.objectToEntry(table, Data);
	    	  DB.put(null, new DatabaseEntry(table_name.getBytes("UTF-8")), Data);
	    	  
		      //print message
		      errorMsg.printMessage(errorMsg.DELETE_RESULT, String.valueOf(deleteCount), !(cancelCount > 0));
		      if(cancelCount > 0)
		    	  errorMsg.printMessage(errorMsg.DELETE_REFERENTIAL_INTEGRITY_PASSED, String.valueOf(cancelCount), true);
		  } catch (Exception e) {System.out.println(e.getMessage());}
	}
	
	
	/*insert record into table */
	public static void insert(String table_name, ArrayList<Types> record, ArrayList<String> record_match_attributes) {
	  	DatabaseEntry Key;
		DatabaseEntry Data;
		Table table;

		// check if # of column and # of values from query are the same
		if(record_match_attributes.size() != 0 && record.size() != record_match_attributes.size()) {
			errorMsg.printMessage(errorMsg.INSERT_TYPE_MISMATCH_ERROR, "", true);
			return;
		}
		
		try {
		    Key = new DatabaseEntry(table_name.getBytes("UTF-8"));
		    Data = new DatabaseEntry();
			// check if table which the record is inserted into is exist
			if(DB.get(null, Key, Data, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
				errorMsg.printMessage(errorMsg.NO_SUCH_TABLE, "", true);
				return;
			}
		    table = (Table) TDB.entryToObject(Data);
			//check if # of columns in record and # of columns in table is the same
		    //if column list is given, empty columns are allowed only if that attributes are nullable
			if(record.size() != table.attributes.size()) {
				if(record_match_attributes.size() == 0) {
					errorMsg.printMessage(errorMsg.INSERT_TYPE_MISMATCH_ERROR, "", true);
					return; 
				} else {
					
					for(int i = 0; i < table.attributes.size(); i++) {
						if(record_match_attributes.contains(table.attributes.get(i).get_name()))
							continue;
						else {
							if(!table.attributes.get(i).get_is_null())
							{
								errorMsg.printMessage(errorMsg.INSERT_COLUMN_NON_NULLABLE_ERROR, table.attributes.get(i).get_name(), true);
								return; 
							}
						}
					}
					
				}
			}
			
			if(record_match_attributes.size() == 0) {
				for(int j = 0; j < table.attributes.size(); j++) {
				      // check if the given record types and actual types of attributes is the same
				      if(record.get(j).typeN == Types.nullT) {
				        //check if the record value is null and the attribute do not support null value
				      	if(!table.attributes.get(j).get_is_null()) {  
				      		errorMsg.printMessage(errorMsg.INSERT_COLUMN_NON_NULLABLE_ERROR, table.attributes.get(j).get_name(), true);
						  	return;
					 	}
					 	else
					 		break;
					  }
					  
					  //check if there is attempts to insert unknown columns 
					  else if(record.get(j).typeN != table.attributes.get(j).get_type()) {
						  errorMsg.printMessage(errorMsg.INSERT_TYPE_MISMATCH_ERROR, "", true);
					  	return;
				  	  }
				}
			} else {
				//check if the query try to insert values of non existing column
				for(int i = 0; i < record_match_attributes.size(); i++) {
				  if(table.is_attribute(record_match_attributes.get(i)) <  1) {
					  errorMsg.printMessage(errorMsg.INSERT_COLUMN_EXISTENCE_ERROR, record_match_attributes.get(i), true);
				    return;
				  }
				}
				//if values is not sorted by schema order, sort them.
				//new_rec is same as the record except that it is ordered. 
			    ArrayList<Types> new_rec = new ArrayList<Types>();
			    boolean exist_column = false;
			  	for(int j = 0; j < table.attributes.size(); j++) {
			  	  	int i;
					for (i = 0; i < record_match_attributes.size(); i++) {
					    if(record_match_attributes.get(i).equalsIgnoreCase(table.attributes.get(j).get_name())) {
					      exist_column = true;
					      // check if the given record types and actual types of attributes is the same
					      if(record.get(i).typeN == Types.nullT) {
					        //check if the record value is null and the attribute do not support null value
					        if(!table.attributes.get(j).get_is_null()) {
					        	errorMsg.printMessage(errorMsg.INSERT_COLUMN_NON_NULLABLE_ERROR, record_match_attributes.get(i), true);
						  		return;
						 	}
						 	else
						 		break;
						  }
						  //check if the record value is null and the attribute do not support null value 
						  else if(record.get(i).typeN != table.attributes.get(j).get_type()) {
							  errorMsg.printMessage(errorMsg.INSERT_TYPE_MISMATCH_ERROR, "", true);
						  	return;
					  	  }
						  
					  	  break;
					    }
				  }
				  if(exist_column)
					  new_rec.add(record.get(i));
				  else
					  new_rec.add(new NullType());
				  exist_column = false;

			   }
			   record = new_rec;
			}

			//truncate char types
			for(int i = 0; i < record.size(); i++) {
			  if(record.get(i).typeN == Types.charT) {
				((CharType) record.get(i)).truncate(table.attributes.get(i).get_len());
			  }
			}

			//check if the record makes duplicate value of primary key
			for(int i = 0; i < table.records.size(); i ++) {
				boolean equal = true;
			    ArrayList<Types> other_record = table.records.get(i);

				if(table.primary_keys.size() == 0)
					break;
			    
			    for(int r = 0; r < table.primary_keys.size(); r++) {
			      int j = table.indexOfAttribute(table.primary_keys.get(r));
			      if(record.get(j).equals(other_record.get(j)) != Types.T) { 
			      	equal = false;
			      	break;
			     }
			    }
			    if(equal) {
			    	errorMsg.printMessage(errorMsg.INSERT_DUPLICATE_PRIMARYKEY_ERROR, "", true);
				    return;
				}
			}
			//check if the record violate foreign key integrity constraints
			//foreign keys of inserted record should be also in the referenced table or all of them should are null values.
			boolean exist_value = true;
			for(int i = 0; i < table.referencing.size(); i++) {
			  Reference tempRef = table.referencing.get(i);
			  DatabaseEntry refKey = new DatabaseEntry(tempRef.refer_table.getBytes("UTF-8"));
			  DB.get(null, refKey, Data, LockMode.DEFAULT);
			  Table refTable = (Table) TDB.entryToObject(Data);
			  exist_value = false;

			  for(int j = 0; j < tempRef.refer_chaser_names.size(); j++) {
					  int n = table.indexOfAttribute(tempRef.refer_chaser_names.get(j));
					  if(record.get(n).typeN == Types.nullT) {
						exist_value = true;
					    break;
				  }
			  }

			  if(exist_value)
			  	continue;
			  
		      for(int r = 0; r < refTable.records.size(); r++) {
	    	    exist_value = true;
	    	    ArrayList<Types> other_record = refTable.records.get(r);
	    	    for(int j = 0; j < tempRef.refer_chaser_names.size(); j++) {
		      	  	int n = table.indexOfAttribute(tempRef.refer_chaser_names.get(j));
		    	  	int m = refTable.indexOfAttribute(tempRef.refer_target_names.get(j));
				    if(record.get(n).equals(other_record.get(m)) != Types.T) {
				    	exist_value = false;
				    	break;
				    }
				}

				
				if(exist_value)
					break;
		      }

			  if(!exist_value)
			  	break;
			  
			}

			if(!exist_value) {
				errorMsg.printMessage(errorMsg.INSERT_REFERENTIAL_INTEGRITY_ERROR, "", true);
	      	  return;
			}
			

		    //print message
			table.records.add(record);
			TDB.objectToEntry(table, Data);
		    DB.put(null, Key, Data);
		    errorMsg.printMessage(errorMsg.INSERT_RESULT, "", true);
		    } catch (Exception e){
		      System.out.println(e.getMessage()); }
	}
	
	  //select values from table 
	  public static void select(ArrayList<SelectName> SNL, ArrayList<SelectName> STNL, ExpressionType EXP) {
	    ArrayList<ArrayList<Types>> recordList = new ArrayList<ArrayList<Types>>();
	    int[] lengthList = new int[SNL.size()];
	    JoinHandler JH = null;
	    
		try {
		    //check if there is unknown table in from clause
			for(int i = 0; i < STNL.size(); i++) {
			  DatabaseEntry tempKey = new DatabaseEntry(STNL.get(i).tname.getBytes("UTF-8"));
			  DatabaseEntry tempData = new DatabaseEntry();
			  if(DB.get(null, tempKey, tempData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
			    errorMsg.printMessage(errorMsg.SELECT_TABLE_EXISTENCE_ERROR, STNL.get(i).tname, true);
		    	return;
			  }
			}

		    //JoinHandler initialze
			
		    JH = new JoinHandler(STNL, DB, TDB);
		    
		    //asterisk check
		    if(SNL.get(0).cname == "*") {
			    SNL = JH.record_name;
			    lengthList = new int[SNL.size()];
		    }
		    //doing while statement for all composition(cartesian product) of given tables.
		    //if error_flag is not 0, it means that traversal is over
			while(JH.error_flag == 0) {
			    int result = Types.F;
			    try {
			     	 //solve where clause by using record and its name list.
	    	   		//below exceptions are errors which can occur during solving the where clause.
			    	
					result = EXP.solve(JH.record, JH.record_name, JH.record_attributes);
					
					
				} catch (WhereIncomparableException e) {
					errorMsg.printMessage(errorMsg.WHERE_INCOMPARABLE_ERROR, "", true);
				  return;
				} catch (WhereTableNotSpecifiedException e) {
					errorMsg.printMessage(errorMsg.WHERE_TABLE_NOT_SPECIFIED, "", true);
				  return;
				} catch (WhereColumnNotExistException e) {
					errorMsg.printMessage(errorMsg.WHERE_COLUMN_NOT_EXIST, "", true);
				  return;
				} catch (WhereAmbiguousReference e) {
					errorMsg.printMessage(errorMsg.WHERE_AMBIGUOUS_REFERENCE, "", true);
				  return;
				}
				if(result == Types.T) {
				  int i;
				  int j;
				  
				  ArrayList<Types> record = new ArrayList<Types>();

					//check if SNL is ambiguous
				  for (i = 0; i < SNL.size(); i++) {
					
				    int resolve_count = 0;
				  	for (j = 0; j < JH.record.size(); j++) {
				  		if(SNL.get(i).resolve(JH.record_name.get(j))) {
				  		  resolve_count += 1;
				  		  record.add(JH.record.get(j));
				  		}
				  	}
				  	if(resolve_count != 1) {
				  		errorMsg.printMessage(errorMsg.SELECT_COLUMN_RESOLVE_ERROR, SNL.get(i).cname, true);
		    			return;  
				  	}
				  }
				  
				  //lengthList making
				  //it is used in message format 
				  for(i = 0; i < lengthList.length; i++) { 
				  	lengthList[i] = Math.max(lengthList[i], record.get(i).toString().length());
				  }

				  
				  recordList.add(record);

				}
				JH.getNext();
			}
			int i;
			
			//print all records
			String line = "+";
			String describe_line = "|";
			for(i = 0; i < SNL.size(); i++)
			{
			  SelectName temp = SNL.get(i);
			  String name;
			  if(temp.dname != null)
			  	name = temp.dname;
			  else
			  	name = temp.cname;
			  lengthList[i] = Math.max(lengthList[i], name.length());
			  line += "-".repeat(lengthList[i]+2) + "+";
			  describe_line += " " + name + " ".repeat(lengthList[i] - name.length()) + " |";
			}
			System.out.println(line);
			System.out.println(describe_line);
			System.out.println(line);

			for(i = 0; i < recordList.size(); i++)
			{
			  ArrayList<Types> temp = recordList.get(i);
			  String temp_line = "|";
			  int j;
			  for(j = 0; j < temp.size(); j++) {
			    String s = temp.get(j).toString();
			  	temp_line += " " + s + " ".repeat(lengthList[j] - s.length()) + " |";
			  }
			  System.out.println(temp_line);
			}

			System.out.println(line);
			
		} catch (Exception e) {
		  System.out.println(e.getMessage());
		  };
	  }
	  
}
