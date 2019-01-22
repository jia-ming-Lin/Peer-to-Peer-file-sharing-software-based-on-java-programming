
public class PeerInfo {
	int id;
	int port;
	String host;
	int hasFile;
	public PeerInfo(String[] arr){
		id=Integer.parseInt(arr[0]);
		host=arr[1];
		port=Integer.parseInt(arr[2]);
		hasFile=Integer.parseInt(arr[3]);
	}
}
