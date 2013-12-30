//***************** PolpeScratch, Scratch to control Arduino  *************
//modified by Claudio Becchetti  on original a4s ideas from (see below)
//
// 1.1. 5-12-2013   now compile properly with firmata.java
// 1.2: 8-12-2013   pwm implemented
// 1.3  12-12-2013
// 1.4	23-12-2013	added at function at point 1 below 
// 					at startup if the com is not available the program continues to try to open the port
// 1.5  30-12-2013  fixed bug for digital-analog input not updated now works
//					remember that you should configure the pin as input in order to get data
//					added a new scratch example to test analog digital input output

// *******************************************
//          Functions to be implemented
//
// 1) at startup if the com is not available the program continues to try to open the port
// 2) if the port closed the program goes in standby and wait for the port to be open
// 3) if port is not indicated the program starts scanning available ports for firmata answer
// 4) gui start up

// ********************************************** 


// A4S.java
// Copyright (c) MIT Media Laboratory, 2013
//
// Helper app that runs an HTTP server allowing Scratch to communicate with
// Arduino boards running the Firmata firmware (StandardFirmata example).
//
// Note: the Scratch extension mechanism is a work-in-progress and still
// evolving. This code will need updates to work with future version of Scratch.
//
// Based on HTTPExtensionExample by John Maloney. Adapted for Arduino and
// Firmata by David Mellis.
//
// Inspired by Tom Lauwers Finch/Hummingbird server and Conner Hudson's Snap extensions.


import java.io.*;
import java.net.*;
import java.util.*;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import org.firmata.Firmata;

public class A4S {
	private static boolean DEBUG_ACTIVE = true;
	private	static int num_of_poll_reponse =0;
	
	private	static int refresh_rate_30msec =60;// after 1800 millisec arduino. init
	private static int REPORT_DIGITAL         = 0xD0; // enable digital input by port

	private static final int PORT = 12345; // set to your extension's port number
	private static int volume = 8; // replace with your extension's data, if any

	private static InputStream sockIn;
	private static OutputStream sockOut;

	private static SerialPort serialPort;
	private static Firmata arduino;
	
	private static SerialReader reader;
	
	public static class SerialReader implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent e) {
			try {
				while (serialPort.getInputStream().available() > 0) arduino.processInput(serialPort.getInputStream().read());
			} catch (IOException  err) {
				System.err.println(err.getStackTrace()[0].getLineNumber() + ":" + err);
			}
		}
	}
	
