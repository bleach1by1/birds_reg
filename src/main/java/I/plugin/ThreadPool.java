package I.plugin;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

	private ThreadPool() {}
	
	public static int COREPOOLSIZE = 100;
	
	public static int MAXIMUMPOOLSIZE = 660;
	
	public static long KEEPALIVETIME = 1;
	
	private static ArrayBlockingQueue<Runnable> WORKQUEUE = null;
	
	public static ThreadPoolExecutor EXECUTOR = null;
	
	public static ThreadPoolExecutor getThreadPool() {
		if(EXECUTOR==null) {
			synchronized (ThreadPool.class) {
				if(EXECUTOR==null) {
					WORKQUEUE = new ArrayBlockingQueue<>(COREPOOLSIZE);
					EXECUTOR = new ThreadPoolExecutor(COREPOOLSIZE, MAXIMUMPOOLSIZE, KEEPALIVETIME, TimeUnit.MINUTES, WORKQUEUE);
					EXECUTOR.allowCoreThreadTimeOut(true);
				}
			}
		}
		return EXECUTOR;
	}
	
	public static void main(String[] args) throws Exception{
		for(int i=0;i<1000;i++)
		ThreadPool.getThreadPool().execute(()->{
			System.out.print("ok");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

}
