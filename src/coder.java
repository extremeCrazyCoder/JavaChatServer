import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;


public class coder {
	private static String supportedCharsV1 = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,;.:-_#'+*!\u00A7$%&?\u00df/\\()=<>\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\"\n\r\t";
	private static String supportedCharsV2 = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,;.:-_#'+*!\u00A7$%&?\u00df/\\()=<>\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\"\n\r\t";
	private static String outGoingCharsV1 = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,;.:-_#'+*!\u00A7$%&?\u00df/\\()=<>\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\"";
	private static String outGoingCharsV2 = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	
	public static String decodeV1(String toDecode, String password) {
		//Prepare Password
		byte[] negSHA512 = toNegHex(getSHA512(password));
		byte[] sha512 = toHex(getSHA512(password));

		//decoding second caesar
		toDecode = caesarV1(toDecode, negSHA512);
		
		//gethering length info
		String txtLen = toDecode.substring(0, 6);
		toDecode = toDecode.substring(6);
		
		//decoding second 2D-Cryption
		toDecode = twoDDecryptionRows(toDecode, negSHA512);

		//backmoving the Chars
		toDecode = move(toDecode, sha512, false);
		
		//decoding first 2D-Cryption
		toDecode = twoDDecryptionColumns(toDecode, negSHA512);
		
		//decoding first caesar
		toDecode = caesarV1(toDecode, negSHA512);
		
		//Finishing Decoding Process
		toDecode = toDecode.substring(0, hexToInt(txtLen));
		toDecode = toDecode.replaceAll("\\$\\$", "$");
		toDecode = toDecode.replaceAll("\\$t", "\t");
		toDecode = toDecode.replaceAll("\\$r", "\r");
		toDecode = toDecode.replaceAll("\\$n", "\n");
		return toDecode;
	}

	public static String codeV1(String toCode, String password) throws IOException {
		//Prepare Password
		byte[] sha512 = toHex(getSHA512(password));
		
		//Prepare text
		checkCharsV1(toCode);
		toCode = toCode.replaceAll("\\$", "\\$\\$");
		toCode = toCode.replaceAll("\t", "\\$t");
		toCode = toCode.replaceAll("\r", "\\$r");
		toCode = toCode.replaceAll("\n", "\\$n");
		int toCodeLen = toCode.length();
		int toAppend = (256 - (toCode.length() & 0xFF)) & 0xFF;
		
		if(toCodeLen == 0)
			toAppend = 256;
		
		for(int i = 0; i < toAppend; i++)
			toCode+= " ";
		
		//doing caesar first time
		toCode = caesarV1(toCode, sha512);
		
		//doing 2D-Crypting first time
		toCode = twoDCryptionColumns(toCode, sha512);
		
		//moving some Chars at the end of the String
		toCode = move(toCode, sha512, true);
		
		//doing 2D-Crypting second time
		toCode = twoDCryptionRows(toCode, sha512);
		
		//Appending length info
		toCode = calcHexLen(toCodeLen) + toCode;
		
		//doing caesar second time
		toCode = caesarV1(toCode, sha512);
		
		return toCode;
	}
	
