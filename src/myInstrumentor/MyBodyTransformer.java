package myInstrumentor;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
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

    static SootClass activityClass;
    static SootMethod getApplicationContextMethod;

    public static void main(String[] args) {
        // args[0]: directory from which to process classes
        // args[1]: path for finding the android.jar file

        PackManager.v().getPack("jtp").add(
            new Transform("jtp.myInstrumenter", new MyBodyTransformer()));

        soot.Main.main(new String[]{
            "-debug",
            "-prepend-classpath",
            "-process-dir", args[0],
            "-android-jars", args[1],
            "-src-prec", "apk",
            "-output-format", "dex"
        });
    }

    static void init() {
        if (schedulerClass != null)
            return;

        schedulerClass = Scene.v().getSootClass("myScheduler.MyScheduler");
        initiateScheduler = schedulerClass.getMethod("void initiateScheduler(android.content.Context)");
        waitMyTurn = schedulerClass.getMethod("void waitMyTurn()");
        notifyScheduler = schedulerClass.getMethod("void notifyScheduler()");
        enterMonitor = schedulerClass.getMethod("void enterMonitor()");
        exitMonitor = schedulerClass.getMethod("void exitMonitor()");

        activityClass = Scene.v().getSootClass("android.app.Activity");
        getApplicationContextMethod = Scene.v().getMethod("<android.content.ContextWrapper: android.content.Context getApplicationContext()>");
    }

    @Override
    protected void internalTransform(final Body b, String phaseName,
            @SuppressWarnings("rawtypes") Map options) {

        init();

        String className = b.getMethod().getDeclaringClass().toString();
        String methodName = b.getMethod().getName();

        if (className.startsWith("myScheduler.")) {

        } else if (className.startsWith("android.support")) {

        } else if (methodName.equals("onCreate")) {
            instrumentOnCreateMethod(b);
        
        // TODO: If parent is pure Thread, then detect loop and instrument
        // beginning and the end of the loop runnables posted to a looper
        // thread by post, postAtFrontOfQueue, postAtTime, postDelayed
        // or sent to UI thread by runOnUIThread

        } else if (methodName.equals("run") ||
                   methodName.equals("handleMessage") ||
                   methodName.equals("onPreExecute") ||
                   methodName.equals("onPostExecute") ||
                   methodName.equals("onProgressUpdate") ||
                   methodName.equals("doInBackground")) {

            System.out.println("===========Instrumenting " + methodName + "..");
            instrumentMethod(b);

        } else if (b.getMethod().getDeclaringClass().getSuperclass().toString().equals("android.app.Activity") &&
                !activityClass.declaresMethod(b.getMethod().getNumberedSubSignature()) ){

            // if a method in an Activity class is defined by user, synchronize 
            // it with the scheduler: (so that UI gives change to other threads 
            // when a UI event is received)
            System.out.println("===========Instrumenting a user defined method: " + b.getMethod());
            instrumentMethod(b);
        }
    }

    void instrumentOnCreateMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        while (iter.hasNext()) {

            iter.next().apply(new AbstractStmtSwitch() {

                public void caseInvokeStmt(InvokeStmt stmt) {
                    if (stmt.getInvokeExpr().getMethod().getSignature().equals("<android.app.Activity: void onCreate(android.os.Bundle)>")) {

                        // get application context:
                        Local context = createLocal(b, "ctx", "android.content.Context");

                        // initiate scheduler with the application context
                        units.insertAfter(staticInvocation(initiateScheduler,context), stmt);
                        units.insertAfter(
                            Jimple.v().newAssignStmt(context,
                                Jimple.v().newVirtualInvokeExpr(b.getThisLocal(),
                                    getApplicationContextMethod.makeRef())),
                            stmt);
                        System.out.println("===========Initiate Scheduler stmt added..");
                    }
                }
            });
        }
    }

    void instrumentMethod(final Body b){
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        if(!iter.hasNext())
            return;
        
        Unit u = iter.next();
        units.insertAfter(staticInvocation(waitMyTurn), u);
        System.out.println("Wait for CPU stmt added..");

        while (iter.hasNext()) {
            u = iter.next();
            u.apply(new AbstractStmtSwitch() {

                public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
                    units.insertBefore(staticInvocation(notifyScheduler), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseReturnStmt(ReturnStmt stmt) {
                    units.insertBefore(staticInvocation(notifyScheduler), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseRetStmt(RetStmt stmt) {
                    units.insertBefore(staticInvocation(notifyScheduler), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseEnterMonitorStmt(EnterMonitorStmt stmt){
                    units.insertAfter(staticInvocation(enterMonitor), stmt);
                    System.out.println("Enter monitor stmt added..");
                }
                
                public void caseExitMonitorStmt(ExitMonitorStmt stmt){
                    units.insertAfter(staticInvocation(exitMonitor), stmt);
                    System.out.println("Exit monitor stmt added..");
                }
            });
        }
    }

    static Local createLocal(Body body, String name, String type) {
        Local l = Jimple.v().newLocal(name, RefType.v(type));
        body.getLocals().add(l);
        return l;
    }

    static InvokeStmt staticInvocation(SootMethod m) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef()));
    }

    static InvokeStmt staticInvocation(SootMethod m, Local arg) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(),arg));
    }
}
