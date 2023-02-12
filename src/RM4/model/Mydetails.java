/**
 * 
 */
package RM4.model;

import java.util.List;

/**
 * @author Krishna Patel
 *
 */
	public class Mydetails {
		List<String> Uid;
		public Mydetails(List<String> uid, int slots) {
			super();
			Uid = uid;
			this.slots = slots;
		}
		int slots;
		public List<String> getUid() {
			return Uid;
		}
		public void setUid(List<String> uid) {
			Uid = uid;
		}
		public int getSlots() {
			return slots;
		}
		public void setSlots(int slots) {
			this.slots = slots;
		}

	}