	public static String decodeV2(String toDecode, String password) {
		//Prepare Password
		byte[] negSHA512 = toNegHex(getSHA512(password));
		byte[] sha512 = toHex(getSHA512(password));
		
		//decoding second caesar
		toDecode = caesarV2(toDecode, negSHA512);
		
		//gethering length info
		String txtLen = toDecode.substring(0, 6);
		toDecode = toDecode.substring(6);
		
		//decoding second 2D-Cryption
		toDecode = twoDDecryptionRows(toDecode, negSHA512);

		//backmoving the Chars
		toDecode = move(toDecode, sha512, false);
		
		//decoding first 2D-Cryption
		toDecode = twoDDecryptionColumns(toDecode, negSHA512);
		
		//decoding first caesar
		toDecode = caesarV2(toDecode, negSHA512);
		
		//Finishing Decoding Process
		toDecode = toDecode.substring(0, hexToInt(txtLen));
		
		List<Replacement> replaceOptions = new ArrayList<Replacement>();
		replaceOptions.add(new Replacement("yy", "y"));
		replaceOptions.add(new Replacement("yT", "\t"));
		replaceOptions.add(new Replacement("yR", "\r"));
		replaceOptions.add(new Replacement("yN", "\n"));
		replaceOptions.add(new Replacement("y0", "$"));
		replaceOptions.add(new Replacement("y1", "#"));
		replaceOptions.add(new Replacement("y2", "'"));
		replaceOptions.add(new Replacement("y3", "+"));
		replaceOptions.add(new Replacement("y4", "*"));
		replaceOptions.add(new Replacement("y5", "!"));
		replaceOptions.add(new Replacement("y6", "\u00A7"));
		replaceOptions.add(new Replacement("y8", "%"));
		replaceOptions.add(new Replacement("y9", "&"));
		replaceOptions.add(new Replacement("ya", "/"));
		replaceOptions.add(new Replacement("yb", "\\"));
		replaceOptions.add(new Replacement("yc", "("));
		replaceOptions.add(new Replacement("yd", ")"));
		replaceOptions.add(new Replacement("ye", "="));
		replaceOptions.add(new Replacement("yf", "<"));
		replaceOptions.add(new Replacement("yg", ">"));
		replaceOptions.add(new Replacement("yh", "\u00e4"));
		replaceOptions.add(new Replacement("yi", "\u00f6"));
		replaceOptions.add(new Replacement("yj", "\u00fc"));
		replaceOptions.add(new Replacement("yk", "\u00c4"));
		replaceOptions.add(new Replacement("yl", "\u00d6"));
		replaceOptions.add(new Replacement("ym", "\u00dc"));
		replaceOptions.add(new Replacement("yn", "\""));
		replaceOptions.add(new Replacement("y7", "?"));
		replaceOptions.add(new Replacement("yo", "\u00df"));
		replaceOptions.add(new Replacement("yp", "-"));
		replaceOptions.add(new Replacement("yq", "_"));
		replaceOptions.add(new Replacement("yr", ":"));
		replaceOptions.add(new Replacement("ys", "."));
		replaceOptions.add(new Replacement("yt", ","));
		replaceOptions.add(new Replacement("yu", ";"));
		
		toDecode = replaceAll(toDecode, replaceOptions);
		return toDecode;
	}
	
	public static String codeV2(String toCode, String password) throws IOException {
		//Prepare Password
		byte[] sha512 = toHex(getSHA512(password));
		
		//Prepare text
		checkCharsV2(toCode);
		List<Replacement> replaceOptions = new ArrayList<Replacement>();
		replaceOptions.add(new Replacement("y", "yy"));
		replaceOptions.add(new Replacement("\t", "yT"));
		replaceOptions.add(new Replacement("\r", "yR"));
		replaceOptions.add(new Replacement("\n", "yN"));
		replaceOptions.add(new Replacement("$", "y0"));
		replaceOptions.add(new Replacement("#", "y1"));
		replaceOptions.add(new Replacement("'", "y2"));
		replaceOptions.add(new Replacement("+", "y3"));
		replaceOptions.add(new Replacement("*", "y4"));
		replaceOptions.add(new Replacement("!", "y5"));
		replaceOptions.add(new Replacement("\u00A7", "y6"));
		replaceOptions.add(new Replacement("%", "y8"));
		replaceOptions.add(new Replacement("&", "y9"));
		replaceOptions.add(new Replacement("/", "ya"));
		replaceOptions.add(new Replacement("\\", "yb"));
		replaceOptions.add(new Replacement("(", "yc"));
		replaceOptions.add(new Replacement(")", "yd"));
		replaceOptions.add(new Replacement("=", "ye"));
		replaceOptions.add(new Replacement("<", "yf"));
		replaceOptions.add(new Replacement(">", "yg"));
		replaceOptions.add(new Replacement("\u00e4", "yh"));
		replaceOptions.add(new Replacement("\u00f6", "yi"));
		replaceOptions.add(new Replacement("\u00fc", "yj"));
		replaceOptions.add(new Replacement("\u00c4", "yk"));
		replaceOptions.add(new Replacement("\u00d6", "yl"));
		replaceOptions.add(new Replacement("\u00dc", "ym"));
		replaceOptions.add(new Replacement("\"", "yn"));
		replaceOptions.add(new Replacement("?", "y7"));
		replaceOptions.add(new Replacement("\u00df", "yo"));
		replaceOptions.add(new Replacement("-", "yp"));
		replaceOptions.add(new Replacement("_", "yq"));
		replaceOptions.add(new Replacement(":", "yr"));
		replaceOptions.add(new Replacement(".", "ys"));
		replaceOptions.add(new Replacement(",", "yt"));
		replaceOptions.add(new Replacement(";", "yu"));
		
		toCode = replaceAll(toCode, replaceOptions);
		int toCodeLen = toCode.length();
		int toAppend = (256 - (toCode.length() & 0xFF)) & 0xFF;
		
		if(toCodeLen == 0)
			toAppend = 256;
		
		for(int i = 0; i < toAppend; i++)
			toCode+= outGoingCharsV2.charAt((int) (Math.random() * outGoingCharsV2.length()));
		
		//doing caesar first time
		toCode = caesarV2(toCode, sha512);
		
		//doing 2D-Crypting first time
		toCode = twoDCryptionColumns(toCode, sha512);
		
		//moving some Chars at the end of the String
		toCode = move(toCode, sha512, true);
		
		//doing 2D-Crypting second time
		toCode = twoDCryptionRows(toCode, sha512);
		
		//Appending length info
		toCode = calcHexLen(toCodeLen) + toCode;
		
		//doing caesar second time
		toCode = caesarV2(toCode, sha512);
		
		return toCode;
	}
	
