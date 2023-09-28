package de.gwasch.code.demothreadpool.events;

import de.gwasch.code.escframework.events.events.AbstractAction;

public class WorkAction extends AbstractAction {

	private String workPackageName;
		
	public WorkAction(String workPackageName, long pushTime) {
		this.workPackageName = workPackageName;
		setPushTime(pushTime);
	}
	
	public WorkAction(String workPackageName) {
		this(workPackageName, 0);
	}

	public boolean execute() {
		
		try {
			Thread.sleep(500);
		} 
		catch (InterruptedException e) {
		}
		
		System.out.println(workPackageName + " executed in thread " + Thread.currentThread().getName());	
		
		return true;
	}
}
