package de.gwasch.code.demothreadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.gwasch.code.demothreadpool.events.WorkAction;
import de.gwasch.code.escframework.events.events.MainPushAction;
import de.gwasch.code.escframework.events.events.TimerAction;
import de.gwasch.code.escframework.events.processors.Executor;
import de.gwasch.code.escframework.events.processors.ManagedThread;
import de.gwasch.code.escframework.events.processors.Processor;
import de.gwasch.code.escframework.events.processors.Scheduler;
import de.gwasch.code.escframework.events.processors.TimerHandler;
import de.gwasch.code.escframework.events.utils.PNBuilder;
import de.gwasch.code.escframework.events.utils.TimerFactory;

public class App2 {
	
    public static void main(String[] args) throws InterruptedException {
    	
    	BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
		Processor<TimerAction<WorkAction>> timer = TimerFactory.createTimer(new MainPushAction(queue));
		timer.activate();
	
		Processor<WorkAction> p = new PNBuilder<WorkAction>("init")
			.add(new TimerHandler<>(timer))
			.add(new Scheduler<>())
			.add(new ManagedThread<>(new MainPushAction(queue)))
			.add(new Executor<>()).top();
		
		p.activate();
		
		p.process(new WorkAction("wp1", System.currentTimeMillis() + 6000));
		p.process(new WorkAction("wp2", System.currentTimeMillis() + 3000));
		
		while(true) {
			queue.take().run();
		}
    }
}
