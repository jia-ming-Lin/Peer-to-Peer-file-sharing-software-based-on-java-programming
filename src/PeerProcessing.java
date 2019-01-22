import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class PeerProcessing {
	int numberofPreferredNeighbors;
	int unchokingInterval;
	int optimisticUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	List<PeerInfo> peerInfo;

	peerThread op;

	int id;
	int port;
	String hostname;
	int hasFile;
	PeerInfo p;

	BitSet bitfield;
	boolean completed;
	List<peerThread> list;
	writelog w;
	boolean flag=false;
	int ntime;
	int opntime;
	public PeerProcessing(int peerId) throws Exception{
		
		BufferedReader read = new BufferedReader(new FileReader(new File("src/Common.cfg")));
		numberofPreferredNeighbors=Integer.parseInt(read.readLine().split(" ")[1]);
		unchokingInterval=Integer.parseInt(read.readLine().split(" ")[1]);
		optimisticUnchokingInterval=Integer.parseInt(read.readLine().split(" ")[1]);
		fileName=read.readLine().split(" ")[1];
		fileSize=Integer.parseInt(read.readLine().split(" ")[1]);
		pieceSize=Integer.parseInt(read.readLine().split(" ")[1]);
		read = new BufferedReader(new FileReader(new File("src/PeerInfo.cfg")));
		String line=read.readLine();
		peerInfo=new ArrayList<PeerInfo>();
		
		op=null;
		
		completed=false;
		
		ServerSocket serverSocket = null;
		list=new ArrayList<peerThread>();
		
		while(line!=null){
			String[] arr=line.split(" ");
			peerInfo.add(new PeerInfo(arr));
			if(peerId==Integer.parseInt(arr[0]))p=new PeerInfo(arr);
			
			/*
			if(peerId>Integer.parseInt(arr[0])){
				//make connection
				Socket socket=new Socket(arr[1],Integer.parseInt(arr[2]));
				
				peerIdforConnection.add(Integer.parseInt(arr[0]));
			}
			else if(peerId<Integer.parseInt(arr[0])){
				
			}
			else{
				id=peerId;
				hostname=arr[1];
				port=Integer.parseInt(arr[2]);
				hasFile=Integer.parseInt(arr[3]);
				serverSocket=new ServerSocket(port);
				//break;
			}*/
			line=read.readLine();
		}
	
		//System.out.println(peerInfo.size());
		
		int bitsetlength = BitsetLength(fileSize, pieceSize);
		bitfield=new BitSet(bitsetlength);
		if(p.hasFile==1){
			for(int i=0;i<bitsetlength;i++){
				bitfield.set(i,true);
			}
		}
		else{
			for(int i=0;i<bitsetlength;i++){
				bitfield.set(i,false);
			}
		}
		w = new writelog(p.id);
		
		for(PeerInfo pI:peerInfo){
			
			if(pI.id==peerId){
				//p=pI;
				//serverSocket = new ServerSocket(p.port);	
				
				
			}
			else if(peerId<pI.id){
				
				
                if (serverSocket == null) {
                	serverSocket = new ServerSocket(p.port);	
                }
                Socket socket = serverSocket.accept();
               

                list.add(new peerThread(p,pI,socket,fileName,fileSize,pieceSize,bitfield,w,this));
			}
			else{
				Socket socket=new Socket(pI.host,pI.port);
			

                list.add(new peerThread(p,pI,socket,fileName,fileSize,pieceSize,bitfield,w,this));
			}
		}
		
		for(peerThread pt:list){
			new Thread(pt).start();
		}
		//start thread
		//new Thread(new peerThread(id,hostname,port,hasFile,peerIdforConnection,numberofPreferredNeighbors,unchokingInterval,optimisticUnchokingInterval,fileName,fileSize,pieceSize)).start();
		setTimers();
		
	}
	public int cycletimer(){
		int t=(ntime>=opntime)?opntime:ntime;
		ntime=ntime-t;
		opntime=opntime-t;
		return t*1000;
	}
	public void setTimers(){
		TimerTask task_preferred = new TimerTask() {
			
			@Override
			public void run() {
				//System.out.println(unchokingInterval);
				//PeerDoes.chokeAndUnchokePreferred(Peer.this, preferredRemotePeers, selectNewPreferredNeighbors());
				if(completed){
					System.out.println("prefer cancle");
					this.cancel();
				
				}
				else{
					try {
						choosingPNeightbors();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		};
		Timer timer_preferred = new Timer();
		timer_preferred.scheduleAtFixedRate(task_preferred, 0, unchokingInterval * 1000);
		TimerTask task_op = new TimerTask() {
			
			@Override
			public void run() {
				//System.out.println(unchokingInterval);
				//PeerDoes.chokeAndUnchokePreferred(Peer.this, preferredRemotePeers, selectNewPreferredNeighbors());
				if(completed){
					System.out.println("op cancle");
					this.cancel();
				
				}
				else{
					try {
						chooseOp();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		};
		Timer timer_op = new Timer();
		timer_op.scheduleAtFixedRate(task_op, 0, optimisticUnchokingInterval* 1000);
		TimerTask task_check = new TimerTask() {
			
			@Override
			public void run() {
				//System.out.println(unchokingInterval);
				//PeerDoes.chokeAndUnchokePreferred(Peer.this, preferredRemotePeers, selectNewPreferredNeighbors());
				if(completed){
					this.cancel();
					try {
						w.finish();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(op==null&&list.size()==0){
					completed=true;
					System.out.println("Peer "+p.id+" completed transfer.");
					
				}
			}
		};
		Timer timer_check = new Timer();
		timer_check.scheduleAtFixedRate(task_check, 0, 3* 1000);
		
		
	
	}
	private synchronized void choosingPNeightbors() throws IOException{
		ArrayList<Integer> temp=new ArrayList<Integer>();
		if(list.size()!=0){
			if(list.size()<=numberofPreferredNeighbors){
				
				for(peerThread pt:list){
					if(pt.chokestate!=-1){
						pt.unchoke();
						temp.add(pt.target.id);
					}
				}
			}
			else{
				for(int i=0;i<list.size();i++){
					if(i<numberofPreferredNeighbors){
						if(list.get(i).chokestate!=-1){
							list.get(i).unchoke();
							temp.add(list.get(i).target.id);
						}
					}
					else{
						if(list.get(i).chokestate!=1){
							list.get(i).choke();
							temp.add(list.get(i).target.id);
						}
					}
				}
			}
			
		}
		sortList(list);
		w.changeofpreferredneighbors(temp);
	}
	private synchronized void chooseOp() throws IOException{
		if(op!=null){
			op.choke();
			list.add(op);
			op=null;
		}
		for(peerThread pt:list){
			if(pt.chokestate!=-1){
				op=pt;
				op.unchoke();
				list.remove(pt);
				w.change_of_op_uncho(op.peerInfo.id);
				return;
			}
		}
	}
	private void sortList(List<peerThread> list){
		Comparator <peerThread> comparator = new Comparator <peerThread> () {
            int speed1, speed2;
            public int compare (peerThread p1, peerThread p2) {
                speed1 = p1.getSpeed ();
                speed2 = p2.getSpeed ();
                if (speed1 > speed2)
                    return 1;
                else if (speed1 < speed2)
                    return -1;
                else
                    return 0;
            }
        };
        list.sort (comparator);
	}
	private int BitsetLength(int filesize, int chunksize)
	{
		if(filesize%chunksize == 0)
			return filesize/chunksize;
		else
			return filesize/chunksize + 1;
	}
	public synchronized void quit(peerThread pt) throws IOException{
		if(!flag){
			flag=true;
			//w.finish();
		}
		if(op==pt){
			op=null;
		}
		else{
			list.remove(pt);
		}
	}
	public synchronized void broadcast(int i){
		for(peerThread pt:list){
			pt.have(i);
		}
		if(op!=null)op.have(i);
	}
	public static void main(String[] args) throws Exception
	{
		new PeerProcessing(Integer.parseInt(args[0]));
		
		//new PeerProcessing(Integer.parseInt("1001"));
		/*System.out.println("!");
		
		System.out.println("!@3");
		new Thread(new Test(1001)).start();
		new Thread(new Test(1002)).start();
		new Thread(new Test(1003)).start();*/
	}
}
