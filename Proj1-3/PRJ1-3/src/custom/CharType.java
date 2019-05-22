package custom;

import java.io.Serializable;

public class CharType extends Types implements Serializable {
	public String value;
	
	public CharType(String s) {
		super(Types.charT);
		this.value = s.substring(1, s.length()-1);

	}
	
	/* truncate value to length n */
	public void truncate(int n) {
		this.value = value.substring(0, Math.min(this.value.length(),n));
	}


}
