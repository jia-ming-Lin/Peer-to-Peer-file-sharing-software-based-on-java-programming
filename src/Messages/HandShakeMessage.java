package Messages;
/*The class is for handshakemessage*/
/*Author: Jia-Ming,Lin | Ju-yin,chung | Jia-hao,Zhu*/
public class HandShakeMessage {
	int PeerId;
	public String HandShakeMessage;
	public String HandShakeHeader;
	// For testing code
	//public static void main (String[] args){
	//	HandShakeMessage s = new HandShakeMessage(Integer.parseInt(args[0]));
	//	byte[] k = s.Convert_Message_Bytes();
	//	System.out.println(new String(k));
	//	HandShakeMessage q = new HandShakeMessage(k);
	//	System.out.println(q.PeerId + q.HandShakeHeader);
	//}	
	//
	//Creating HandShakeMessage	Header+'0000000000'+PeeId
	public HandShakeMessage(int PeerId)
	{	//As mention in the assignment, the  handshakeheader should be "P2PFILESHARINGPROJ"//
		this.PeerId = PeerId;
		HandShakeHeader = "P2PFILESHARINGPROJ";
		HandShakeMessage=HandShakeHeader + "0000000000" +PeerId;
	}
	
	public HandShakeMessage(byte[] ReceivedHandSM)
	{
		String Receivedmessage = new String(ReceivedHandSM);
		StringBuffer stringBuffer = new StringBuffer(Receivedmessage);
		HandShakeHeader = stringBuffer.substring(0, 18);
		PeerId = Integer.parseInt(stringBuffer.substring(28,32));
	}	
		
//Convert the message to bytes.	public HandShakeMessage(byte[] ReceivedHandSM)
	public byte[] Convert_Message_Bytes()
		{	byte[] message = new byte[32];
			message = HandShakeMessage.getBytes();
			return message;
		}	
	public boolean checkHeader(){
		if(HandShakeHeader.equals("P2PFILESHARINGPROJ"))return true;
		return false;
	}
	public int getId(){
		return PeerId;
	}
}
