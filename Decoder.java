package Server;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

public class Decoder {
	
	private static HashMap<Integer, String> DecodeValues = new HashMap(); 
	private static HashMap<String, Integer> EncodeValues = new HashMap(); 
	private static boolean AlreadyInitialized = false;
	
	public static String DecodeMessage(IntBuffer aMessage)
	{
		String lDecodedMessage = "";
		
		for(int lIndex = 0; lIndex < 300; lIndex++)
		{
			if(DecodeValues.containsKey(aMessage.get(lIndex)))
			{
				lDecodedMessage += DecodeValues.get(aMessage.get(lIndex));
			}
			else
			{
				//no code value for character, must be not encoded
				int lValueOfCharacter = aMessage.get(lIndex);
				if(lValueOfCharacter < 0 )
				{
					//not encoded character, done on purpose
					lValueOfCharacter *= -1;
					byte lLowerByte = (byte) (lValueOfCharacter & 255);
					byte lUpperByte = (byte) (lValueOfCharacter & 65280);
					byte[] lByteArray = new byte [1];
					lByteArray [0] = lLowerByte;
					String lNotEncodedCharacter = new String(lByteArray);
					lDecodedMessage += lNotEncodedCharacter;
				}
				else
				{
					//something went wrong, codeValueMission
					lDecodedMessage += "NULL";
				}
			}
		}
		
		return lDecodedMessage;
	}
	
	public static ByteBuffer EncodeMessage(String aMessage)
	{
		ByteBuffer lEncodedMessage = ByteBuffer.allocateDirect(300 * 4);//current message size
		
		for(int lIndex = 0; lIndex < aMessage.length(); lIndex++)
		{
			String lCharacter = aMessage.substring(lIndex, lIndex + 1);
			
			if(EncodeValues.containsKey(lCharacter))
			{
				int lValue = EncodeValues.get(lCharacter);
				lEncodedMessage.putInt(lValue);
			}
			else
			{
				//no code value for character, don't encode it
				int lValueOfCharacter = aMessage.substring(lIndex, lIndex + 1).codePointAt(0);
				lEncodedMessage.putInt(lValueOfCharacter * -1); //make it negative to signal client that this value is not encoded
			}
		}
		
		//reset start location to beginning
		lEncodedMessage.rewind();
		return lEncodedMessage;
	}
	
