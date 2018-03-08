
public class PWCoder {
	public static String codeV1(String password, String codeString) {
		byte tocode[] = password.getBytes();
		byte codeBytes[] = base64Decode(codeString);
		byte codedBytes[] = new byte[tocode.length];
		
		for(int i = 0; i < codedBytes.length; i++) {
			codedBytes[i] = (byte) (tocode[i] ^ codeBytes[i % codeBytes.length]);
		}
		
		String finished = base64Code(codedBytes);
		
		return finished;
	}
	
	public static String decodeV1(String codedPassword, String codeString) {
		byte codeBytes[] = base64Decode(codeString);
		byte codedBytes[] = base64Decode(codedPassword);
		
		StringBuilder passw = new StringBuilder();
		
		for(int i = 0; i < codedBytes.length; i++) {
			passw.append((char) (codedBytes[i] ^ codeBytes[i % codeBytes.length]));
		}
		
		return passw.toString();
	}
	
	static byte[] base64Decode(String codedPassword) {
		char[] base64Chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
				+ "0123456789+/").toCharArray();
		char[] temp = codedPassword.toCharArray();
		
		int sixBitLen = 0;
		for(int i = 0; i < temp.length; i++) {
			if(sixBitLen == 0 && temp[i] == '=')
				sixBitLen = i;
		}
		if(sixBitLen == 0)
			sixBitLen = temp.length;
		
		byte[] sixBit = new byte[sixBitLen];
		
		for(int i = 0; i < sixBitLen; i++)
			for(int j = 0; j < base64Chars.length; j++)
				if(temp[i] == base64Chars[j]) {
					sixBit[i] = (byte) j;
					break;
				}
		
		byte decoded[] = conversation6to8Bit(sixBit, temp.length - sixBitLen);
		return decoded;
	}

	public static String base64Code(byte[] orig) {
		char[] base64Chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
				+ "0123456789+/").toCharArray();
		
		byte[] sixBit = conversation8to6Bit(orig);
		StringBuilder back = new StringBuilder();
		
		//Calc how much = must be used
		int end = (sixBit.length * 3) - (orig.length * 4);
		
		for(int i = 0; i < sixBit.length; i++) {
			back.append(base64Chars[sixBit[i]]);
		}
		
		for(int i = 0; i < end; i++)
			back.append("=");
		
		return back.toString();
	}

	private static byte[] conversation8to6Bit(byte[] orig) {
		//every byte Contains 8 bit
		int bitsOrig = orig.length * 8;
		
		//Add as much bits as needed to be divisble by six
		int numElements = bitsOrig +  (6 - (bitsOrig % 6)) % 6;
		
		//Create the Array
		boolean[] boolElements = new boolean[numElements];
		
		//fill it
		for(int i = 0; i < boolElements.length; i++) {
			if(i < bitsOrig) {
				//fill with orig bit
				boolElements[i] = (orig[i / 8] & (1 << (7 - (i % 8)))) > 0;
			}
			else {
				//fill with 0
				boolElements[i] = false;
			}
		}
		
		//Create the final Array
		byte[] sixBit = new byte[numElements / 6];
		
		//and fill it
		for(int i = 0; i < numElements; i++) {
			sixBit[i / 6] |= (boolElements[i])?(1 << (5 - (i % 6))):(0);
		}
		
		return sixBit;
	}
	
	private static byte[] conversation6to8Bit(byte[] sixBit, int numToMuch) {
			boolean[] boolElements = new boolean[sixBit.length * 6 - numToMuch * 2];
			
			//fill
			for(int i = 0; i < boolElements.length; i++) {
				boolElements[i] = (sixBit[i / 6] & (1 << (5 - (i % 6)))) > 0;
			}
			
			//Create the final Array
			byte[] fullByte = new byte[boolElements.length / 8];
			
			//and fill it
			for(int i = 0; i < boolElements.length; i++) {
				fullByte[i / 8] |= (boolElements[i])?(1 << (7 - (i % 8))):(0);
			}
			
			return fullByte;
	}

	public static int getNormalVersion() {
		return 1;
	}
	
	public static String codeNormal(String password, String codeString) {
		return codeV1(password, codeString);
	}
	
	public static String decodeNormal(String codedPassword, String codeString) {
		return decodeV1(codedPassword, codeString);
	}
}
