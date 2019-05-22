package custom;

import java.io.Serializable;

public class NullType extends Types implements Serializable {
	
	public NullType() {
		super(Types.nullT);
	}

}
