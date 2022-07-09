package LogicalSimulation;

import java.io.Serializable;

public class InitiateTransfer implements Serializable {

/*
* Used to send the initial SYN data which includes packet size, window size, Number of packets to the receiver.
*/

// Parameters : type {0 : GBN (Go Back N), 1 : SR (Selective Repeat)}, window size, packet size, number of packets
	private static final long serialVersionUID = 1L;
	private int type;
	private int windowSize;
	private long packetSize;
	private int numPackets;

// Setter and Getter

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public long getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(long packetSize) {
		this.packetSize = packetSize;
	}

	public int getNumPackets() {
		return numPackets;
	}

	public void setNumPackets(int numPackets) {
		this.numPackets = numPackets;
	}

// toString method
	@Override
	public String toString() {
		return "LogicalSimulation.InitiateTransfer [type=" + type + ", windowSize=" + windowSize + ", packetSize=" + packetSize
				+ ", numPackets=" + numPackets + "]";
	}

}
