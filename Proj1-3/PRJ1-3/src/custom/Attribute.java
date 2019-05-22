package custom;

import java.io.Serializable;

/* describe attribute */
public class Attribute implements Serializable {
	public final static int nullT = 0;
	public final static int intT = 1;
	public final static int charT = 2;
	public final static int dateT = 3;
	
	private String name;
	private int type;
	private int length;
	private boolean is_null = true;
	
	public String type_toString() {
		String s = "null";
		switch(this.type) {
		case Attribute.intT:
			s = "int";
			break;
		case Attribute.charT:
			s = "char";
			break;
		case Attribute.dateT:
			s = "date";
			break;
		}
		return s;
	}
	
	public void set_name(String n)
	{
		this.name = n;
	}
	public void set_type(int t)
	{
		this.type = t;
	}
	public void set_len(int l)
	{
		this.length = l;
	}
	
	public void set_is_null(boolean n)
	{
		this.is_null = n;
	}
	
	
	public String get_name()
	{
		return name;
	}
	
	public int get_type()
	{
		return type;
	}
	
	public int get_len()
	{
		return length;
	}
	
	public boolean get_is_null()
	{
		return is_null;
	}
	
	
}
