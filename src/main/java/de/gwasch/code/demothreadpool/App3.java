package de.gwasch.code.demothreadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gwasch.code.demothreadpool.events.DeactivateRunnable;
import de.gwasch.code.demothreadpool.events.WorkAction;
import de.gwasch.code.escframework.events.events.MainPushAction;
import de.gwasch.code.escframework.events.events.TimerAction;
import de.gwasch.code.escframework.events.handler.EventAdapter;
import de.gwasch.code.escframework.events.handler.StateAdapter;
import de.gwasch.code.escframework.events.processors.Dispatcher;
import de.gwasch.code.escframework.events.processors.Executor;
import de.gwasch.code.escframework.events.processors.ManagedThread;
import de.gwasch.code.escframework.events.processors.Processor;
import de.gwasch.code.escframework.events.processors.Scheduler;
import de.gwasch.code.escframework.events.processors.TimerHandler;
import de.gwasch.code.escframework.events.utils.PNBuilder;
import de.gwasch.code.escframework.events.utils.TimerFactory;



public class App3 {

	static class EventHandler extends EventAdapter<WorkAction> {
		
		private int nrEventsFinished;
		
		public EventHandler() {
			nrEventsFinished = 0;
		}
		public void onFinish(WorkAction a, boolean success) {
			nrEventsFinished++;
			
			if (nrEventsFinished == 2) {
				p.deactivate();
			}
		}
	}
	
	static class InitStateHandler extends StateAdapter {
		
		public void onDeactivated(boolean success) {
			System.out.println("worker thread deactivated");
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
		
		p = new PNBuilder<WorkAction>("init")
			.add(dispatcher = new Dispatcher<>())
			.add(new TimerHandler<>(timer))
			.add(new Scheduler<>())
			.add(new ManagedThread<>(new MainPushAction(queue)))
			.add(new Executor<>()).top();
		
		dispatcher.register(WorkAction.class, new EventHandler());
		dispatcher.register(new InitStateHandler());
		
		p.activate();
		
		p.process(new WorkAction("wp1", System.currentTimeMillis() + 6000));
		p.process(new WorkAction("wp2", System.currentTimeMillis() + 3000));
		
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
