package de.gwasch.code.demothreadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gwasch.code.demothreadpool.events.DeactivateRunnable;
import de.gwasch.code.demothreadpool.events.WorkAction;
import de.gwasch.code.escframework.events.events.MainPushAction;
import de.gwasch.code.escframework.events.events.TimerAction;
import de.gwasch.code.escframework.events.listeners.StateAdapter;
import de.gwasch.code.escframework.events.processors.Dispatcher;
import de.gwasch.code.escframework.events.processors.Executor;
import de.gwasch.code.escframework.events.processors.ManagedThread;
import de.gwasch.code.escframework.events.processors.Processor;
import de.gwasch.code.escframework.events.processors.Scheduler;
import de.gwasch.code.escframework.events.processors.TimerHandler;
import de.gwasch.code.escframework.events.utils.PNBuilder;
import de.gwasch.code.escframework.events.utils.TimerFactory;



public class App5 {

	static class InitStateHandler extends StateAdapter {
				
		public void onIdle() {
			p.deactivate();
		}
		
		public void onDeactivated(boolean success) {
			System.out.println("worker threads deactivated");
			timer.deactivate();
		}
	}
	
	static class TimerStateHandler extends StateAdapter {
		
		public void onDeactivated(boolean success) {
			System.out.println("timer thread deactivated");
			queue.add(new DeactivateRunnable());
		}
	}
	
	private static BlockingQueue<Runnable> queue;
	private static Dispatcher<TimerAction<WorkAction>> timer;
	private static Processor<WorkAction> p;
	
    public static void main(String[] args) throws InterruptedException {
    	
    	queue = new LinkedBlockingQueue<>();
		timer = TimerFactory.createTimer(new MainPushAction(queue));
		timer.register(new TimerStateHandler());
		timer.activate();
		
		Dispatcher<WorkAction> dispatcher;
		Scheduler<WorkAction> scheduler;
		
		PNBuilder<WorkAction> pnBuilder = new PNBuilder<WorkAction>("init")
			.add(dispatcher = new Dispatcher<>())
			.add(new TimerHandler<>(timer))
			.add(scheduler = new Scheduler<>());
		
		for (int i = 0; i < 2; i++) {
			pnBuilder.add(scheduler, new ManagedThread<>(new MainPushAction(queue)))
				.add(new Executor<>());
		}
			
		dispatcher.register(new InitStateHandler());
		
		p = pnBuilder.top();
		
		p.activate();
		
		for (int i = 0; i < 20; i++ ) {
			p.process(new WorkAction("wp1"));
		}
		p.process(new WorkAction("wp2", System.currentTimeMillis() + 2000));
		p.process(new WorkAction("wp3", System.currentTimeMillis() + 2000));
		
		while(true) {
			Runnable r = queue.take();
			 
			if (r instanceof DeactivateRunnable) {
				break;
			}
			else {
				r.run();
			}
		}
    }
}
