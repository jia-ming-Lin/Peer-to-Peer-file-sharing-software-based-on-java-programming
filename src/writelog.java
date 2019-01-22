

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class writelog {
BufferedWriter writefile;
int peer_id;

public writelog(int peer_id) throws IOException {
	this.peer_id=peer_id;
	String filepath="/cise/homes/jinhao/P2P/Log/log_peer_" + peer_id + ".log";
	File place=new File("/cise/homes/jinhao/P2P/Log/peer_"+peer_id+"/");
	//System.out.println(p.id+String.valueOf(place.exists()));
	if(!place.exists()){
		//System.out.println("create"+"/P2P/peer_"+p.id+"/");
		place.mkdirs();
	}
	writefile=new BufferedWriter(new FileWriter(filepath,true));
}

public  String time(){
	 Date dNow = new Date();
	SimpleDateFormat timeformat = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz",Locale.ENGLISH);
    return timeformat.format(dNow);
}
public  void startconnection(int connection_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] makes a connection to Peer ["+connection_id+"].\n";
	//String content="123";
	System.out.println(content);
	
	writefile.write(content);
	
}
public  void connectiondone(int connection_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] is connected from Peer ["+connection_id+"].\n";
	
	writefile.write(content);
	
}
public  void changeofpreferredneighbors(ArrayList peer_list) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] has the preferred neighbors ["+peer_list+"].";
	for(int i = 0; i < peer_list.size(); i++)
	{
		if(i == peer_list.size() - 1) {
			writefile.write(peer_list.get(i)+"].\n");
		System.out.print(peer_list.get(i)+".");
		}
		else
		{
			writefile.write(peer_list.get(i)+", ");
		System.out.print(peer_list.get(i)+",");
		}
	}
	writefile.write(content);
}
public  void change_of_op_uncho(int op_uncho_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] has the optimistically unchoked neighbor [" + 
			op_uncho_id+"].\n";
	writefile.write(content);
}
public  void unchoking(int uncho_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] is unchoked by ["+uncho_id+"]. \n";
	System.out.println(content);
	writefile.write(content);
}
public  void choking(int cho_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] is unchoked by ["+cho_id+"]. \n";
	writefile.write(content);
}
public  void rcvhavemessage (int connection_id,int piece_index) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] received the �have� message from ["+connection_id+"] for the piece ["+piece_index+"].\n ";
	writefile.write(content);
}
public  void rcvinterestmessage(int connection_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] received the �interested� message from ["+connection_id+"].\n ";
	writefile.write(content);
}

public  void rcvnotinterestmessage(int connection_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] received the �not interested� message from ["+connection_id+"]. \n";
    writefile.write(content);}
public  void dlpiece(int piece_index,int connection_id) throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] has downloaded the piece ["+piece_index+"] from ["+connection_id+"]. \n";
    writefile.write(content);}
public  void completeofdl() throws IOException{
	String content="["+time()+"]: Peer ["+peer_id+"] has downloaded the complete file.\n";
    writefile.write(content);
}
public void close() throws IOException{
	writefile.close();
}
synchronized public void finish() throws IOException
{
	writefile.write("["+time()+"]: Peer ["+peer_id+"] has downloaded the complete file.\n");
	writefile.close();
}
public static void main(String args) throws Exception{
	writelog log = new writelog(123);
	ArrayList<Integer> array = new ArrayList<Integer>();
	array.add(111);
	array.add(222);
	array.add(333);
	array.add(444);
	array.add(555);
	array.add(666);
	log.change_of_op_uncho(1);
	log.connectiondone(2);
	log.completeofdl();
	
	
}
}
