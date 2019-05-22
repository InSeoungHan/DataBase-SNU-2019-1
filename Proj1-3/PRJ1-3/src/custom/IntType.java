package custom;

import java.io.Serializable;

public class IntType extends Types implements Serializable {
	public int value;
	
	public IntType(String s) {
		super(Types.intT);
		this.value = Integer.parseInt(s);
	}

}
