package LogicalSimulation;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class MyReceiver {

// font colors
	public static final String RED_BOLD = "\033[1;31m";    // RED
	public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
	public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
	public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
	public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
	public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
	public static final String ANSI_RESET = "\u001B[0m";

// Set probability of losing packet.
	public static final double LOST_PACK_PROBABILITY = 0.1;

	public static void main(String[] args) {
// For calculating duration of execution time consider current time as start time.
		long startTime = System.currentTimeMillis();
		DatagramSocket socket = null;
		int portNumber = 0;
// Checks the number and formats of the command line arguments passed
		if (args.length == 1) {
// Get port number as first argument.
			portNumber = Integer.parseInt(args[0]);

// If the number of input argument is incorrect print this message for guiding.
		} else {
			System.out.println(RED_BOLD + "Invalid Parameters argv[0] - PortNumber/n" + ANSI_RESET);
		}

// Define array of bytes for storing incoming data.
		byte[] incomingData = new byte[1024];
// Set a new socket and the receiver announces that it is ready to accept packet from specific input port number.
		try {
			socket = new DatagramSocket(portNumber);
			System.out.println(CYAN_BOLD + "Receiver Side is Ready to Accept Packets at PortNumber: " + portNumber + "\n" + ANSI_RESET);
// Create a new DatagramPacket for initial Packet by using data and its length.
			DatagramPacket initialPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(initialPacket);
			byte[] data1 = initialPacket.getData();
			ByteArrayInputStream inInitial = new ByteArrayInputStream(data1);
			ObjectInputStream isInitial = new ObjectInputStream(inInitial);
			InitiateTransfer initiateTransfer = (InitiateTransfer) isInitial.readObject();
			System.out.println(CYAN_BOLD + "Initial configuration Received = " + initiateTransfer.toString() + "\n" + ANSI_RESET);
// Set current time as start time and set type (GBN/SR), IP Address and port number
			startTime = System.currentTimeMillis();
			int type = initiateTransfer.getType();
			InetAddress IPAddress = initialPacket.getAddress();
			int port = initialPacket.getPort();
			initiateTransfer.setType(100);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(initiateTransfer);

/*
 Store reply to array of bytes.
 Create a new Datagram for reply packet by using reply and its length, IP Address and port number
 */
			byte[] replyByte = outputStream.toByteArray();
			DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, IPAddress, port);
			socket.send(replyPacket);

// GBN (Go Back N) Transfer : Type = 0
			if (type == 0) {
				initiateTransfer.setType(0);
				gbnTransfer(socket, initiateTransfer);
// SR (Selective Repeat) Transfer : Type = 1
			} else {
				initiateTransfer.setType(1);
				srTransfer(socket, initiateTransfer);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
// For calculating execution time, get the current time at the first and end of execution and find the difference.
		long endTime   = System.currentTimeMillis();
        	long totalTime = endTime - startTime;
        	System.out.println("Total time in seconds : "+TimeUnit.MILLISECONDS.toSeconds(totalTime));

	}

/*
Selective Repeat Transfer method
input : socket and initiate transfer
 */
	private static void srTransfer(DatagramSocket socket, InitiateTransfer initiateTransfer)
			throws IOException, ClassNotFoundException {

/*
 Create ArrayList of segment data for storing received data.
 Define a boolean variable for showing end of transfer at first and set it false.
 Define an integer value for showing the packet number that the receiver is waiting for receive it.
 Create a new array of bytes for storing incoming data.
 */
		ArrayList<SegmentData> received = new ArrayList<>();
		boolean end = false;
		int waitingFor = 0;
		byte[] incomingData = new byte[1024];

/*
 Create ArrayList of Segment data for buffering data.
 This will be used for storing unordered packet.
 */
		ArrayList<SegmentData> buffer = new ArrayList<>();

// Loop : work if the boolean parameter end is false
		while (!end) {
/*
 Create a new DatagramPacket for incoming packet by using incoming data and its length.
 Socket receives incoming packet.
 Set IP Address and port number.
 Store incoming packet as data in array of bytes.
 */
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);
			InetAddress IPAddress = incomingPacket.getAddress();
			int port = incomingPacket.getPort();
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			SegmentData segmentData = (SegmentData) is.readObject();

/*
 Generate random character and store in segment data as payload.
 Assign a hashcode for checking checksum.
 */
			char ch = segmentData.getPayLoad();
			int hashCode = ("" + ch).hashCode();
			boolean checkSum = (hashCode == segmentData.getCheckSum());

/*
 If there is no error
 Sequence number is equal to packet number which receiver is waiting for and checksum is OK.
 Then increment waiting for packet number and then add segment data to received segment data array list.
 */
			if (segmentData.getSeqNum() == waitingFor && segmentData.isLast() && checkSum) {
				waitingFor++;
				received.add(segmentData);
/*
Initialize value of sending data by using segment data, packet number that receiver is waiting for,
socket, IP Address and port number.
 */
				int value = sendData(segmentData, waitingFor, socket, IPAddress, port, false);
/*
If the return value of sendData method is less than packet number that receiver is waiting for,
it means that receiver is still waiting for that packet and packet is lost.
Also, it means that it is not the end and receiver is still waiting for packet so set boolean end false.
 */
				if (value < waitingFor) {
					waitingFor = value;
					int length = received.size();
					System.out.println("Packet " + (waitingFor) + "");
					System.out.println("Packet Lost\n");
					received.remove(length - 1);
					end = false;

// Else means that all packet and set boolean end true.
				} else {
					System.out.println(GREEN_BOLD + "Last packet received\n" + ANSI_RESET);
					end = true;
				}

			}
/*
 If Sequence number is equal to packet number which receiver is waiting for and checksum is OK
 and buffer size is more than 0 (it means than receiver did not receive one packet
 and store received next packet packets to buffer),
 increment packet number that receiver is waiting for and add segment data to received data ArrayList.
 */

			else if (segmentData.getSeqNum() == waitingFor && checkSum && buffer.size() > 0) {
				received.add(segmentData);
				waitingFor++;
/*
If the return value of sendData method is less than packet number that receiver is waiting for,
it means packet is lost in transmission and is not received by receiver.
 */
				int value = sendData(segmentData, waitingFor, socket, IPAddress, port, false);
				if (value < waitingFor) {
					waitingFor = value;
					int length = received.size();
					System.out.println(RED_BOLD + "Packet " + (waitingFor) + " lost in the Transmission\n" + ANSI_RESET);
					received.remove(length - 1);
// Create a temporary ArrayList of segment data and add all buffered data to it.
				} else {
					ArrayList<SegmentData> temp = new ArrayList<>();
					temp.addAll(buffer);
					int count = 0;

// If lost unordered packet received, deliver next packets which receive before and stored in buffer to application.

					for (int i = 0; i < temp.size(); i++) {
						if (!(waitingFor == temp.get(i).getSeqNum())) {
							break;
						} else {
							waitingFor++;
							count++;
							System.out.println(GREEN_BOLD + "Packet " + buffer.get(i).getSeqNum() + " delivered to Application From Buffer\n" + ANSI_RESET);
						}
					}
// Loop for adding segment data stored in temporary ArrayList to buffer.
					buffer = new ArrayList<>();
					for (int j = 0; j < temp.size(); j++) {
						if (j < count) {
							continue;
						}
						buffer.add(temp.get(j));
					}
// If all packet received, set boolean end true. It means that number of packets is equal number of acknowledgements.
					if (waitingFor == initiateTransfer.getNumPackets()) {
						end = true;
					}

				}

			}
/*
If segment data sequence number is equal to packet number that receiver is waiting for, check sum is Ok
and buffer is empty, add segment data to received data ArrayList and increment the packet number waiting for.
 */
			else if (segmentData.getSeqNum() == waitingFor && checkSum && buffer.size() == 0) {
				received.add(segmentData);
				waitingFor++;
				int value = sendData(segmentData, waitingFor, socket, IPAddress, port, false);
/*
If the return value of sendData method is less than the packet number waiting for,
means that a packet lost in transmission.
 */
				if (value < waitingFor) {
					waitingFor = value;
					int length = received.size();
					System.out.println(RED_BOLD + "Packet " + (waitingFor) + " lost in the Transmission\n" + ANSI_RESET);
					received.remove(length - 1);

				}

			}

/*
If segment data sequence number is more than packet number that waiting for and checksum is Ok,
store sent data to buffer.
 */
			else if (segmentData.getSeqNum() > waitingFor && checkSum) {
				sendData(segmentData, waitingFor, socket, IPAddress, port, true);
				System.out.println(YELLOW_BOLD + "Packet " + segmentData.getSeqNum() + " Stored in Buffer\n" + ANSI_RESET);
				buffer.add(segmentData);
				Collections.sort(buffer);

			}

/*
If segment data sequence number is less than packet number that waiting for and checksum is Ok,
means that packet is already delivered.
 */
			else if (segmentData.getSeqNum() < waitingFor && checkSum) {
				sendData(segmentData, waitingFor, socket, IPAddress, port, true);
				System.out.println(PURPLE_BOLD + "Packet Already Delivered Sending Duplicate Ack\n" + ANSI_RESET);
			}

/*
If checksum is not OK means that hashcode assigned to data did not match,
packet received but because of checksum error discarded.
 */
			else if (!checkSum) {
				System.out.println(YELLOW_BOLD + "Packet " + (segmentData.getSeqNum()) + " received" + ANSI_RESET);
				System.out.println(PURPLE_BOLD + "Checksum Error" + ANSI_RESET);
				System.out.println(RED_BOLD + "Packet " + segmentData.getSeqNum() + " Discarded\n" + ANSI_RESET);
				segmentData.setSeqNum(-1000);
			}

 // If the packet does not have none of the previous conditions, it is discarded.
			else {
				System.out.println(RED_BOLD + "Packet " + segmentData.getSeqNum() + " Discarded\n" + ANSI_RESET);
				segmentData.setSeqNum(-1000);
			}

		}

	}

/*
 This method is using for sending acknowledgement data according to segment data,
 packet number that receiver waiting for, socket, IP Address, port, boolean b that showing buffered data.
 */
	public static int sendData(SegmentData segmentData, int waitingFor, DatagramSocket socket, InetAddress iPAddress,
			int port, boolean b) throws IOException {
// Sending ack for received packet and add ack no. in each step.
		AckData ackData = new AckData();
		ackData.setAckNo(segmentData.getSeqNum() + 1);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		os.writeObject(ackData);

// Create new DatagramPacket for reply Packet according to reply byte, IP Address and port number.
		byte[] replyByte = outputStream.toByteArray();
		DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, iPAddress, port);

/*
 If random variable more than probability of losing packet and or buffered and segment data is not discarded,
 packet will receive and receiver send acknowledgment for that packet.
 */
		if ((Math.random() > LOST_PACK_PROBABILITY | b) && segmentData.getSeqNum() != -1000) {
			System.out.println(GREEN_BOLD + "Packet " + (ackData.getAckNo()-1) + " received" + ANSI_RESET);
			String reply = "Sending Acknowledgment for Packet :" + (ackData.getAckNo() - 1) + "";
			System.out.println(BLUE_BOLD + reply + "\n" + ANSI_RESET);
			socket.send(replyPacket);
/*
 If the packet is not discarded and not in buffer not waiting for it anymore
 so decrement number of packets receiver waiting for.
 */
		} else if (segmentData.getSeqNum() != -1000 && !b) {
			waitingFor--;
		}
		return waitingFor;
	}

