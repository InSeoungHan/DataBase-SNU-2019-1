package custom;

import java.io.Serializable;

public class SelectName implements Serializable{
	public String cname;
	public String dname;
	public String tname;
	
	public SelectName() {
		
	}
	public SelectName(String cname, String tname, String dname) {
		this.cname = cname;
		this.dname = dname;
		this.tname = tname;
	}
	
	@Override
	public boolean equals(Object o) 
	{
	    if (o instanceof SelectName) 
	    {
	    	SelectName c = (SelectName) o;
	      if(this.dname != null && c.dname != null) {
	    	  return this.dname.equals(c.dname);
	      }
	      else if(this.tname != null && c.tname != null)
	         return (this.tname.equals(c.tname)) && (this.cname.equalsIgnoreCase(c.cname));
	      else
	    	 return (this.cname.equalsIgnoreCase(c.cname));
	    }
	    return false;
	}
	
	//check if this name is resolved to sn
	public boolean resolve(SelectName sn) {
		if(this.tname == null && this.cname.equalsIgnoreCase(sn.cname)) {
			return true;
		}
		else {
			String temp = sn.dname;
			if(temp == null) temp = sn.tname;
			if(temp.equals(this.tname) && this.cname.equalsIgnoreCase(sn.cname)) 
				return true;
		}
		return false;
	}
}
