import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;


@SuppressWarnings("unused")
public class CommonUsedFeatures {
    public static void printArray(String[] data) {
        for(int i = 0; i < data.length; i++) {
            System.out.println("\t[" + i + "]: " + data[i]);
        }
    }

    public static String convertToString(List<String> data) {
        StringBuilder str = new StringBuilder();
        
        for(int i = 0; i < data.size(); i++) {
            str.append(data.get(i));
            str.append("\n");
        }
        
        return str.toString();
    }
    
    public static void showHint(String hint) {
        //JOptionPane.showMessageDialog(null, hint);
        System.out.println(hint);
    }
    
    public static void showProblem(String hint) {
        System.out.println(hint);
    }

    public static void showWrongDataFromClientMessage(ServerClient client, String line, String hint) {
        System.out.println("Wrong data from Client\tIP: " + client.ipAndPort +
                "\tData: " + line + "\tState: " + client.state + "\t" + hint);
    }

    public static String intToHex(int toConvert) {
        char[] hexChars = "0123456789ABCDEF".toCharArray();
        
        StringBuilder back = new StringBuilder();
        
        while(toConvert != 0) {
            back.append(hexChars[toConvert & 0x0F]);
            back.append(hexChars[(toConvert & 0xF0) >> 4]);
            
            toConvert = toConvert >> 8;
        }
        
        return back.reverse().toString();
    }

    public static void showErr(Exception e) {
        e.printStackTrace();
    }

    public static byte[] ListToByteArray(List<Integer> list) {
        byte[] array = new byte[list.size()];
        
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).byteValue();
        }
        
        return array;
    }

    public static byte[] ListToArray(List<Byte> list) {
        byte[] array = new byte[list.size()];
        
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        
        return array;
    }
    
    public static String getTime() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
    }
    
    public static String byteToHex(byte[] bytes) {
        char[] hexChars = "0123456789ABCDEF".toCharArray();
        StringBuilder hex = new StringBuilder();
        
        for(int i = bytes.length; i > 0; i--) {
            int tempByte = bytes[i - 1] & 0xFF;
            hex.append(hexChars[tempByte >> 4]);
            hex.append(hexChars[tempByte & 0x0F]);
        }
        
        return hex.toString();
    }

    public static byte[] getMD5(String toHash) {
        try {
            return MessageDigest.getInstance("MD5").digest(toHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }
    
    public static String getMD5String(String toHash) {
        return byteToHex(getMD5(toHash));
    }

    public static String formatNumber(int number, int before) {
        String back = "" + number;
        
        while(back.length() < before) {
            back = "0" + back;
        }
        
        return back;
    }

    public static String base64(byte[] orig) {
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
        for(int i = 0; i < boolElements.length; i++) {
            sixBit[i / 6] |= (boolElements[i])?(1 << (5 - (i % 6))):(0);
        }
        
        return sixBit;
    }
}