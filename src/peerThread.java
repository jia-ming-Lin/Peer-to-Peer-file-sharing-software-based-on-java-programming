import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import Messages.ActualMessage;
import Messages.HandShakeMessage;


public class peerThread implements Runnable{
	PeerInfo peerInfo;
	PeerInfo target;

	String fileName;
	int fileSize;
	int pieceSize;
	Socket socket;
	int speed;
	OutputStream output;
	InputStream input;
	writelog w;
	BitSet bitfield;
	BitSet tarbitfield;
	int chokestate=0;
	int bechokestate=0;
	int requestate=0;
	int interestate=0;
	int sendstate=-1;
	int havestate=0;
	int renum=-1;
	int[] requestfield;
	RandomAccessFile raf;
	List<Integer> havelist=new ArrayList<Integer>();
	PeerProcessing peerProcess;
	int blength;
	public peerThread(PeerInfo p,PeerInfo targetp,Socket socket,String fileN,int fsize,int psize,BitSet bitfield,writelog w,PeerProcessing peerProcess) throws IOException{
		peerInfo=p;
		target=targetp;
		this.w =w;

		fileName="/cise/homes/jinhao/P2P/peer_"+p.id+"/"+fileN;
		File place=new File("/cise/homes/jinhao/P2P/peer_"+p.id+"/");
		//System.out.println(p.id+String.valueOf(place.exists()));
		if(!place.exists()){
			System.out.println("create"+"/P2P/peer_"+p.id+"/");
			place.mkdirs();
		}
		fileSize=fsize;
		pieceSize=psize;
		output=socket.getOutputStream();
		input=socket.getInputStream();
		raf=new RandomAccessFile(this.fileName,"rw");
		this.bitfield=bitfield;
		this.blength=BitsetLength(fileSize, pieceSize);
		
		
		//System.out.println(peerInfo.id+":"+blength+""+checkbf(bitfield));
		tarbitfield=new BitSet(blength);
		tarbitfield.set(0, blength, false);
		requestfield=new int[blength];
		for(int i=0;i<requestfield.length;i++){
			requestfield[i]=0;
		}
		this.peerProcess=peerProcess;
	}
	
