package custom;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;

/* this class has information about table schema */
public class Table implements Serializable{
	public String table_name;
	public ArrayList<Attribute> attributes;
	public ArrayList<String> primary_keys;
	public ArrayList<Reference> referencing;
	public ArrayList<String> referenced;
	public ArrayList<ArrayList<Types>> records;
	public Table() {
		attributes = new ArrayList<Attribute>();
		primary_keys = new ArrayList<String>();
		referencing = new ArrayList<Reference>();
		referenced = new ArrayList<String>();
		records = new ArrayList<ArrayList<Types>>();
		
	}
	
	/* get index of attribute */
	public int indexOfAttribute(String attribute_name) {
		int i;
		Attribute A;
		for(i = 0; i < attributes.size(); i++)
		{
			A = attributes.get(i);
			if(attribute_name.equalsIgnoreCase(A.get_name()))
					return i;
		}
		return -1;
	}
	
	/* search attribute in attributes(List) */
	public Attribute get_attribute(String attribute_name)
	{
		int i;
		Attribute A;
		for(i = 0; i < attributes.size(); i++)
		{
			A = attributes.get(i);
			if(attribute_name.equalsIgnoreCase(A.get_name()))
					return A;
		}
		return null;
		
	}
	
	/* check whether it is attribute name exist in attributes */
	/* and return count that the name is encountered during */
	public int is_attribute(String attribute_name)
	{
		int count = 0;
		Iterator<Attribute> it = attributes.iterator();
		while(it.hasNext())
		{
			Attribute A = (Attribute) it.next();
			if(attribute_name.equalsIgnoreCase(A.get_name()))
				count += 1;
		}
		return count;
	}
	
	/* check if it is primary_key */
	public boolean is_primary_key(String attribute_name)
	{
		if(primary_keys.contains(attribute_name))
			return true;
		return false;
	}
	
	/* check if it is foreign_key */
	public boolean is_foreign_key(String attribute_name)
	{
		Iterator<Reference> it = referencing.iterator();
		while(it.hasNext())
		{
			Reference R = (Reference) it.next();
			if(R.refer_chaser_names.contains(attribute_name))
				return true;
		}
		return false;
	}
	
	/* get index of reference in which there is table_name for refer_table */
	public int indexOfReference(String table_name) {
		for(int i = 0; i < referencing.size(); i++) {
			if(referencing.get(i).refer_table.equalsIgnoreCase(table_name))
				return i;
		}
		return -1;
	}
}
