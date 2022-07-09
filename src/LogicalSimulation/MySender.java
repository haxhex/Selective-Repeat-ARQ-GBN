package LogicalSimulation;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MySender {

// font colors
	public static final String RED_BOLD = "\033[1;31m";    // RED
	public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
	public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
	public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
	public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
	public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
	public static final String ANSI_RESET = "\u001B[0m";
/*
Initializing
Timer is set equal 50.
Probability of losing packet is set equal 0.05.
Probability of bit error is set equal 0.1.
A total number of retransmission data equal 0 at first.
 */
	public static int TIMER = 50;
	public static final double LOST_ACK_PROBABILITY = 0.05;
	public static final double BIT_ERROR_PROBABILITY = 0.1;
	public static double totalResent = 0;

	public static void main(String[] args) {

// Read port number and number of packet from file.
		BufferedReader br = null;
		String fileName = "";
		int portNumber = 0;
		int numPackets = 0;


//Initialize type of protocol, sequenceNumBits, size of window, timeout, size of segment.
		String type = "";
		int sequenceNumBits = 0;
		int windowSize = 0;
		long timeOut = 0;
		long sizeSegment = 0;

/*
Get the parameters
first : filename -> InitialConfig.txt
{type: GBN/SR, Seq bits, Window Size, timeout, Segment Size}
second : port number
third : number of packets
 */
		if (args.length == 3) {
			fileName = args[0];
			portNumber = Integer.parseInt(args[1]);
			numPackets = Integer.parseInt(args[2]);

		} else {
// If the number of input arguments is not correct, this message is printed.
			System.out.println(RED_BOLD + "Invalid Parameters argv[0] - FileName (File containing configurations), argv[1] - PortNumber, argv[2] - NumberOfPackets\n" + ANSI_RESET);
		}

// Reading Sequence number of bits, size of the window, timeout and size of the segment from the file.
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			int i = 0;
			while (line != null) {
				if (i == 0) {
					type = line.trim();
				} else if (i == 1) {
					sequenceNumBits = Integer.parseInt(line.charAt(0) + "");
					windowSize = Integer.parseInt(line.charAt(2) + "");
				} else if (i == 2) {
					timeOut = Long.parseLong(line);
				} else if (i == 3) {
					sizeSegment = Long.parseLong(line);

				}
				i++;
				line = br.readLine();
			}

			br.close();
		} catch (Exception e) {
// Printing this message if an error occurred in reading a file.
			System.out.println(PURPLE_BOLD + "Error occurred while reading file\n" + ANSI_RESET);
		}

/*
Printing values obtained from the file.
{type : GNB/SR, Number of Sequence bits, Window Size, Timeout, Segment Size}
 */
		System.out.println(CYAN_BOLD + "Type: " + type + " Number of Seq bits: " + sequenceNumBits + " Window Size " + windowSize
				+ " Timeout: " + timeOut + " Segment Size: " + sizeSegment + "\n" + ANSI_RESET);

// Set timer equal timeout.
		TIMER = (int) timeOut;

/*
Sending Data Function
Sending data according to port number, type (GNB/SR), Sequence number of bits, window size, timeout and segment size.
 */
		try {
			sendData(portNumber, numPackets, type, sequenceNumBits, windowSize, timeOut, sizeSegment);
		} catch (Exception e) {

			e.printStackTrace();
		}
// Calculating Loss ratio
		System.out.println("Miss ratio - " + (totalResent/(totalResent+numPackets)));

	}

	private static void sendData(int portNumber, int numPackets, String type, int sequenceNumBits, int windowSize,
			long timeOut, long sizeSegment) throws IOException, ClassNotFoundException, InterruptedException {

		ArrayList<SegmentData> sent = new ArrayList<>();

// Last Packet sent
		int lastSent = 0;

// Sequence number of the last Acknowledged packet
		int waitingForAck = 0;

		String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int N = alphabet.length();

		DatagramSocket Socket = null;

// Type {Go Back N = 0, Selective Repeat = 1}
// Go Back N
		if (type.equalsIgnoreCase("gbn")) {

// Set type = 0 (Go Back N), number of packets, segment size
			byte[] incomingData = new byte[1024];
			InitiateTransfer initiateTransfer = new InitiateTransfer();
			initiateTransfer.setType(0);
			initiateTransfer.setNumPackets(numPackets);
			initiateTransfer.setPacketSize(sizeSegment);
			initiateTransfer.setWindowSize(1);

// Define a new Socket
			Socket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(initiateTransfer);
			byte[] data1 = outputStream.toByteArray();

// Sending initial configuration to receiver data, IP Address and port number
			DatagramPacket initialPacket = new DatagramPacket(data1, data1.length, IPAddress, portNumber);
			System.out.println(CYAN_BOLD + "Sending initial configuration to receiver" + "\n" + ANSI_RESET);
			Socket.send(initialPacket);

// DatagramPacket for initial acknowledgment
			DatagramPacket initialAck = new DatagramPacket(incomingData, incomingData.length);
			Socket.receive(initialAck);
			byte[] dataImp = initialAck.getData();
			ByteArrayInputStream inReturn = new ByteArrayInputStream(dataImp);
			ObjectInputStream isReturn = new ObjectInputStream(inReturn);
			InitiateTransfer initiateTransfer2 = (InitiateTransfer) isReturn.readObject();

//Type 100 - initial transfer, Type 0 - GBN, Type 1 - SR
			if (initiateTransfer2.getType() == 100) {

				while (true) {
/*
Keep on sending the packets until no. of items which are sent and acknowledgment not received
is less than window size or last sent packet is equal to maximum packet size.
 */
					while (lastSent - waitingForAck < windowSize && lastSent < numPackets) {
						if (lastSent == 0 && waitingForAck == 0) {
							System.out.println(YELLOW_BOLD + "-Timer Started for Packet:  " + 0 + "\n" + ANSI_RESET);
						}
/*
Generate random character to send.
Payload : The actual information or message in transmitted data.
Set segment Payload, Sequence Number and Check Sum by hashcode
*/
						Random r = new Random();
						char ch = alphabet.charAt(r.nextInt(N));
						int hashCode = ("" + ch).hashCode();
						SegmentData segmentData = new SegmentData();
						segmentData.setPayLoad(ch);
						segmentData.setSeqNum(lastSent);
						segmentData.setCheckSum(hashCode);

// If the value of last send data is equal to [number of packets] - 1 last data of segment has been sent.
						if (lastSent == numPackets - 1) {
							segmentData.setLast(true);
						}

// According to bit error probability randomly set data payload.
						if (Math.random() <= BIT_ERROR_PROBABILITY) {

							segmentData.setPayLoad(alphabet.charAt(r.nextInt(N)));

						}
						outputStream = new ByteArrayOutputStream();
						os = new ObjectOutputStream(outputStream);
						os.writeObject(segmentData);
						byte[] data = outputStream.toByteArray();

/*
 DatagramPacket for sending packet by using data, IP Address and port number.
 Increment number of sent package.
 */
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
						System.out.println(GREEN_BOLD + "Sending Packet : " + segmentData.getSeqNum() + "\n" + ANSI_RESET);
						sent.add(segmentData);
						Socket.send(sendPacket);
						lastSent++;
						Thread.sleep(50);

					}
/*
DatagramPacket for incoming packet by using data.
Set timout for incoming packet.
Generate ack for incoming data.
 */
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
					try {
						Socket.setSoTimeout(TIMER);
						Socket.receive(incomingPacket);
						byte[] data = incomingPacket.getData();
						ByteArrayInputStream in = new ByteArrayInputStream(data);
						ObjectInputStream is = new ObjectInputStream(in);
						AckData ackData = (AckData) is.readObject();

/*
Randomly if random variable more than probability of losing acknowledgement, acknowledgement receiving by sender (server).
After receiving an acknowledgement sender waiting for next acknowledgement (for next sent data or for acknowledgement not received).
 */
						if (Math.random() > LOST_ACK_PROBABILITY) {
							System.out.println(GREEN_BOLD + "Received ACK for :" + (ackData.getAckNo() - 1) + "\n" + ANSI_RESET);
							waitingForAck = Math.max(waitingForAck, ackData.getAckNo());

// If sender waiting for acknowledgement which is not equal to number of packet, timer start for that acknowledgement.
							if (!(waitingForAck == numPackets)) {
								System.out.println(YELLOW_BOLD + "Timer Started for Packet:  " + ackData.getAckNo() + "\n" + ANSI_RESET);
							}
// Else acknowledgement lost for that packet
						} else {
							System.out.println(RED_BOLD + "Acknowledgment Lost for :" + (ackData.getAckNo() - 1) + "\n" + ANSI_RESET);
						}
// If acknowledgement number equal to number of packet it means that all acknowledgement received
						if (ackData.getAckNo() == numPackets) {
							break;
						}
					} catch (SocketTimeoutException e) {

/*
 If acknowledgement does not receive after defined time, timeout occurred for packet.
 Creates the task to resend all packets that have been previously
 sent but have not yet been acknowledged if a timeout occurs, which
 is attached to the new timer
 */
						System.out.println(RED_BOLD + "Timeout Occurred for Packet " + waitingForAck + "\n" + ANSI_RESET);

// Loop start from acknowledgement which sender waiting for to last sent packet.
						for (int i = waitingForAck; i < lastSent; i++) {

// Hashcode for checking checksum
							SegmentData segmentData = sent.get(i);
							char ch = segmentData.getPayLoad();
							int hashCode = ("" + ch).hashCode();
							segmentData.setCheckSum(hashCode);

// If random variable less than defined bit error probability, set payload and put data in the segment correctly.
							if (Math.random() <= BIT_ERROR_PROBABILITY) {
								Random r = new Random();
								segmentData.setPayLoad(alphabet.charAt(r.nextInt(N)));

							}
							outputStream = new ByteArrayOutputStream();
							os = new ObjectOutputStream(outputStream);
							os.writeObject(segmentData);
							byte[] data = outputStream.toByteArray();

// Datagram for sending lost packet again
							DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
							System.out.println(CYAN_BOLD + "Resending Packet :" + segmentData.getSeqNum() + "\n" + ANSI_RESET);
							Socket.send(sendPacket);
							totalResent++;
							Thread.sleep(50);

						}

					}

				}
			}

		}
/*
 Type : Selective Repeat (SR) -> 1
 Hashset for unordered packet.
 Define array of bytes for incoming data.
 Initiate Transfer:
 Set type = 1, number of packets, segment size and window size
 */
		else if (type.equalsIgnoreCase("sr")) {

			HashSet<Integer> unOrdered = new HashSet<>();
			byte[] incomingData = new byte[1024];
			InitiateTransfer initiateTransfer = new InitiateTransfer();
			initiateTransfer.setType(1);
			initiateTransfer.setNumPackets(numPackets);
			initiateTransfer.setPacketSize(sizeSegment);
			initiateTransfer.setWindowSize(windowSize);

// Create new Datagram and set IP Address "localhost".
			Socket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(initiateTransfer);
			byte[] data1 = outputStream.toByteArray();

// Datagram for initial packet by using data, IP Address and port number
			DatagramPacket initialPacket = new DatagramPacket(data1, data1.length, IPAddress, portNumber);
			System.out.println(CYAN_BOLD + "Sending Initial Data" + "\n" + ANSI_RESET);
			Socket.send(initialPacket);

// Datagram for initial acknowledgement by using data, IP Address and port number
			DatagramPacket initialAck = new DatagramPacket(incomingData, incomingData.length);
			Socket.receive(initialAck);
			byte[] dataImp = initialAck.getData();
			ByteArrayInputStream inReturn = new ByteArrayInputStream(dataImp);
			ObjectInputStream isReturn = new ObjectInputStream(inReturn);
			InitiateTransfer initiateTransfer2 = (InitiateTransfer) isReturn.readObject();
//Type 100 - initial transfer, Type 0 - GBN, Type 1 - SR
			if (initiateTransfer2.getType() == 100) {

				while (true) {
/*
Keep on sending the packets until no. of items which are sent and acknowledgment not received
is less than window size or last sent packet is equal to maximum packet size.
 */
					while (lastSent - waitingForAck < windowSize && lastSent < numPackets) {
// If last sent packet number equal to acknowledgement which sender is waiting for, timer start for that packet.
						if(lastSent-waitingForAck==0) {
							System.out.println(YELLOW_BOLD + "Timer Started for Packet: " + lastSent + "\n" + ANSI_RESET);
						}
// Else means that timer is already running for that packet.
						else {
							System.out.println(YELLOW_BOLD + "Timer Already Running\n" + ANSI_RESET);
						}
						
/*
Generate random data for sending.
Assign hashcode to our data for checking checksum.
payload : put data to segment
Set sequence number last sent data.
 */
						Random r = new Random();
						char ch = alphabet.charAt(r.nextInt(N));
						int hashCode = ("" + ch).hashCode();
						SegmentData segmentData = new SegmentData();
						segmentData.setPayLoad(ch);
						segmentData.setSeqNum(lastSent);
						segmentData.setCheckSum(hashCode);
						if (lastSent == numPackets - 1) {
							segmentData.setLast(true);
						}

// If random variable less than probability of bit error, put data to the segment correctly.
						if (Math.random() <= BIT_ERROR_PROBABILITY) {
							segmentData.setPayLoad(alphabet.charAt(r.nextInt(N)));
						}
						outputStream = new ByteArrayOutputStream();
						os = new ObjectOutputStream(outputStream);
						os.writeObject(segmentData);
						byte[] data = outputStream.toByteArray();

/*
 Define DatagramPacket for sending packet by using data, IP Address and port number.
 If packet send, add it to sent packet ArrayList and increment last sent packet number.
 */
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, portNumber);
						System.out.println(BLUE_BOLD + "Sending Packet : " + segmentData.getSeqNum() + "\n" + ANSI_RESET);
						sent.add(segmentData);
						Socket.send(sendPacket);
						lastSent++;
						Thread.sleep(50);
					}
/*
 Define a new datagram for incoming packet by using incoming data and its length.
 Set socket timout our defined timer value.
 Define array of bytes of data for storing incoming packet.
 And define LogicalSimulation.AckData for storing acknowledgement data.
 */
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
					try {
						Socket.setSoTimeout(TIMER);
						Socket.receive(incomingPacket);
						byte[] data = incomingPacket.getData();
						ByteArrayInputStream in = new ByteArrayInputStream(data);
						ObjectInputStream is = new ObjectInputStream(in);
						AckData ackData = (AckData) is.readObject();

/*
If random variable more than probability of losing acknowledgement, sender will receive acknowledgement  from Receiver.
If ack receive from receiver, sender will wait for the next data acknowledgement (increment waiting for ack).
If sender receive unordered acknowledgement, waiting for next ack according to last sent (increment waiting for ack)
Then timer starts for receiving next acknowledgement.
 */
						if (Math.random() > LOST_ACK_PROBABILITY) {
							System.out.println(GREEN_BOLD + "Received ACK for :" + (ackData.getAckNo() - 1) + "\n" + ANSI_RESET);
							if ((ackData.getAckNo() - waitingForAck) == 1) {
								waitingForAck = waitingForAck + 1;
								if (unOrdered.size() > 0) {
									for (int i = waitingForAck; i <= lastSent; i++) {
										if (unOrdered.contains(i)) {
											unOrdered.remove(i);
											waitingForAck++;
										} else {
											break;
										}

									}
								}
								System.out.println(YELLOW_BOLD + "Timer Started for Packet:  " + waitingForAck + "\n" + ANSI_RESET);
// If sender did not receive previous ack It's still waiting for receiving ack so timer is already running for it.
							} else {
								System.out.println(YELLOW_BOLD + "Timer already Running for   " + waitingForAck + "\n" + ANSI_RESET);
								unOrdered.add((ackData.getAckNo() - 1));
							}

// If the time is over and the ack doesn't receive it means that acknowledgment is lost.
						} else {
							System.out.println(PURPLE_BOLD + "Acknowledgment Lost for :" + (ackData.getAckNo() - 1)+ "\n" + ANSI_RESET);
						}
/*
 If the last received is equal to the last packet number and our unordered list (use for unordered ack) is empty
 it means that all acknowledgment received so break.
 */
						if (waitingForAck == numPackets && unOrdered.size() == 0) {
							break;
						}
					} catch (SocketTimeoutException e) {

// If the defined time is over, timeout occurred.
						System.out.println(RED_BOLD + "Timeout Occurred\n" + ANSI_RESET);

// If our unordered list is empty, generate new random data and assign it hashcode for checking checksum.
						for (int i = waitingForAck; i < lastSent; i++) {
							SegmentData segmentData = sent.get(i);
							if (!(unOrdered.contains(segmentData.getSeqNum()))) {
								char ch = segmentData.getPayLoad();
								int hashCode = ("" + ch).hashCode();
								segmentData.setCheckSum(hashCode);

// If random variable less than probability of bit error, generate random data and set payload to segment.
								if (Math.random() <= BIT_ERROR_PROBABILITY) {
									Random r = new Random();
									segmentData.setPayLoad(alphabet.charAt(r.nextInt(N)));

								}
								outputStream = new ByteArrayOutputStream();
								os = new ObjectOutputStream(outputStream);
								os.writeObject(segmentData);
								byte[] data = outputStream.toByteArray();

// Define new Datagram for resending lost packet by using data and its length, IP Address and port.
								DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress,
										portNumber);
								System.out.println(CYAN_BOLD + "Resending Packet :" + segmentData.getSeqNum() + "\n" + ANSI_RESET);
								Socket.send(sendPacket);
								totalResent++;
								Thread.sleep(50);

							}

						}

					}

				}
			}

		}

	}

}
