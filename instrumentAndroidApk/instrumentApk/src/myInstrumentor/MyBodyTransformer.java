
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
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;


public class MyBodyTransformer extends BodyTransformer {

	static SootClass schedulerClass;
	static SootMethod initiateScheduler, waitMyTurn, notifyCompletion, sendThreadInfo;

/*	static {
		schedulerClass = Scene.v().loadClassAndSupport("MyScheduler2");
		initiate = schedulerClass.getMethod("void initiateScheduler()");
		waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
		notifyCompletion = schedulerClass.getMethod("void notifyCompletion()");
	}*/

	@Override
	protected void internalTransform(final Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {

		schedulerClass = Scene.v().getSootClass("myScheduler.MyScheduler");
		initiateScheduler = schedulerClass.getMethod("void initiateScheduler()");
		sendThreadInfo = schedulerClass.getMethod("void sendThreadInfo()");
		waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
		notifyCompletion = schedulerClass.getMethod("void notifyCompletion()");
		
		final PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		Unit u = iter.next();

		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.MyScheduler")){
			System.out.println("Skipping myScheduler.MyScheduler");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.MyScheduler$1")){
			System.out.println("Skipping myScheduler.MyScheduler$1");
			return;
		}
		if(b.getMethod().getDeclaringClass().toString().equals("myScheduler.MyScheduler$1Anonymous0")){
			System.out.println("Skipping myScheduler.MyScheduler$1Anonymous0");
			return;
		}
		
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
			
		if (b.getMethod().getName().equals("run")) {
			
			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);
						System.out.println("run release CPU stmt added..");
					}
				});
				u = iter.next();
			}
			return;

		}
		
		if (b.getMethod().getName().equals("handleMessage")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);
						System.out.println("handleMessage release CPU stmt added..");
					}
				});
				u = iter.next();
			}
			return;

		}
		
		if (b.getMethod().getName().equals("onPreExecute")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {
				if (u instanceof ReturnStmt){		
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, u);
						System.out.println("onPreExecute release CPU stmt added..");
				}
				u = iter.next();
			}
			
			StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
			Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
			units.insertBefore(notifyStmt, u);
			return;
		}
		
		if (b.getMethod().getName().equals("onPostExecute")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {
				if (u instanceof ReturnStmt){		
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, u);
						System.out.println("onPostExecute release CPU stmt added..");
				}
				u = iter.next();
			}
			
			StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
			Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
			units.insertBefore(notifyStmt, u);
			return;

		}
		
		if (b.getMethod().getName().equals("onProgressUpdate")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {
				if (u instanceof ReturnStmt){		
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, u);
						System.out.println("onPogressUpdate release CPU stmt added..");
				}
				u = iter.next();
			}
			
			StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
			Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
			units.insertBefore(notifyStmt, u);
			return;
		}
		
		if (b.getMethod().getName().equals("doInBackground")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {
				if (u instanceof ReturnStmt){		
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, u);
						System.out.println("doInBackground release CPU stmt added..");
				}
				u = iter.next();
			}
			
			StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
			Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
			units.insertBefore(notifyStmt, u);
			return;
		}

	}


}