	public static void CreateCodeValues()
	{
		if(AlreadyInitialized)
			return; //has already been populated
		else
			AlreadyInitialized = true;
		
		//Capital Letters
		DecodeValues.put(100, "A");
		DecodeValues.put(53532, "B");
		DecodeValues.put(32942, "C");
		DecodeValues.put(3594289, "D");
		DecodeValues.put(94289, "E");
		DecodeValues.put(90424, "F");
		DecodeValues.put(5, "G");
		DecodeValues.put(2315, "H");
		DecodeValues.put(7654, "I");
		DecodeValues.put(3248, "J");
		DecodeValues.put(8679, "K");
		DecodeValues.put(868, "L");
		DecodeValues.put(29, "M");
		DecodeValues.put(2277, "N");
		DecodeValues.put(7646, "O");
		DecodeValues.put(77, "P");
		DecodeValues.put(9999, "Q");
		DecodeValues.put(325, "R");
		DecodeValues.put(886637, "S");
		DecodeValues.put(5902, "T");
		DecodeValues.put(6603, "U");
		DecodeValues.put(232, "V");
		DecodeValues.put(4, "W");
		DecodeValues.put(909, "X");
		DecodeValues.put(55558, "Y");
		DecodeValues.put(221789, "Z");
		
		//Lower Letters
		DecodeValues.put(10090, "a");
		DecodeValues.put(8, "b");
		DecodeValues.put(2390, "c");
		DecodeValues.put(321, "d");
		DecodeValues.put(942829, "e");
		DecodeValues.put(54525, "f");
		DecodeValues.put(3, "g");
		DecodeValues.put(324922, "h");
		DecodeValues.put(22147, "i");
		DecodeValues.put(57432, "j");
		DecodeValues.put(247, "k");
		DecodeValues.put(112, "l");
		DecodeValues.put(9324, "m");
		DecodeValues.put(28967, "n");
		DecodeValues.put(353566, "o");
		DecodeValues.put(2021, "p");
		DecodeValues.put(2389, "q");
		DecodeValues.put(12345678, "r");
		DecodeValues.put(905634, "s");
		DecodeValues.put(2221, "t");
		DecodeValues.put(23134, "u");
		DecodeValues.put(8987, "v");
		DecodeValues.put(33226, "w");
		DecodeValues.put(218, "x");
		DecodeValues.put(919, "y");
		DecodeValues.put(3774872, "z");
		
		//Numbers
		DecodeValues.put(4812, "0");
		DecodeValues.put(652, "1");
		DecodeValues.put(21209, "2");
		DecodeValues.put(2784, "3");
		DecodeValues.put(3856, "4");
		DecodeValues.put(221133, "5");
		DecodeValues.put(784321, "6");
		DecodeValues.put(3214, "7");
		DecodeValues.put(83, "8");
		DecodeValues.put(4312, "9");
		
		//Special Characters
		DecodeValues.put(614324, ":");
		DecodeValues.put(8873, "-");
		DecodeValues.put(48907, "|");
		DecodeValues.put(24571, " ");
		DecodeValues.put(28771874, "_");
		
		//------------------------- FOR ENCODING ------------------------------//
		//Capital Letters
		EncodeValues.put("A", 100);
		EncodeValues.put("B", 53532);
		EncodeValues.put("C", 32942);
		EncodeValues.put("D", 3594289);
		EncodeValues.put("E", 94289);
		EncodeValues.put("F", 90424);
		EncodeValues.put("G", 5);
		EncodeValues.put("H", 2315);
		EncodeValues.put("I", 7654);
		EncodeValues.put("J", 3248);
		EncodeValues.put("K", 8679);
		EncodeValues.put("L", 868);
		EncodeValues.put("M", 29);
		EncodeValues.put("N", 2277);
		EncodeValues.put("O", 7646);
		EncodeValues.put("P", 77);
		EncodeValues.put("Q", 9999);
		EncodeValues.put("R", 325);
		EncodeValues.put("S", 886637);
		EncodeValues.put("T", 5902);
		EncodeValues.put("U", 6603);
		EncodeValues.put("V", 232);
		EncodeValues.put("W", 4);
		EncodeValues.put("X", 909);
		EncodeValues.put("Y", 55558);
		EncodeValues.put("Z", 221789);
		
		//Lower Letters
		EncodeValues.put("a", 10090);
		EncodeValues.put("b", 8);
		EncodeValues.put("c", 2390);
		EncodeValues.put("d", 321);
		EncodeValues.put("e", 942829);
		EncodeValues.put("f", 54525);
		EncodeValues.put("g", 3);
		EncodeValues.put("h", 324922);
		EncodeValues.put("i", 22147);
		EncodeValues.put("j", 57432);
		EncodeValues.put("k", 247);
		EncodeValues.put("l", 112);
		EncodeValues.put("m", 9324);
		EncodeValues.put("n", 28967);
		EncodeValues.put("o", 353566);
		EncodeValues.put("p", 2021);
		EncodeValues.put("q", 2389);
		EncodeValues.put("r", 12345678);
		EncodeValues.put("s", 905634);
		EncodeValues.put("t", 2221);
		EncodeValues.put("u", 23134);
		EncodeValues.put("v", 8987);
		EncodeValues.put("w", 33226);
		EncodeValues.put("x", 218);
		EncodeValues.put("y", 919);
		EncodeValues.put("z", 3774872);
		
		//Numbers
		EncodeValues.put("0", 4812);
		EncodeValues.put("1", 652);
		EncodeValues.put("2", 21209);
		EncodeValues.put("3", 2784);
		EncodeValues.put("4", 3856);
		EncodeValues.put("5", 221133);
		EncodeValues.put("6", 784321);
		EncodeValues.put("7", 3214);
		EncodeValues.put("8", 83);
		EncodeValues.put("9", 4312);
		
		//Special Characters
		EncodeValues.put(":", 614324);
		EncodeValues.put("-", 8873);
		EncodeValues.put("|", 48907);
		EncodeValues.put(" ", 24571);
		EncodeValues.put("_", 28771874);
	}

}
