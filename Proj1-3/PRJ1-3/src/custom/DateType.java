package custom;

import java.io.Serializable;

public class DateType extends Types implements Serializable{
	public int year;
	public int month;
	public int day;
	
	public DateType(String s) {
		super(Types.dateT);
		String[] temp = s.split("-");
		this.year = Integer.parseInt(temp[0]);
		this.month = Integer.parseInt(temp[1]);
		this.day = Integer.parseInt(temp[2]);
	}


}
