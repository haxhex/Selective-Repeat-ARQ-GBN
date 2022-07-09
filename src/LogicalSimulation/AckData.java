package LogicalSimulation;

import java.io.Serializable;

/*
 * This class is used to send the details about the acknowledgement.
 */
public class AckData implements Serializable {
	
// Parameter : ack number
	private static final long serialVersionUID = 1L;
	int ackNo;

// Setter and getter for ack number
	public int getAckNo() {
		return ackNo;
	}

	public void setAckNo(int ackNo) {
		this.ackNo = ackNo;
	}

// toString method
	@Override
	public String toString() {
		return "LogicalSimulation.AckData [ackNo=" + ackNo + "]";
	}
}