	private static String replaceAll(String unreplaced, List<Replacement> replaceOptions) {
		String replaced = "";
		
		while(unreplaced.length() > 0) {
			boolean charReplaced = false;
			
			for(Replacement option: replaceOptions) {
				if(unreplaced.startsWith(option.getToReplace())) {
					replaced+= option.getReplacement();
					unreplaced = unreplaced.substring(option.getToReplace().length());
					charReplaced = true;
					break;
				}
			}
			
			if(!charReplaced) {
				replaced+= unreplaced.charAt(0);
				unreplaced = unreplaced.substring(1);
			}
		}
		
		return replaced;
	}

	private static String calcHexLen(int len) {
		char[] hexChars = "0123456789ABCDEF".toCharArray();
		String hex = "";
		
		for(int i = 0; i < 6; i++) {
			hex = hexChars[(len >> (i << 2)) & 0x0F] + hex;
		}
		return hex;
	}
	
	private static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}

	private static String twoDDecryptionRows(String toProcess, byte[] sha512) {
		char[][][] chars = strTo2DChar(toProcess);
		for(int i = 0; i < chars.length; i++) {
			chars[i] = moveColumns(chars[i], sha512, 16 + ((i % 4) << 5));
			chars[i] = moveRows(chars[i], sha512, (i % 4) << 5);
		}
		return char2DToStr(chars);
	}

	private static String twoDDecryptionColumns(String toProcess, byte[] sha512) {
		char[][][] chars = strTo2DChar(toProcess);
		for(int i = 0; i < chars.length; i++) {
			chars[i] = moveRows(chars[i], sha512, 16 + ((i % 4) << 5));
			chars[i] = moveColumns(chars[i], sha512, (i % 4) << 5);
		}
		return char2DToStr(chars);
	}
	
	private static String twoDCryptionRows(String toProcess, byte[] sha512) {
		char[][][] chars = strTo2DChar(toProcess);
		for(int i = 0; i < chars.length; i++) {
			chars[i] = moveRows(chars[i], sha512, (i % 4) << 5);
			chars[i] = moveColumns(chars[i], sha512, 16 + ((i % 4) << 5));
		}
		return char2DToStr(chars);
	}

	private static String twoDCryptionColumns(String toProcess, byte[] sha512) {
		char[][][] chars = strTo2DChar(toProcess);
		for(int i = 0; i < chars.length; i++) {
			chars[i] = moveColumns(chars[i], sha512, (i % 4) << 5);
			chars[i] = moveRows(chars[i], sha512, 16 + ((i % 4) << 5));
		}
		return char2DToStr(chars);
	}

	private static String char2DToStr(char[][][] chars) {
		StringBuilder coded = new StringBuilder();
		for(int i = 0; i < chars.length; i++)
			for(int j = 0; j < chars[i].length; j++)
				for(int z = 0; z < chars[i][j].length; z++)
					coded.append(chars[i][j][z]);
		
		return coded.toString();
	}
	
	private static char[][][] strTo2DChar(String toProcess) {
		char chars[][][] = new char[toProcess.length() >> 8][16][16];
		
		for(int i = 0; i < chars.length; i++)
			for(int j = 0; j < chars[i].length; j++)
				for(int z = 0; z < chars[i][j].length; z++)
					chars[i][j][z] = toProcess.charAt(((i * chars[i].length + j) * chars[i][j].length) + z);
		return chars;
	}
	
	private static char[][] moveRows(char[][] normal, byte[] sha512, int start) {
		/*
		 *    0 1 2 3 4 5 6 7 8 9 A B C D E F
		 * 00 a a a a a a a a a a a a a a a a
		 * 10 a a a a a a a a a a a a a a a a
		 * 20 a a a a a a a a a a a a a a a a
		 * 30 a a a a a a a a a a a a a a a a
		 * 40 a a a a a a a a a a a a a a a a
		 * 50 a a a a a a a a a a a a a a a a
		 * 60 a a a a a a a a a a a a a a a a
		 * 70 a a a a a a a a a a a a a a a a
		 * 80 a a a a a a a a a a a a a a a a
		 * 90 a a a a a a a a a a a a a a a a
		 * A0 a a a a a a a a a a a a a a a a
		 * B0 a a a a a a a a a a a a a a a a
		 * C0 a a a a a a a a a a a a a a a a
		 * D0 a a a a a a a a a a a a a a a a
		 * E0 a a a a a a a a a a a a a a a a
		 * F0 a a a a a a a a a a a a a a a a
		 */
		
		char[][] after = new char[normal.length][normal[0].length];
		
		for(int row = 0; row < normal.length; row++) {
			for(int column = 0; column < normal[0].length; column++) {
				int newColumn = (column + sha512[start + row]) % 16;
				if(newColumn < 0) newColumn+= 16;
				after[row][newColumn] = normal[row][column];
			}
		}
		return after;
	}

	private static char[][] moveColumns(char[][] normal, byte[] sha512, int start) {
		char[][] after = new char[normal.length][normal[0].length];
		
		for(int row = 0; row < normal.length; row++) {
			for(int column = 0; column < normal[0].length; column++) {
				int newRow = (row + sha512[start + column]) % 16;
				if(newRow < 0) newRow+= 16;
				after[newRow][column] = normal[row][column];
			}
		}
		return after;
	}

	private static byte[] toHex(byte[] bytes) {
		byte[] back = new byte[bytes.length<<1];
		
		for(int i = 0; i < bytes.length; i++) {
			back[i<<1] = (byte) ((bytes[i] & 0xF0) >> 4);
			back[(i<<1) + 1] = (byte) (bytes[i] & 0x0F);
		}
		return back;
	}
	
	private static byte[] toNegHex(byte[] bytes) {
		bytes = toHex(bytes);
		
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (-1 * bytes[i]);
		}
		
		return bytes;
	}

	private static String move(String toProcess, byte[] sha512, boolean forward) {
		//Count the ones of the hash
		int toMove = 64; //offset
		for(int i = 0; i < sha512.length; i++)
			for(int j = 128; j > 0; j = j>>1)
				if((sha512[i] & j) > 0) toMove++;
		
		toMove = toMove % toProcess.length();
		if(toMove == 0) toMove = toProcess.length() / 2;
		
		//move the chars
		if(forward)
			toProcess = toProcess.substring(toMove) + toProcess.substring(0, toMove);
		else
			toProcess = toProcess.substring(toProcess.length() - toMove) + toProcess.substring(0, toProcess.length() - toMove);
		
		return toProcess;
	}

	private static String caesarV1(String toCode, byte[] amounts) {
		String crypted = "";
		
		for(int i = 0; i < toCode.length(); i++) {
			int charNum = outGoingCharsV1.indexOf(toCode.charAt(i));
			int newCharNum = (charNum + (amounts[i % amounts.length] << 4) + amounts[(i + 1) % amounts.length]) % outGoingCharsV1.length();
			if(newCharNum < 0) newCharNum+= outGoingCharsV1.length();
			crypted+= outGoingCharsV1.charAt(newCharNum);
		}
		return crypted;
	}

	private static String caesarV2(String toCode, byte[] amounts) {
		String crypted = "";
		
		for(int i = 0; i < toCode.length(); i++) {
			int charNum = outGoingCharsV2.indexOf(toCode.charAt(i));
			int newCharNum = (charNum + (amounts[i % amounts.length] << 4) + amounts[(i + 1) % amounts.length]) % outGoingCharsV2.length();
			if(newCharNum < 0) newCharNum+= outGoingCharsV2.length();
			crypted+= outGoingCharsV2.charAt(newCharNum);
		}
		return crypted;
	}

	private static void checkCharsV1(String toCheck) throws IOException {
		for(int i = 0; i < toCheck.length(); i++)
			if(!supportedCharsV1.contains("" + toCheck.charAt(i))) throw new IOException("Wrong Char: " + toCheck.charAt(i));
	}

	private static void checkCharsV2(String toCheck) throws IOException {
		for(int i = 0; i < toCheck.length(); i++)
			if(!supportedCharsV2.contains("" + toCheck.charAt(i))) throw new IOException("Wrong Char: " + toCheck.charAt(i));
	}

	private static byte[] getSHA512(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] b = password.getBytes("UTF-8");
			return md.digest(b);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String getSupportedCharsV1() {
		return supportedCharsV1;
	}
	
	public static String getSupportedCharsV2() {
		return supportedCharsV2;
	}

	public static String decodeNormal(String toCode, String password) {
		return decodeV2(toCode, password);
	}

	public static String codeNormal(String toCode, String password) throws IOException {
		return codeV2(toCode, password);
	}
	
	public static int getNormalVersion() {
		return 2;
	}
}

class Replacement {
	String toReplace;
	String replacement;
	
	public Replacement(String toReplace, String replacement) {
		this.toReplace = toReplace;
		this.replacement = replacement;
	}
	
	String getToReplace() {
		return toReplace;
	}
	
	String getReplacement() {
		return replacement;
	}
}