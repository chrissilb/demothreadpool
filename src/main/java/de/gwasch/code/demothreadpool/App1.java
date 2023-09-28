package de.gwasch.code.demothreadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gwasch.code.demothreadpool.events.WorkAction;
import de.gwasch.code.escframework.events.events.MainPushAction;
import de.gwasch.code.escframework.events.events.PushAction;
import de.gwasch.code.escframework.events.processors.Executor;
import de.gwasch.code.escframework.events.processors.ManagedThread;
import de.gwasch.code.escframework.events.processors.Processor;
import de.gwasch.code.escframework.events.processors.Scheduler;
import de.gwasch.code.escframework.events.utils.PNBuilder;

public class App1 {
	
    public static void main(String[] args) throws InterruptedException {
    	
    	BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
		PushAction pushAction = new MainPushAction(queue);
		
		Processor<WorkAction> p = new PNBuilder<WorkAction>("init")
			.add(new Scheduler<>())
			.add(new ManagedThread<>(pushAction))
			.add(new Executor<>()).top();
		
		p.activate();
		p.process(new WorkAction("wp1"));
		p.process(new WorkAction("wp2"));
//		p.deactivate();
		
		while(true) {
			queue.take().run();
		}
    }
}
