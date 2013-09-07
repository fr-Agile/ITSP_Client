package jp.ac.titech.itpro.sds.fragile;

import java.io.Serializable;
import java.util.Calendar;


public class StoreData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Calendar cal;
	
	protected StoreData(Calendar cal){
		this.cal = cal;
	}

	public Calendar getCal() {
		return cal;
	}

	public void setCal(Calendar cal) {
		this.cal = cal;
	}
	
	
}
