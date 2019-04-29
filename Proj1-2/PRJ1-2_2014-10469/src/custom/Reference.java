package custom;

import java.io.Serializable;
import java.util.ArrayList;

/*this class describe foreign key relation */
public class Reference implements Serializable{
	public ArrayList<String> refer_chaser_names; /* foreign key */
	public String refer_table; /* reference table */
	public ArrayList<String> refer_target_names; /* referenced columns by foreign key */
	
}
