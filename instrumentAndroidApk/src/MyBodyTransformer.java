import java.util.ArrayList;
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
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class MyBodyTransformer extends BodyTransformer {

	static SootClass counterClass;
	static SootMethod increaseCounter, reportCounter, readCounter, getThreadInfo;

	static {
		counterClass = Scene.v().loadClassAndSupport("MyScheduler");
		readCounter = counterClass.getMethod("java.lang.String readCounter()");
		increaseCounter = counterClass.getMethod("void increase(int)");
		reportCounter = counterClass.getMethod("void report()");
		getThreadInfo = counterClass.getMethod("void getThreadInfo()");

	}

	@Override
	protected void internalTransform(final Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {

		final PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		final Unit u = iter.next();
		
		if(b.getMethod().getName().equals("run")){

     		SootMethod callScheduler = Scene.v().getMethod("<MyScheduler: void getThreadInfo()>");			
			StaticInvokeExpr callExpr = Jimple.v().newStaticInvokeExpr(callScheduler.makeRef());
			// to see run method calls
			System.out.println(u.toString());
			Unit stmt = Jimple.v().newInvokeStmt(callExpr);
			units.insertAfter(stmt, u);
			
		}

	}

	private static Local addTmpRef(Body body, String name, String clazzName) {
		Local tmpRef = Jimple.v().newLocal(name, RefType.v(clazzName));
		body.getLocals().add(tmpRef);
		return tmpRef;
	}

	private static Local addTmpString(Body body, String name) {
		return addTmpRef(body, name, "java.lang.String");
	}

}

