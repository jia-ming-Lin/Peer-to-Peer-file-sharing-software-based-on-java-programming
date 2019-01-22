package Messages;
/*
Creating n threads for establishing  TCP connections with other peers.
*/
/*Author: Jia-Ming,Lin | Ju-yin,chung | Jia-hao,Zhu*/
import java.util.BitSet;
public class ActualMessage
{
	public int type;
	public int length;
	public byte[] payload;
	public ActualMessage(int type){
		this.type=type;
		this.payload=null;
		this.length=5;
	}
	public ActualMessage(int type,byte[] payload){
		this.type=type;
		this.payload=payload;
		this.length=1+payload.length;
	}
	public ActualMessage(byte[] temp){
		this.payload=new byte[temp.length-5];
		this.type=(int)temp[4];
		for(int i=5;i<temp.length;i++){
			this.payload[i-5]=temp[i];
		}
		this.length=1+this.payload.length;
	}
	public byte[] message2byte(){
		byte[] message = new byte[length+4];
		int i,j=0;
		message[0]=(byte)(length>>24);
		message[1]=(byte)(length>>16);
		message[2]=(byte)(length>>8);
		message[3]=(byte)(length);
		
		message[4]=(byte)type;
		if(payload!=null)
		{
		for(i=5;i<length+4;i++)
			message[i]=payload[j++];
		}
		return message;
	}
}
