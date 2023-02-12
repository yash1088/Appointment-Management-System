/**
 * 
 */
package RM1.model;

import java.util.List;

/**
 * @author Sumit Monapara
 *
 */
public class AptDetails {
	
	private List<String> patientID;
	private int capacity;
	/**
	 * @param patientID
	 * @param capacity
	 */
	public AptDetails(List<String> patientID, int capacity) {
		super();
		this.patientID = patientID;
		this.capacity = capacity;
	}
	
	public List<String> getPatientID() {
		return patientID;
	}
	
	public void setPatientID(List<String> patientID) {
		this.patientID = patientID;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public String toString() {
		return "AptDetails [patientID=" + patientID + "]";
	}
	
	
}
