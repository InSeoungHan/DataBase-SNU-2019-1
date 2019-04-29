package custom;

import java.io.Serializable;

/* describe attribute */
public class Attribute implements Serializable {
	private String name;
	private String type;
	private int length;
	private boolean is_null = true;
	
	public void set_name(String n)
	{
		this.name = n;
	}
	public void set_type(String t)
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
	
	public String get_type()
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
