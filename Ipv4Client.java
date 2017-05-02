import java.util.*;
import java.io.*;
import java.net.*;

public class Ipv4Client {

	public static void main(String[] args) throws Exception{
		Socket socket = new Socket("codebank.xyz",38003);
		try{
			
			//Read in Stream
			InputStream is = socket.getInputStream();
			//Write to Stream
			OutputStream out = socket.getOutputStream();
			//Structure of the Ipv4 packet in a Byte array
			byte[] packet;

			for (int i = 1;i < 13 ;i++ ) {
				//Find the Size of The packet Byte Array 
				int size = (int)Math.pow(2,i);
				short length = (short)(20 + size);
				packet = new byte[length];
				
				//Assigning Random Data
				new Random().nextBytes(packet);
				//Start of the packet 
				
				//version 4 and HLen 5
				packet[0] = 0b01000101; 
				//TOS
				packet[1] = 0; 
				//first byte of length
				packet[2] = impConversion(2,length); 
				//second byte of length
				packet[3] = impConversion(3, length);
				//first byte of Identity
				packet[4] = 0; 
				//second byte of Ident
				packet[5] = 0; 
				//flags and offset
				packet[6] = (byte)0x40; 
				//offset cont'd
				packet[7] = 0; 
				//TTL
				packet[8] = 50; 
				//protocol
				packet[9] = 6; 
				//assume checksum 0 first
				packet[10] = 0; 
				//assume checksum 0 first
				packet[11] = 0; 

				//Add all Zeros to source
				packet = sourceToZero(packet);

				//set destination
				packet = setDestination(packet, socket);

				//computing the sum
				short check = checksum(packet, size);

				//CheckSum Byte 1 & 2
				packet[10] = impConversion(10, check);
				packet[11] = impConversion(11, check);

				//System.out.println(Arrays.toString(packet));

				//Send Packet 
				out.write(packet);

				//Response
				checkResponse(is,size);

			}

		} catch (Exception e){}

	}	

	private static byte impConversion(int n, short x){
		if (n == 2 || n == 10)
			return (byte) ((x & 0xFF00)>>>8);
		else 
			return (byte) (x & 0x00FF);
	}

	private static byte[] sourceToZero(byte[] byteArray){
		for (int i = 12; i < 16 ;i++ ) {
			byteArray[i] = 0;
		}
		return byteArray;
	}

	private static byte[] setDestination(byte[] byteArray, Socket socket){
		byte[] destArray = socket.getInetAddress().getAddress();
		
		for(int i = 0; i < 4; i++){
			byteArray[i+16] = destArray[i];
		}
		return byteArray;
	}

	public static short checksum(byte[] b, int size){
		long sum = 0;
		int length = b.length - size;
		int i = 0;
		long highVal;
		long lowVal;
		long value;
		
		while(length > 1){
			//gets the two halves of the whole byte and adds to the sum
			highVal = ((b[i] << 8) & 0xFF00); 
			lowVal = ((b[i + 1]) & 0x00FF);
			value = highVal | lowVal;
		    sum += value;
		    
		    //check for the overflow
		    if ((sum & 0xFFFF0000) > 0) {
		        sum = sum & 0xFFFF;
		        sum += 1;
		      }

		      //iterates
		      i += 2;
		      length -= 2;
		}
		//leftover bits
		if(length > 0){
			sum += (b[i] << 8 & 0xFF00);
		      if ((sum & 0xFFFF0000) > 0) {
		        sum = sum & 0xFFFF;
		        sum += 1;
		      }
		}
		
		sum = ~sum;
		sum = sum & 0xFFFF;
		return (short)sum;
	}


	private	static void checkResponse(InputStream is, int size) throws Exception{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String response = br.readLine();

			System.out.println("Packet Size: " + size);
			System.out.println(response);

			if(response.equals("bad"))
				System.exit(0);
			

		}catch (Exception e){}
	}
}