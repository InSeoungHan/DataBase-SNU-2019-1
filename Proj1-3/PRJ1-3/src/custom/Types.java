package custom;

import java.io.Serializable;

public class Types implements Serializable {
	public final static int T = 1;
	public final static int F = -1;
	public final static int U = 0;
	public final static int nullT = 0;
	public final static int intT = 1;
	public final static int charT = 2;
	public final static int dateT = 3;
	public final static int varT = 4;
	public final static int expT = 5;
	public int typeN;

	public Types(int t) {
		this.typeN = t;
	}
	
	public String toString() {
		String temp = "";
		switch(this.typeN) {
    	case Types.intT:
    		temp = String.valueOf(((IntType) this).value);
    		break;
    	case Types.charT:
    		temp = ((CharType) this).value;
    		break;
    	case Types.dateT:
    		temp = String.valueOf(((DateType) this).year) + "-" + String.valueOf(((DateType) this).month) + "-" + String.valueOf(((DateType) this).day);
    		break;
    	case Types.nullT:
    		temp = "null";
    		break;
		}
		return temp;
		
	}
	
	public int equals(Types other) throws WhereIncomparableException
	{		
    	switch(this.typeN) {
    	case Types.intT:
    		if(other.typeN == Types.nullT) return U;
    		if(other.typeN != Types.intT) throw new WhereIncomparableException();
    		return (((IntType) this).value == ((IntType) other).value)?T:F; 
    	case Types.charT:
    		if(other.typeN == Types.nullT) return U;
    		if(other.typeN != Types.charT) throw new WhereIncomparableException();
    		return (((CharType) this).value.equals(((CharType) other).value))?T:F;
    	case Types.dateT:
    		if(other.typeN == Types.nullT) return U;
    		if(other.typeN != Types.dateT) throw new WhereIncomparableException();
    		return (((DateType) this).year == ((DateType) other).year && ((DateType) this).month == ((DateType) other).month && ((DateType) this).day == ((DateType) other).day)?T:F;
    		
    	case Types.nullT:
    		return U;
    	}
    	
    	//it can't reach here
    	return T;
	}
	
	  public int compareTo(Types other) throws WhereIncomparableException {
		if (this.equals(other) == T) {
	      return 0;
	    }
	    
	    switch(this.typeN) {
	    case Types.intT:
	    	if(other.typeN != Types.intT) throw new WhereIncomparableException();
	    	if(((IntType) this).value > ((IntType) other).value) return 1;
	    	else return -1;
	    case Types.charT:
	    	if(other.typeN != Types.charT) throw new WhereIncomparableException();
	    	return ((CharType) this).value.compareTo(((CharType) other).value);
	    case Types.dateT:
	    	if(other.typeN != Types.dateT) throw new WhereIncomparableException();
	    	if(((DateType) this).year != ((DateType) other).year) {
	    		if(((DateType) this).year > ((DateType) other).year) return 1;
	    		else return -1;
	    	}
	    	else if(((DateType) this).month != ((DateType) other).month) {
	    		if(((DateType) this).month > ((DateType) other).month) return 1;
	    		else return -1;
	    	}
	    	else {
	    		if(((DateType) this).day > ((DateType) other).day) return 1;
	    		else return -1;
	    	}
	    }
	    //it can't reach here
	    return 0;
	  }

}