// Go Back N Transfer
	private static void gbnTransfer(DatagramSocket socket, InitiateTransfer initiateTransfer) throws IOException, ClassNotFoundException {
/*
 Store received data in ArrayList of segment data and set packet number that waiting for 0
 and create array of bytes for storing incoming data.
 */
		ArrayList<SegmentData> received = new ArrayList<>();
		boolean end = false;
		int waitingFor = 0;
		byte[] incomingData = new byte[1024];

/*
 While all packets is not received create a new datagram for incoming packet according to incoming data and its length.
 Store incoming packet as data in array of bytes.
 And the print that packet received.
 */
		while (!end) {
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			SegmentData segmentData = (SegmentData) is.readObject();
			System.out.println(GREEN_BOLD+ "Packet Received  = " + segmentData.getSeqNum() + "\n" + ANSI_RESET);

// check the hashcode assigned to is not change according to segment data payload and checking checksum.
			char ch = segmentData.getPayLoad();
			int hashCode = ("" + ch).hashCode();
			boolean checkSum = (hashCode == segmentData.getCheckSum());

// If hashcode is not matched checksum error occurred.
			if (!checkSum) {
				System.out.println(RED_BOLD + "Error Occurred in the Data\n" + ANSI_RESET);
			}

/*
 If the segment data sequence number is equal to packet number that receiver is waiting for and checksum is OK,
 increment packet number that receiver is waiting for and add received data to segment data.
 Also, it means that last packet received. (return value of isLast packet true)
 */
			if (segmentData.getSeqNum() == waitingFor && segmentData.isLast() && checkSum) {
				waitingFor++;
				received.add(segmentData);
				System.out.println(GREEN_BOLD + "Last packet received\n" + ANSI_RESET);
				end = true;

/*
 If the segment data sequence number is equal to packet number that receiver is waiting for and checksum is OK,
 store packet and increment packet number that receiver is waiting for.
 */
			} else if (segmentData.getSeqNum() == waitingFor && checkSum) {
				waitingFor++;
				received.add(segmentData);
			}

// If hashcode of data is not matched, we have checksum error.
			else if (!checkSum) {
				System.out.println(PURPLE_BOLD + "Checksum Error\n"+ ANSI_RESET);
				segmentData.setSeqNum(-1000);
			}

// If we don't have none of previous conditions' packet is discarded because it is not in order.
			else {
				System.out.println(YELLOW_BOLD + "Packet discarded (not in order)\n" + ANSI_RESET);
				segmentData.setSeqNum(-1000);
			}

// Set IP Address and port number of incoming packet.
			InetAddress IPAddress = incomingPacket.getAddress();
			int port = incomingPacket.getPort();

// Define LogicalSimulation.AckData and packet number which receiver is waiting for it to that.
			AckData ackData = new AckData();
			ackData.setAckNo(waitingFor);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(ackData);

// Define DatagramPacket for reply packet according to reply bytes, IP Address and port number.
			byte[] replyByte = outputStream.toByteArray();
			DatagramPacket replyPacket = new DatagramPacket(replyByte, replyByte.length, IPAddress, port);

/*
 If random variable is more than probability of losing packet and segment data is not discarded,
 it means that packet received without any error so sending acknowledgement for receiving next packet.
 */
			if (Math.random() > LOST_PACK_PROBABILITY && segmentData.getSeqNum() != -1000) {
				String reply = "Sending Acknowledgment Number :" + ackData.getAckNo() + "\n";
				System.out.println(CYAN_BOLD + reply + "\n" + ANSI_RESET);
				socket.send(replyPacket);
/*
 If segment data not discarded and does not satisfy last condition, it means that packet lost.
 So decrement packet number waiting for and set end of transferring false.
 */
			} else if (segmentData.getSeqNum() != -1000) {
				int length = received.size();
				System.out.println(RED_BOLD + "Packet Lost\n" + ANSI_RESET);
				received.remove(length - 1);
				waitingFor--;
				if (end) {
					end = false;
				}

			}

		}

	}

}
