
public class Test implements Runnable{
	int pid;
	public Test(int id){
		pid=id;
	}
	public static void main(String[] args) throws Exception
	{
		//new PeerProcessing(Integer.parseInt(args[0]));
		new Thread(new Test(1001)).start();
		new Thread(new Test(1002)).start();
		new Thread(new Test(1003)).start();

		//new Thread(new Test(1004)).start();
		//new Thread(new Test(1005)).start();
		//new PeerProcessing(Integer.parseInt("1002"));
		//System.out.println("!@3");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			new PeerProcessing(pid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
