package custom;

import java.io.Serializable;

public class VarType extends Types implements Serializable {
	public SelectName variable;
	public Types value;
	public VarType(SelectName sn) {
		super(Types.varT);
		this.variable = sn;
		this.value = null;
	}

}