	private int BitsetLength(int filesize, int chunksize)
	{
		if(filesize%chunksize == 0)
			return filesize/chunksize;
		else
			return filesize/chunksize + 1;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//System.out.println("Start running: "+peerInfo.id+" to "+target.id);
		try {
			handShake(target.id);
			ActualMessage bFieldMessage=new ActualMessage(5,bitfield.toByteArray());
			output.write(bFieldMessage.message2byte());
			output.flush();
			while(true){
				if(sendstate==1){
					ActualMessage m=new ActualMessage(0);
					output.write(m.message2byte());
					output.flush();
					sendstate=0;
				}
				else if(sendstate==2){
					ActualMessage m=new ActualMessage(1);
					output.write(m.message2byte());
					output.flush();
					sendstate=0;
				}
				if(havestate==1){
					while(havelist.size()!=0){
						ActualMessage m=new ActualMessage(4);
						byte[] s=int2bytearray(havelist.get(0));
						m.payload=s;
						m.length=1+s.length;
						output.write(m.message2byte());
						output.flush();
						havelist.remove(0);
					}
					havestate=-1;
				}
				//System.out.println(peerInfo.id+" "+flag1+" "+target.id+flag2+"");
				if(checkbf(bitfield)&&checkbf(tarbitfield)&&havestate!=1){
					Thread.sleep(1000);
					peerProcess.quit(this);
					break;
				}
				if(input.available()==0){
					Thread.sleep(50);
					continue;
				}
				
				byte[] byteMessage=receiveFile();
				ActualMessage message=new ActualMessage(byteMessage);
				if(bechokestate==1&&renum!=-1){
					requestate=-1;
					requestfield[renum]=0;
					renum=-1;
				}
				if(renum!=-1&&message.type==0){
					requestate=-1;
					requestfield[renum]=0;
					renum=-1;
				}
				switch(message.type){
					case 0:
						receiveChoke();
						break;
					case 1:
						receiveUnchoke();
						break;
					case 2:
						receiveInterest();
						break;
					case 3:
						receiveNotInterest();
						break;
					case 4:
						receiveHave(message);
						break;
					case 5:
						receiveBitfield(message);
						break;
					case 6:
						receiveRequest(message);
						break;
					case 7:
						receivePiece(message);
						break;
				}
			}
			System.out.println("Finish"+peerInfo.id+" "+target.id);
			//w.finish();
			//w.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		/*while(true){
			
			System.out.println("Start running: "+peerId+fileName);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		//System.out.println("Start running: "+peerId);
	}
	public int getpiece(){
		//int blength=tarbitfield.length();
		int base=new Random().nextInt(blength);
		//System.out.println("The base choose is " +base+"; the bitfield length is "+blength+"/");
		int i;
		for(i=0; i<blength; i++)
				if(tarbitfield.get((base+i)%blength)==true && bitfield.get((base+i)%blength)==false)
					return (base+i)%blength;
	
		return -1;
	}
	public void receiveChoke() throws IOException{
		w.choking(target.id);
		bechokestate=1;
	}
	public void receiveUnchoke() throws IOException{
		int r;
		while(true){
			r=getpiece();
			if(r==-1)
				break;
			if(requestfield[r]==0)break;
		}
		if(r!=-1){
			ActualMessage m=new ActualMessage(6);
			renum=r;
			byte[] s=int2bytearray(r);
			m.payload=s;
			m.length=1+s.length;
			output.write(m.message2byte());
			output.flush();
			requestate=1;
			bechokestate=-1;
			requestfield[r]=1;
		}
		else{
			ActualMessage m=new ActualMessage(3);
			output.write(m.message2byte());
			output.flush();
		}
		w.unchoking(target.id);
	}
	public void receiveInterest(){
		interestate=1;
	}
	public void receiveNotInterest(){
		interestate=-1;
	}
	public void receiveHave(ActualMessage m) throws IOException{
		int piece=bytearray2int(m.payload);
		w.rcvhavemessage(target.id, piece);
		tarbitfield.flip(piece);
		if(bitfield.get(piece)==false){
			ActualMessage message=new ActualMessage(2);
			output.write(message.message2byte());
			output.flush();
			
		}
		else{
			ActualMessage message=new ActualMessage(3);
			output.write(message.message2byte());
			output.flush();
		}
	}
	public void receiveBitfield(ActualMessage m) throws IOException{
		tarbitfield=BitSet.valueOf(m.payload);
		int index=getpiece();
		System.out.println(peerInfo.id+" "+target.id+"piece:"+index);
		if(index>=0){
			ActualMessage message=new ActualMessage(2);
			output.write(message.message2byte());
			output.flush();
		}
		else{
			ActualMessage message=new ActualMessage(3);
			output.write(message.message2byte());
			output.flush();
		}
	}
	public synchronized void have(int i){
		havelist.add(i);
		havestate=1;
	}
	public synchronized void choke(){
		chokestate=1;
		sendstate=1;
	}
	public synchronized void unchoke() throws IOException{
		chokestate=-1;
		sendstate=2;
		w.unchoking(target.id);
	}
	public void receiveRequest(ActualMessage m) throws IOException{
		int piece=bytearray2int(m.payload);
		//System.out.println("asdsf:"+piece+chokestate);
		if(chokestate!=1&&tarbitfield.get(piece)==false){
		
			ActualMessage message=new ActualMessage(7);
			byte[] s=getBytePiece(piece);
			message.payload=s;
			message.length=1+s.length;
			output.write(message.message2byte());
			output.flush();
		}
	}
	public void receivePiece(ActualMessage m) throws IOException{
		byte[] index=new byte[4];
		byte[] content=new byte[m.payload.length-4];
		for(int i=0;i<4;i++){
			index[i]=m.payload[i];
		}
		int in=bytearray2int(index);
		
		if(bitfield.get(in)==false){
			for(int j=0,i=4;i<m.payload.length;i++){
				content[j++]=m.payload[i];
			}
			writePiece(content,in);
			bitfield.set(in,true);
			w.dlpiece(in, target.id);
			speed+=pieceSize;
			peerProcess.broadcast(in);
			renum=-1;
			requestate=-1;
			requestfield[in]=2;
		}
		if(requestate!=1){
			int piece;
			while(true){
				piece=getpiece();
				if(piece==-1||requestfield[piece]==0)break;
			}
			renum=piece;
			
			if(piece!=-1){
				ActualMessage message=new ActualMessage(6);
				byte[] s=int2bytearray(piece);
				message.payload=s;
				message.length=1+s.length;
				output.write(message.message2byte());
				output.flush();
				requestate=1;
				requestfield[piece]=1;
				
			}
			else{
				ActualMessage message=new ActualMessage(3);
				output.write(message.message2byte());
				output.flush();
				requestate=-1;
			}
		}
	}
	public boolean checkbf(BitSet bitfield)
	{
		
		for(int i=0;i<blength;i++)
			if(bitfield.get(i)==false)
				return false;
		return true;
	}
	synchronized void handShake(int targetid) throws IOException{
		HandShakeMessage message=new HandShakeMessage(peerInfo.id);
		byte[] msg=message.Convert_Message_Bytes();
		output.write(msg);
		output.flush();
		w.startconnection(targetid);
		byte[] recv=new byte[32];
		/*while(true){
			if(input.available()>0){
				System.out.println(peerInfo.id+" "+input.available());
				break;
			}
		}*/
		input.read(recv, 0, 32);
		HandShakeMessage recvM=new HandShakeMessage(recv);
		
		if(recvM.checkHeader()){
			if(recvM.getId()==targetid){
				System.out.println(peerInfo.id+" "+recvM.getId());
				w.connectiondone(targetid);
				
			}
		}
		//w.close();
	}
	int bytearray2int(byte[] bytearray)
	{
		int val=0, i=0;
		for(i=0;i<bytearray.length;i++){
			int shift= (4 - 1 - i) * 8;
            val +=(bytearray[i] & 0x000000FF) << shift;
		}
		return val;	
	}
	byte[] int2bytearray(int index)
	{
		byte[] message = new byte[4];
		message[0]=(byte)(index>>24);
		message[1]=(byte)(index>>16);
		message[2]=(byte)(index>>8);
		message[3]=(byte)(index);
		return message;
	}
	synchronized byte[] receiveFile() throws IOException{
		byte[] length = new byte[4];
        byte[] outA;
        int rev, total=0;
        while(true)
        {
            if ((total < 4)) {
                rev = input.read (length, total, 4 - total);
                total = total + rev;
            } else {
                break;
            }
        }
        int fileLength = bytearray2int(length);
        outA = new byte[fileLength];
        total=0;

        while(total<fileLength)
        {
            rev = input.read(outA, total, fileLength-total);
            total = total + rev;
        }
        byte[] out = new byte[length.length+outA.length];
        System.arraycopy(length, 0, out, 0, 4);
        System.arraycopy(outA, 0, out, 4, 4 + outA.length - 4);
        return out;
	}
	public int getSpeed(){
		int s=speed;
		speed=0;
		return s;
	}
	public byte[] getBytePiece(int index) throws IOException{
		int plength;
		int blength=bitfield.length();
		if(index==blength-1)
			plength=fileSize-(blength-1)*pieceSize;
		else
			plength=pieceSize;
		byte[] buf = new byte[plength];
		byte[] piece = int2bytearray(index);
		raf.seek((long)index*pieceSize);
		raf.read(buf);
		byte[] temp = new byte[buf.length+piece.length];
		int i;
		for(i=0; i< piece.length; i++)
			temp[i] = piece[i];
		int j=0;
		for(i=piece.length;i<temp.length;i++)
			temp[i]=buf[j++];
		return temp;
	}
	public void writePiece(byte[] content,int index) throws IOException{
		raf.seek((long)index*pieceSize);
		raf.write(content);
	}
	private synchronized void checkstate(){
		
	}
}
