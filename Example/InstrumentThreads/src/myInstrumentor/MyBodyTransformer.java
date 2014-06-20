package myInstrumentor;

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
import soot.jimple.Jimple;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;


public class MyBodyTransformer extends BodyTransformer {

	static SootClass schedulerClass;
	static SootMethod initiate, waitMyTurn, notifyCompletion;

	@Override
	protected void internalTransform(final Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {

		schedulerClass = Scene.v().getSootClass("myScheduler.MyScheduler");
		initiate = schedulerClass.getMethod("void initiateScheduler()");
		waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
		notifyCompletion = schedulerClass.getMethod("void notifyCompletion()");
		
		final PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		Unit u = iter.next();

		if (b.getMethod().getName().equals("onCreate")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(initiate.makeRef());
			Unit initiateStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(initiateStmt, u);
			
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

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);
					}
				});
				
				u = iter.next();
			}
			return;

		}
		
		if (b.getMethod().getName().equals("onPostExecute")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);

					}
				});
				u = iter.next();
			}
			return;

		}
		
		if (b.getMethod().getName().equals("onProgressUpdate")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);

					}
				});
				u = iter.next();
			}
			return;
		}
		
		if (b.getMethod().getName().equals("doInBackground")) {

			StaticInvokeExpr waitExpr = Jimple.v().newStaticInvokeExpr(waitMyTurn.makeRef());
			Unit waitStmt = Jimple.v().newInvokeStmt(waitExpr);
			units.insertAfter(waitStmt, u);

			while (iter.hasNext()) {

				u.apply(new AbstractStmtSwitch() {

					public void caseReturnStmt(ReturnStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);

					}
					public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);

					}
					public void caseRetStmt(RetStmt stmt) {
						StaticInvokeExpr notifyExpr = Jimple.v().newStaticInvokeExpr(notifyCompletion.makeRef());
						Unit notifyStmt = Jimple.v().newInvokeStmt(notifyExpr);
						units.insertBefore(notifyStmt, stmt);

					}
				});
				u = iter.next();
			}
			return;
		}

	}


}
