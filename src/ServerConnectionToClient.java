import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class ServerConnectionToClient implements Runnable{
	private ServerClient parent;
	private Socket clientSocket;
	private BufferedReader reader;
	private PrintWriter writer;
	private OutputStream output;
	private int clientType = UNDEFINED;
	private static final int UNDEFINED = 0;
	private static final int JAVA = 1;
	private static final int WEBSOCKET = 2;
	
	boolean handShakeDone = false;
	
	private List<String> inputBuffer = new ArrayList<String>();
	
	private boolean StreamCloseOK = false;
	
	public ServerConnectionToClient(Socket s, ServerClient parent) throws IOException
	{
		this.parent = parent;
		this.clientSocket = s;
		output = this.clientSocket.getOutputStream();
		writer = new PrintWriter(output);
		
		InputStreamReader streamReader = new InputStreamReader(this.clientSocket.getInputStream());
		this.reader = new BufferedReader(streamReader);
		StreamCloseOK = false;
	}
	
	@Override
	public void run()
	{
		try {
			//Normal reading
			handShakeDone = false;
			String message;
			while(clientOKLineReading() && (message = reader.readLine()) != null) {
				inputBuffer.add(message);
				
				if(message.equals("") && !handShakeDone) {
					openingHandshake();
					parent.sendNewPWCode();
					handShakeDone = true;
					inputBuffer.clear();
				}
			}
			
			if(clientOKLineReading() && !StreamCloseOK) {
				parent.clientDisconnects();
				CommonUsedFeatures.showHint("Stream Closed");
			}
			else {
				CommonUsedFeatures.showHint("No readLine support");
				
				//reading for special clients (Websockets)
				if(clientType == WEBSOCKET)
					websocketReading();
				
				else {
					CommonUsedFeatures.showHint("Wrong client Type");
				}
				
				if(!StreamCloseOK ) {
					parent.clientDisconnects();
					CommonUsedFeatures.showHint("Stream Closed");
				}
			}
		}
		catch(Exception e) {
			if(StreamCloseOK && e.getMessage().equals("socket closed")) return;
			e.printStackTrace();
		}
	}
	
	private void websocketReading() throws IOException
	{
		//Up to now Websockets do not support encryption
		parent.setClientPasswordVersion(-1);
		parent.setClientVersion(-1);
		
		InputStream fromData = this.clientSocket.getInputStream();
		int read;
		List<Byte> buffer = new ArrayList<Byte>();
		
		while((read = fromData.read()) != -1) {
			buffer.add((byte) read);
//			CommonUsedFeatures.showHint(buffer.size() + ": " + CommonUsedFeatures.intToHex(((int) read) & 0xFF));
			
			try {
				String data = convertWebsocket(buffer);
				
//				CommonUsedFeatures.showHint("data: " + data);
				this.inputBuffer.add(data);
				buffer.clear();
			}
			catch(ToLessInformationException e) {
			}
			catch(Exception e) {
				buffer.clear();
				CommonUsedFeatures.showErr(e);
			}
			//System.out.println(CommonUsedFeatures.intToHex(read));
		}
	}

	private String convertWebsocket(List<Byte> buffer) throws IOException {
		int headerLenght = 2;
		IOException toTrow = null;
		
		//Look if the header is fully here
		if(buffer.size() < headerLenght)
			throw new ToLessInformationException("Header not here");
		
		//Look if it is the last Fragment
		boolean fin = (buffer.get(0) & 0x80) > 0;
		//TODO Support of multiple Frames
		if(!fin)
			if(toTrow == null)
				toTrow = new IOException("NOT Supported jet");
		
		//Get the type of Data
		byte dataType = (byte) (buffer.get(0) & 0x0F);
		//TODO Support of different sending types
		if(dataType != 1)
			if(toTrow == null)
				toTrow = new IOException("Sent Data is not Text !!\n Data type: " + dataType);
		
		//Look if it is masked (encrypted)
		boolean masked = (buffer.get(1) & 0x80) > 0;
		if(masked)
			headerLenght += 4;
		
		//get the Lenght
		long contentLenght = (byte) (buffer.get(1) & 0x7F);
		if(contentLenght > 125) {
			if(contentLenght == 126) {
				//Two Bytes Lenght
				headerLenght += 2;
				if(buffer.size() < headerLenght)
					throw new ToLessInformationException("Header not here");
				
				contentLenght = 0;
				
				for(int i = 2; i < 4; i++) {
					contentLenght += ((buffer.get(i) & 0xFF) << (8 * (3 - i)));
				}
			}
			else if(contentLenght == 127) {
				//8 Bytes Lenght
				headerLenght += 8;
				if(buffer.size() < headerLenght)
					throw new ToLessInformationException("Header not here");
				
				contentLenght = 0;
				
				for(int i = 2; i < 10; i++) {
					contentLenght += (buffer.get(i) << (8 * (9 - i)));
				}
			}
		}
		
		//Look if the lenght of the buffer is ok
		if(buffer.size() < headerLenght + contentLenght)
			throw new ToLessInformationException("Message not Fully received");
		
		//Look if there were errors during Processing
		//Not all errs are thrown imediately because this makes the data unreadable
		//(Everytime there is a err the cache is cleared)
		if(toTrow != null)
			throw toTrow; 
		
		//Log output (only for testing)
//		CommonUsedFeatures.showHint("Buffer: " + buffer.size() +
//				"\tContent: " + contentLenght +
//				"\tHeader: " + headerLenght);
		
		//get the mask keys
		byte maskKey[] = new byte[4];
		if(masked) {
			for(int i = headerLenght - 4,j = 0; i < headerLenght; i++) {
				maskKey[j] = buffer.get(i);
				j++;
			}
		}
		
		StringBuilder data = new StringBuilder();
		
		for(int i = 0; i < headerLenght; i++)
			buffer.remove(0);
		
		byte[] arrayBuffer = CommonUsedFeatures.ListToArray(buffer);
		if(masked)
			arrayBuffer = mask(arrayBuffer, maskKey);
		
		for(int i = 0; i < contentLenght; i++) {
			data.append((char) (arrayBuffer[i] & 0xFF));
		}
		return data.toString();
	}

	private boolean clientOKLineReading()
	{
		if(clientType == UNDEFINED) return true;
		if(clientType == JAVA) return true;
		if(clientType == WEBSOCKET) return false;
		
		return false;
	}

	private void openingHandshake() throws NoSuchAlgorithmException
	{
		checkWichClient();
		
		if(clientType == WEBSOCKET)
			replyWebsocket(inputBuffer);
		
		if(clientType == UNDEFINED)
			System.err.println("Undefined Client");
	}

	private void checkWichClient()
	{
		String firstLine = inputBuffer.get(0);
		
		//JAVA Client
		if(firstLine.startsWith("JAVA")) {
			clientType = JAVA;
			parent.setClientVersion(Integer.parseInt(firstLine.split(" ")[1]));
			parent.setClientPasswordVersion(Integer.parseInt(firstLine.split(" ")[2]));
		}
		
		//Websocket & HTTP
		if(firstLine.startsWith("GET")) {
			HTTPHeader request = new HTTPHeader(inputBuffer);
			
			if(request.getField("Upgrade") != null &&
					request.getField("Upgrade").getValue().equals("websocket"))
				clientType = WEBSOCKET;
		}
	}

	public void closeStream()
	{
		//TODO Close websocket correctly
		StreamCloseOK = true;
		writer.close();
	}

	public void sendData(String data)
	{
		if(clientType == UNDEFINED)
			sendUndefined(data);
		else if(clientType == JAVA)
			directSending(data);
		else if(clientType == WEBSOCKET)
			try {
				sendWebsocket(data);
			} catch (IOException e) {
				CommonUsedFeatures.showErr(e);
			}
	}

	private void sendWebsocket(String data) throws IOException {
		List<Integer> header = new ArrayList<Integer>();
		
		//add the start sequence
		header.add( 0x81 );
		
		if(data.length() > 125) {
			//indicate that the lenghtindicator is 2Byte Long
			//indicate that the data is masked
			header.add( 0xFE );
			
			//Calc the two byte and add them
			header.add( data.length() >> 8 );
			header.add( data.length() & 0xFF );
		}
		else {
			//indicate that the data is masked
			//write the lenght of the data
			header.add( (data.length() & 0x7F) | 0x80 );
		}
		
		//Generate the mask and add
		byte[] mask = new byte[4];
		
		for(int i = 0; i < 4; i++) {
			mask[i] = (byte) ((int) ((Math.random() * 256)) & 0xFF);
			header.add( mask[i] & 0xFF );
		}
		
		//Convert the data to an array and mask it
		byte dataArray[] = data.getBytes();
		dataArray = mask(dataArray, mask);
		
		//Write the header
		output.write(CommonUsedFeatures.ListToByteArray(header));
		
		//Write the data
		output.write(dataArray);
	}
	
	private byte[] mask(byte[] data, byte[] mask) {
		for(int i = 0, j = 0; i < data.length; i++) {
			data[i] = (byte) ((mask[j] ^ data[i]) & 0xFF);
			
			j = (j + 1) % 4;
		}
		return data;
	}

	private void directSending(String data) {
		writer.print(data + "\r\n");
		writer.flush();
	}

	private void sendUndefined(String data) {
		if(!data.equals("")) {
			String[] parts = data.split("\n");
			data = "";
			
			for(int i = 0; i < parts.length; i++) {
				data = data + parts[i] + "\r\n";
			}
		}
		
		directSending(data);
	}

	public void replyWebsocket(List<String> req) throws NoSuchAlgorithmException
	{
		HTTPHeader request = new HTTPHeader(req);
		HTTPHeaderField key = request.getField("Sec-WebSocket-Key");
		
		String token = ServerSecurityToken.create(key.getValue());
		
		HTTPHeader responce = new HTTPHeader();
		responce.setFirstLine("HTTP/1.1 101 Switching Protocols");
		responce.addField(new HTTPHeaderField("Upgrade", "websocket"));
		responce.addField(new HTTPHeaderField("Connection", "Upgrade"));
		responce.addField(new HTTPHeaderField("Sec-WebSocket-Accept", token));
		
		sendUndefined(responce.toString());
	}
	
	public boolean linesAvailable() {
		if(!handShakeDone) return false;
		return inputBuffer.size() > 0;
	}
	
	public String getLine() {
		if(!handShakeDone) return "";
		String line = inputBuffer.get(0);
		inputBuffer.remove(0);
		return line;
	}
	
	public String getLineWithoutReading() {
		return inputBuffer.get(0);
	}
}

@SuppressWarnings("serial")
class ToLessInformationException extends IOException{
	public ToLessInformationException(String message) {
		super(message);
	}
}