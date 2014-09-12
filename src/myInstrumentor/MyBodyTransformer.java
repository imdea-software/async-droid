package myInstrumentor;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ThisRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;


public class MyBodyTransformer extends BodyTransformer {

	static SootClass schedulerClass;
	static SootMethod initiateScheduler, waitMyTurn, notifyScheduler, enterMonitor, exitMonitor;

	@Override
	protected void internalTransform(final Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {

		schedulerClass = Scene.v().getSootClass("myScheduler.MyScheduler");
		initiateScheduler = schedulerClass.getMethod("void initiateScheduler(android.content.Context)");
		waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
		notifyScheduler = schedulerClass.getMethod("void notifyScheduler()");
		enterMonitor = schedulerClass.getMethod("void enterMonitor()");
		exitMonitor = schedulerClass.getMethod("void exitMonitor()");

		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.MyScheduler")){
			//System.out.println("Skipping myScheduler.MyScheduler");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.SchedulerRunnable")){
			//System.out.println("Skipping myScheduler.SchedulerRunnable");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.DelayServiceConHandler")){
			//System.out.println("Skipping myScheduler.DelayServiceConHandler");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.DelayServiceConHandler$IncomingMessageHandler")){
			//System.out.println("Skipping myScheduler.DelayServiceConHandler$IncomingMessageHandler");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.DelayGenerator")){
			//System.out.println("Skipping myScheduler.DelayGenerator");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.PendingThreads")){
			//System.out.println("Skipping myScheduler.PendingThreads");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.ThreadData")){
			//System.out.println("Skipping myScheduler.ThreadData");
			return;
		}
		
		if(b.getMethod().getDeclaringClass().toString().startsWith("android.support")){
			//System.out.println("Skipping android.support methods");
			return;
		}
		
		final PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();

		if (b.getMethod().getName().equals("onCreate")) {

			while (iter.hasNext()) {

				Unit u = iter.next();
				u.apply(new AbstractStmtSwitch() {

					public void caseInvokeStmt(InvokeStmt stmt) {
						if(stmt.getInvokeExpr().getMethod().getSignature().equals("<android.app.Activity: void onCreate(android.os.Bundle)>")){

							// get activity's this reference
							Local thisAct = b.getThisLocal();
													
							// get application context:
							Local context = addTmpRef(b, "ctx", "android.content.Context");
							SootMethod sm = Scene.v().getMethod("<android.content.ContextWrapper: android.content.Context getApplicationContext()>");	
							VirtualInvokeExpr getContextExpr = Jimple.v().newVirtualInvokeExpr(thisAct, sm.makeRef());
																		
							// initiate scheduler with this context
							StaticInvokeExpr initiateExpr = Jimple.v().newStaticInvokeExpr(initiateScheduler.makeRef(), context);
							Unit initiateStmt = Jimple.v().newInvokeStmt(initiateExpr);
							units.insertAfter(initiateStmt, stmt);
							units.insertAfter(Jimple.v().newAssignStmt(context, getContextExpr), stmt);  // correct order
							System.out.println("===========Initiate Scheduler stmt added..");
						}
					}
				});
			}
			return;
		}
		
		//TODO: If parent is pure Thread, then detect loop and instrument beginning and the end of the loop
		// runnables posted to a looper thread
		// by post, postAtFrontOfQueue, postAtTime, postDelayed 
		// or sent to UI thread by runOnUIThread
		if (b.getMethod().getName().equals("run")) {
			System.out.println("===========Instrumenting run..");
			instrumentMethod(units, iter);
			return;
		}

		// handling of messages sent to a looper thread
		// by sendMessage, sendMessageAtFrontOfQueue, sendMessageAtTime, sendMessageDelayed
		if (b.getMethod().getName().equals("handleMessage")) {
			System.out.println("===========Instrumenting handleMessage..");
			instrumentMethod(units, iter);
			return;
		}

		if (b.getMethod().getName().equals("onPreExecute")) {
			System.out.println("===========Instrumenting onPreExecute..");
			instrumentMethod(units, iter);
			return;
		}

		if (b.getMethod().getName().equals("onPostExecute")) {
			System.out.println("===========Instrumenting onPostExecute..");
			instrumentMethod(units, iter);
			return;
		}

		if (b.getMethod().getName().equals("onProgressUpdate")) {
			System.out.println("===========Instrumenting donProgressUpdate..");
			instrumentMethod(units, iter);
			return;
		}

		if (b.getMethod().getName().equals("doInBackground")) {
			System.out.println("===========Instrumenting doInBackground..");
			instrumentMethod(units, iter);
			return;
		}
		
		// if a method in an Activity class is defined by user, synchronize it with the scheduler:
		// (so that UI gives change to other threads when a UI event is received)	
		SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
		if(b.getMethod().getDeclaringClass().getSuperclass().toString().equals("android.app.Activity") && !activityClass.declaresMethod(b.getMethod().getNumberedSubSignature()) ){
			System.out.println("===========Instrumenting a user defined method: " + b.getMethod());
			instrumentMethod(units, iter);
			return;
		}

	}
	
	
	public void instrumentMethod(final PatchingChain<Unit> units, Iterator<Unit> iter){
		
		if(!iter.hasNext())
			return;
		
		Unit u = iter.next();
		StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
		Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
		units.insertAfter(waitStmt, u);
		System.out.println("Wait for CPU stmt added..");

		while (iter.hasNext()) {

			u = iter.next();
			u.apply(new AbstractStmtSwitch() {

				public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
					StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyScheduler.makeRef());
					Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
					units.insertBefore(notifyStmt, stmt);
					System.out.println("Release CPU stmt added..");
				}
				
				public void caseReturnStmt(ReturnStmt stmt) {
					StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyScheduler.makeRef());
					Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
					units.insertBefore(notifyStmt, stmt);
					System.out.println("Release CPU stmt added..");
				}
				
				public void caseRetStmt(RetStmt stmt) {
					StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyScheduler.makeRef());
					Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
					units.insertBefore(notifyStmt, stmt);
					System.out.println("Release CPU stmt added..");
				}
				
				public void caseEnterMonitorStmt(EnterMonitorStmt stmt){
					StaticInvokeExpr enterMonitorExpr = Jimple.v().newStaticInvokeExpr(enterMonitor.makeRef());
					Unit enterMonitorStmt = Jimple.v().newInvokeStmt(enterMonitorExpr);
					units.insertAfter(enterMonitorStmt, stmt);
					System.out.println("Enter monitor stmt added..");
				}
				
				public void caseExitMonitorStmt(ExitMonitorStmt stmt){
					StaticInvokeExpr exitMonitorExpr = Jimple.v().newStaticInvokeExpr(exitMonitor.makeRef());
					Unit exitMonitorStmt = Jimple.v().newInvokeStmt(exitMonitorExpr);
					units.insertAfter(exitMonitorStmt, stmt);
					System.out.println("Exit monitor stmt added..");
				}
			});
			
		}

	}

	private static Local addTmpRef(Body body, String name, String className) {
		Local tmpRef = Jimple.v().newLocal(name, RefType.v(className));
		body.getLocals().add(tmpRef);
		return tmpRef;
	}

}