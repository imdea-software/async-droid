import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
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
import soot.jimple.StaticInvokeExpr;


public class MyBodyTransformer extends BodyTransformer {

	static SootClass schedulerClass;
	static SootMethod initiateScheduler, waitMyTurn, notifyScheduler, enterMonitor, exitMonitor;

	@Override
	protected void internalTransform(final Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {

		schedulerClass = Scene.v().getSootClass("myScheduler.MyScheduler");
		initiateScheduler = schedulerClass.getMethod("void initiateScheduler()");
		waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
		notifyScheduler = schedulerClass.getMethod("void notifyScheduler()");
		enterMonitor = schedulerClass.getMethod("void enterMonitor()");
		exitMonitor = schedulerClass.getMethod("void exitMonitor()");

		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.MyScheduler")){
			System.out.println("Skipping myScheduler.MyScheduler");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.SchedulerRunnable")){
			System.out.println("Skipping myScheduler.SchedulerRunnable");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.PendingThreads")){
			System.out.println("Skipping myScheduler.PendingThreads");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.ThreadData")){
			System.out.println("Skipping myScheduler.ThreadData");
			return;
		}
		
		final PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		Unit u = iter.next();

		if (b.getMethod().getName().equals("onCreate")) {

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseInvokeStmt(InvokeStmt stmt) {
						if(stmt.getInvokeExpr().getMethod().getSignature().equals("<android.app.Activity: void onCreate(android.os.Bundle)>")){

							StaticInvokeExpr initiateExpr = Jimple.v().newStaticInvokeExpr(initiateScheduler.makeRef());
							Unit initiateStmt = Jimple.v().newInvokeStmt(initiateExpr);
							units.insertAfter(initiateStmt, stmt);
							System.out.println("Initiate Scheduler stmt added..");
						}
						//System.out.println(stmt.getInvokeExpr().getMethod().getSignature().toString());
					}
				});
				u = iter.next();
			}
			return;
		}
		
		// runnables posted to a looper thread
		// by post, postAtFrontOfQueue, postAtTime, postDelayed 
		// or sent to UI thread by runOnUIThread
		if (b.getMethod().getName().equals("run")) {
			instrumentMethod(units, u, iter);
			return;
		}

		// handling of messages sent to a looper thread
		// by sendMessage, sendMessageAtFrontOfQueue, sendMessageAtTime, sendMessageDelayed
		if (b.getMethod().getName().equals("handleMessage")) {
			instrumentMethod(units, u, iter);
			return;
		}

		if (b.getMethod().getName().equals("onPreExecute")) {
			instrumentMethod(units, u, iter);
			return;
		}

		if (b.getMethod().getName().equals("onPostExecute")) {
			instrumentMethod(units, u, iter);
			return;
		}

		if (b.getMethod().getName().equals("onProgressUpdate")) {
			instrumentMethod(units, u, iter);
			return;
		}

		if (b.getMethod().getName().equals("doInBackground")) {
			instrumentMethod(units, u, iter);
			return;
		}

	}
	
	public void instrumentMethod(final PatchingChain<Unit> units, Unit u, Iterator<Unit> iter){
		StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
		Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
		units.insertAfter(waitStmt, u);
		System.out.println("Wait for CPU stmt added..");

		while (iter.hasNext()) {

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
			u = iter.next();
		}

		StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyScheduler.makeRef());
		Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
		units.insertBefore(notifyStmt, u);
		System.out.println("Release CPU stmt added before the last unit..");
	}


}