public static class  MyWriter implements Firmata.Writer {
	public void write(int val) {
		try {
		serialPort.getOutputStream().write(val);
		} catch (IOException  err) {
				System.err.println(err.getStackTrace()[0].getLineNumber() + ":" + err);
			}
		}
	}	
	public static MyWriter writer;
	
	public static void main(String[] args) throws IOException {
	
		System.out.println("\n\r\n\r************ PolpeScratch ***************");
		System.out.println("Scratch to control Arduino V. 1.5.1  30-12-2013 ");
		
		CommPortIdentifier portIdentifier;
		CommPort commPort;
		int i=0;
		
		//1) check arguments
		try {
			if (args.length < 1) {
				System.err.println("Please specify serial port on command line.");
				return;
			}
		
		} catch (Exception e) {
			System.err.println("port not found in main arguments");
			System.err.println(e.getStackTrace()[0].getLineNumber() + ":" + e);
			return;
		}
		
		//2) find identifier 
		while (true)
		{

		try {
			i++;
			Thread.sleep(1000);
			portIdentifier = CommPortIdentifier.getPortIdentifier(args[0]);
			
			
			break;
			} catch (Exception e) {
			
			System.err.println("problems on finding port " + args[0]);
			System.err.println(i+" "+ e);
			Enumeration portList = CommPortIdentifier.getPortIdentifiers();	 
			}
		}
		
		//3)  open port
		try {
			
			commPort = portIdentifier.open("A4S",2000);
			
			} catch (Exception e) {
			System.err.println("problems on opening port " + args[0]);
			System.err.println(e);
			return;
		}
		
		try {

			if ( commPort instanceof SerialPort )
			{
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

				//arduino = new Firmata(serialPort.getInputStream(), serialPort.getOutputStream());
				writer = new MyWriter();
				
				arduino = new Firmata(writer);
				reader = new SerialReader();
				
				serialPort.addEventListener(reader);
				serialPort.notifyOnDataAvailable(true);
			}
			else
			{
				System.out.println("Error: Only serial ports are handled by this example.");
				return;
			}
			
			
		
		} catch (Exception e) {
			System.err.println("problems on opened port " + args[0] );
			System.err.println(e);
			return;
		}
		System.out.println("\n\r\n\r************ PolpeScratch ***************");
		System.out.println("\n\rArduino port " + args[0]+ " working\n\r" );
		
		InetAddress addr = InetAddress.getLocalHost();
		System.out.println("PolpeScratch http server for Scratch started on " + addr.toString());
		
		ServerSocket serverSock = new ServerSocket(PORT);
		
		
		
		while (true) {
			Socket sock = serverSock.accept();
			sockIn = sock.getInputStream();
			sockOut = sock.getOutputStream();
			try {
				handleRequest();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				sendResponse("unknown server error");
			}
			sock.close();
		}
		
	}

	private static void handleRequest() throws IOException {
		String httpBuf = "";
		int i;

		// read data until the first HTTP header line is complete (i.e. a '\n' is seen)
		while ((i = httpBuf.indexOf('\n')) < 0) {
			byte[] buf = new byte[5000];
			int bytes_read = sockIn.read(buf, 0, buf.length);
			if (bytes_read < 0) {
				System.out.println("Socket closed; no HTTP header.");
				return;
			}
			httpBuf += new String(Arrays.copyOf(buf, bytes_read));
		}
		
		String header = httpBuf.substring(0, i);
		if (header.indexOf("GET ") != 0) {
			System.out.println("This server only handles HTTP GET requests.");
			return;
		}
		i = header.indexOf("HTTP/1");
		if (i < 0) {
			System.out.println("Bad HTTP GET header.");
			return;
		}
		header = header.substring(5, i - 1);
		if (header.equals("favicon.ico")) return; // igore browser favicon.ico requests
		else if (header.equals("crossdomain.xml")) sendPolicyFile();
		else if (header.length() == 0) doHelp();
		else doCommand(header);
	}

	private static void sendPolicyFile() {
		// Send a Flash null-teriminated cross-domain policy file.
		String policyFile =
			"<cross-domain-policy>\n" +
			"  <allow-access-from domain=\"*\" to-ports=\"" + PORT + "\"/>\n" +
			"</cross-domain-policy>\n\0";
		sendResponse(policyFile);
	}

	private static void sendResponse(String s) {
		String crlf = "\r\n";
		String httpResponse = "HTTP/1.1 200 OK" + crlf;
		httpResponse += "Content-Type: text/html; charset=ISO-8859-1" + crlf;
		httpResponse += "Access-Control-Allow-Origin: *" + crlf;
		httpResponse += crlf;
		httpResponse += s + crlf;
		try {
			byte[] outBuf = httpResponse.getBytes();
			sockOut.write(outBuf, 0, outBuf.length);
		} catch (Exception ignored) { }
	}
	
	private static void doCommand(String cmdAndArgs) {
		// Essential: handle commands understood by this server
		String response = "okay";
		String[] parts = cmdAndArgs.split("/");
		String cmd = parts[0];

		
		if (DEBUG_ACTIVE)
			if (cmd.equals("poll")==false)
				System.out.print(cmdAndArgs);
				
		
		//try {
			/* old commands to be removed
			if (cmd.equals("pinOutput")) {
				arduino.pinMode(Integer.parseInt(parts[1]), Firmata.OUTPUT);
			} else if (cmd.equals("pinInput")) {
				arduino.pinMode(Integer.parseInt(parts[1]), Firmata.INPUT);
								
			} else if (cmd.equals("pinPwm")) {// added pwm
				arduino.pinMode(Integer.parseInt(parts[1]), Firmata.PWM);
			
			} else if (cmd.equals("pinHigh")) {
				arduino.digitalWrite(Integer.parseInt(parts[1]), Firmata.HIGH);
			} else if (cmd.equals("pinLow")) {
				arduino.digitalWrite(Integer.parseInt(parts[1]), Firmata.LOW);
			} else 
			*/
			if (cmd.equals("pinMode")) {
				if ("input".equals(parts[2])) // added pwm
					{
					arduino.pinMode(Integer.parseInt(parts[1]),  Firmata.INPUT ); 
					
					//set report active without this digital input is not updated
					if (DEBUG_ACTIVE )
							System.out.println("sent digital report");
					/*
					for (int i = 0; i < 16; i++) {
							serialPort.getOutputStream().write(REPORT_DIGITAL | i);
							serialPort.getOutputStream().write(1);
							}
					*/	
					arduino.init();
					}
					else
				if ("output".equals(parts[2]))
					arduino.pinMode(Integer.parseInt(parts[1]), Firmata.OUTPUT); else
				if ("pwm".equals(parts[2])) // added pwm
					{
					arduino.pinMode(Integer.parseInt(parts[1]), Firmata.PWM);	
					//System.out.println("pwm requested \n");					
					}
				//arduino.pinMode(Integer.parseInt(parts[1]), "input".equals(parts[2]) ? Firmata.INPUT : Firmata.OUTPUT); //replaced
			} else if (cmd.equals("digitalWrite")) {
				arduino.digitalWrite(Integer.parseInt(parts[1]), "high".equals(parts[2]) ? Firmata.HIGH : Firmata.LOW);
			} else if (cmd.equals("analogWrite")) {// added pwm  
				arduino.analogWrite(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) );
				
				
			} else if (cmd.equals("poll")) {
				// set response to a collection of sensor, value pairs, one pair per line
				// in this example there is only one sensor, "volume"
				//response = "volume " + volume + "\n";
				response = "";
				for (int i = 2; i <= 13; i++) {
					response += "digitalRead/" + i + " " + (arduino.digitalRead(i) == Firmata.HIGH ? "true" : "false") + "\n";
				}
				for (int i = 0; i <= 5; i++) {
					response += "analogRead/" + i + " " + (arduino.analogRead(i)) + "\n";
				
				}
				refresh_rate_30msec--;
				if (refresh_rate_30msec==0)
					{
					refresh_rate_30msec = 200;
					
					
					if (arduino.refresh_arrived == 0)
						{
						if (DEBUG_ACTIVE )
							System.out.println("no updates:"+ arduino.refresh_arrived + " refresh requested");
						arduino.init(); // every some time restart
						
						}
						else arduino.refresh_arrived =0;
					}
					
				if (DEBUG_ACTIVE )
					{
					num_of_poll_reponse++;
					if (num_of_poll_reponse == 120)
						{
						num_of_poll_reponse=0;
						System.out.println(" " + response +"refresh analog = "+ arduino.refresh_arrived +" ");
					
						}
					}
			} else {
				response = "unknown command: " + cmd;
			}
			
				
			sendResponse(response);
		//} catch (IOException e) {
		//	System.err.println(e);		 }
	}

	private static void doHelp() {
		// Optional: return a list of commands understood by this server
		String help = "HTTP Extension Example Server<br><br>";
		sendResponse(help);
	}

}
