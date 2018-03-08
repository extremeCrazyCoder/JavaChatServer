import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ServerSecurityToken {
	public static void main(String[] args) throws Exception {
		String token = "";
		
		if(args.length == 1) {
			token = args[0];
		}
		else {
			InputStreamReader streamReader = new InputStreamReader(System.in);
			BufferedReader bReader = new BufferedReader(streamReader);
			
			System.out.print("Input: ");
			token = bReader.readLine();
		}
		
		System.out.println("Output: " + create(token));
	}
	
	public static String create(String token) throws NoSuchAlgorithmException {
		//Add the GUID (Gloabally Unique Identifier)
		token = token + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		
		//Create the SHA-1 Hash
		byte[] hash = getSHA1(token);
		
		//Code via base64
		return CommonUsedFeatures.base64(hash);
	}

	private static byte[] getSHA1(String toHash) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA1").digest(toHash.getBytes());
	}
}