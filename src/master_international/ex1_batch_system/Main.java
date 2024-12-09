package master_international.ex1_batch_system;

public class Main {
	public static void main(String[] args) {
		var batch = new MultiThreadedBatchSystem(10);
		batch.start();
	}
	

	
	public void f() {
		
		// this is a synchronous call (because of the wait)
		response = sendMessage("do it for me").wait();

		// process the response....
		
		
		 sendMessage("do it for me").handler(new Handler() {
			
			void whenResponseArrives(response){
				// process the response....
			 }
			 
		 });
		
		
		
		
		
		
		
		
	}
	
	
	
	
}
