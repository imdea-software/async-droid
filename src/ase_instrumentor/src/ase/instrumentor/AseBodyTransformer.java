package ase.instrumentor;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.JIdentityStmt;

public class AseBodyTransformer extends BodyTransformer {

    private static SootClass aseTestBridgeClass;
    private static SootMethod initiateTesting, waitForDispatch, notifyDispatcher, enterMonitor, exitMonitor;
    private static SootMethod setActivityViewTraverser, setFragmentViewTraverser, setAdapterItemViewTraverser; 
    private static SootMethod setActionBarMenu, setRecorderForActionBar, setRecorderForActionBarTab;
    private static SootMethod setRecorderForItemClick, /*setRecorderForItemSelected,*/ setRecorderForTouchEvent;

    public static void main(String[] args) {
        // args[0]: directory from which to process classes
        // args[1]: path for finding the android.jar file

        PackManager.v().getPack("jtp").add(
            new Transform("jtp.myInstrumenter", new AseBodyTransformer()));

        soot.Main.main(new String[]{
            "-debug",
            "-prepend-classpath",
            "-process-dir", args[0],
            "-android-jars", args[1],
            "-src-prec", "apk",
            "-output-format", "dex",
            "-allow-phantom-refs"
        });
    }

    private void init() {
        if (aseTestBridgeClass != null)
            return;

        aseTestBridgeClass = Scene.v().getSootClass("ase.AseTestBridge");
        initiateTesting = aseTestBridgeClass.getMethod("void initiateTesting(android.content.Context)");
        waitForDispatch = aseTestBridgeClass.getMethod("void waitForDispatch()");
        notifyDispatcher = aseTestBridgeClass.getMethod("void notifyDispatcher()");
        enterMonitor = aseTestBridgeClass.getMethod("void enterMonitor()");
        exitMonitor = aseTestBridgeClass.getMethod("void exitMonitor()");
        setActivityViewTraverser = aseTestBridgeClass.getMethod("void setActivityViewTraverser(android.app.Activity)");
        setFragmentViewTraverser = aseTestBridgeClass.getMethod("void setFragmentViewTraverser(android.view.View)");
        setActionBarMenu = aseTestBridgeClass.getMethod("void setActionBarMenu(android.view.Menu)");
        setRecorderForActionBar = aseTestBridgeClass.getMethod("void setRecorderForActionBar(android.view.MenuItem)");
        setAdapterItemViewTraverser = aseTestBridgeClass.getMethod("void setAdapterItemViewTraverser(android.view.View,android.view.ViewGroup,int)");
        setRecorderForActionBarTab = aseTestBridgeClass.getMethod("void setRecorderForActionBarTab(java.lang.Object)");
        setRecorderForItemClick = aseTestBridgeClass.getMethod("void setRecorderForItemClick(android.widget.AdapterView,android.view.View,int,long)");
        // setRecorderForItemSelected = aseTestBridgeClass.getMethod("void setRecorderForItemSelected(android.widget.AdapterView,android.view.View,int,long)");
        setRecorderForTouchEvent = aseTestBridgeClass.getMethod("void setRecorderForTouchEvent(android.view.MotionEvent)");
    }

    @Override
    protected void internalTransform(final Body b, String phaseName,
            @SuppressWarnings("rawtypes") Map options) {

        init();

        String className = b.getMethod().getDeclaringClass().toString();
        String methodName = b.getMethod().getName();

        SootClass clazz = b.getMethod().getDeclaringClass();
        SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
        SootClass applicationClass = Scene.v().getSootClass("android.app.Application");
        SootClass adapterClass = Scene.v().getSootClass("android.widget.BaseAdapter");
        
        // TODO take the library package names and do not instrument these packages
        
        if (className.startsWith("ase.")) {
            // skip
        } else if (className.startsWith("android.support")) {
            // skip
        } else if (className.startsWith("org.apache")) {
            // skip
        } else if (className.startsWith("org.droidparts")) {
            // skip
        } else if (className.startsWith("org.joda")) {
            // skip
        } else if (className.startsWith("org.osmdroid")) {
            // skip
        } else if (className.startsWith("uk.co.senab")) {
            // skip
        } else if (className.startsWith("com.viewpageindicator")) {
            // skip
        } else if (className.startsWith("com.google")) {
            // skip
        } else if (className.startsWith("org.acra")) {
            // skip
        } else if (className.startsWith("org.xml")) {
            // skip
        } else if (className.startsWith("org.json")) {
            // skip
        } else if (className.startsWith("com.google.gson")) {
            // skip
        } else if (className.startsWith("com.actionbarsherlock")) {
            // skip
        } else if (methodName.equals("onCreate")) {
            if (SootUtils.hasParentClass(clazz, activityClass))
                instrumentOnCreateMethod(b, true); // instrument for UI traversing
            else if (SootUtils.hasParentClass(clazz, applicationClass))
                instrumentOnCreateMethod(b, false);

        } else if (methodName.equals("onCreateView")) {
            instrumentOnCreateViewMethod(b);
            
        } else if (methodName.equals("onViewCreated")) {
            instrumentOnViewCreatedMethod(b);

        } else if (methodName.equals("onCreateOptionsMenu")) {  
            instrumentOnCreateOptionsMenu(b);

        } else if (methodName.equals("onOptionsItemSelected")) { 
            instrumentOnOptionsItemSelected(b);
            
        } else if (methodName.equals("onTabSelected")) {
            instrumentOnTabSelected(b);
        
        } else if (methodName.equals("onItemClick")) {  
            instrumentOnItemClickMethod(b);
            
        //} else if (methodName.equals("onItemSelected")) {  
        //    instrumentOnItemSelectedMethod(b);
           
        } else if (methodName.equals("onTouchEvent")) {  
            instrumentOnTouchEventMethod(b);
            
        } else if (methodName.equals("getView") && SootUtils.hasParentClass(clazz, adapterClass)) { 
            instrumentGetViewMethod(b);
            
        } else if (methodName.equals("doInBackground") ||
                   methodName.equals("onPostExecute") ||
                   methodName.equals("onProgressUpdate") ||
                   methodName.equals("onCancelled") ||
                   methodName.equals("run") ||
                   methodName.equals("handleMessage") ||
                   methodName.equals("handleIntent")) {

            System.out.println("===========Instrumenting " + methodName + "..");
            instrumentMethod(b);
        }
    }
    
    /**
     * Adds a statement to initiate ase scheduler
     * If instrumentUI is true, adds a call to setActivityViewTraverser
     * to set root view of the app and traverse the views in the activity layout
     */
    private void instrumentOnCreateMethod(final Body b, boolean instrumentUI) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        // initiate scheduler as the first statement
        // since the latter statements may call async tasks
        Stmt stmt = ((JimpleBody) b).getFirstNonIdentityStmt();
        units.insertBefore(SootUtils.staticInvocation(initiateTesting, b.getThisLocal()), stmt);
        System.out.println("===========Initiate Scheduler stmt added..");

        // if it is an Activity onCreate, than instrument for UI traversal
        if(instrumentUI) {
            while (iter.hasNext()) {
                Unit u = iter.next();
                u.apply(new AbstractStmtSwitch() {
                    
                    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
                        units.insertBefore(SootUtils.staticInvocation(setActivityViewTraverser, b.getThisLocal()), stmt);
                        System.out.println("===========ActivityViewTraversal stmt added..");
                    }
            });
            }
        }
    }

    /**
     * Adds call to setFragmentViewTraverser to traverse the views in fragment view
     * to enable record/replay
     */
    private void instrumentOnCreateViewMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnStmt(ReturnStmt stmt) {                   
                    //read to-be-returned value
                    Value returnedView = stmt.getOpBox().getValue();
                    // stmt.getReturnExpr().getArg(0);
                    // insert call to setFragmentViewTraverser
                    units.insertBefore(SootUtils.staticInvocation(setFragmentViewTraverser, returnedView), stmt);
                    System.out.println("===========FragmentViewTraversal stmt added..");
                }
            });
        }
    }
    
    /**
     * Adds call to setFragmentViewTraverser to traverse the views in fragment view
     * to enable record/replay
     */
    private void instrumentOnViewCreatedMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        iter.next(); // the identity statement for the method
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); // the identity statement for the parameter
        final Value paramView = stmt.getLeftOp();

        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {               
                public void caseReturnStmt(ReturnStmt stmt) {                                     
                    units.insertBefore(SootUtils.staticInvocation(setFragmentViewTraverser, paramView), stmt);
                    System.out.println("===========FragmentViewTraversal stmt added..");
                }
            });
        }
    }

    /**
     * Adds call to getView of an AdapterView to traverse the views in a list item
     * to enable record/replay
     */
    private void instrumentGetViewMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();
        // method: public View getView(final int position, View view, final ViewGroup parent)
        
        iter.next(); // the identity statement for the method
        // the identity statement for the first parameter - position
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); 
        final Value posParam = stmt.getLeftOp();    
        // the identity statement for the second parameter - view
        stmt = (JIdentityStmt) iter.next(); 
        final Value viewParam = stmt.getLeftOp();     
        // the identity statement for the third parameter - parent
        stmt = (JIdentityStmt) iter.next(); 
        final Value parentParam = stmt.getLeftOp();
        
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnStmt(ReturnStmt stmt) {               
                    units.insertBefore(SootUtils.staticInvocation(setAdapterItemViewTraverser, viewParam, parentParam, posParam), stmt);
                    System.out.println("===========ItemViewTraversal stmt added..");
                }
            });
        }
    }
    
    /**
     * Adds call to getView of an AdapterView to traverse the views in a list item
     * to enable record/replay
     */
    private void instrumentOnItemClickMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();
        // method: public void onItemClick(AdapterView adapter, View view, int position, long index)
        
        iter.next(); // the identity statement for the method
        // the identity statement for the first parameter - adapter
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); 
        final Value adapterParam = stmt.getLeftOp();    
        // the identity statement for the second parameter - view
        stmt = (JIdentityStmt) iter.next(); 
        final Value viewParam = stmt.getLeftOp(); 
        // the identity statement for the third parameter - position
        stmt = (JIdentityStmt) iter.next(); 
        final Value posParam = stmt.getLeftOp();
        // the identity statement for the fourth parameter - index
        stmt = (JIdentityStmt) iter.next(); 
        final Value indexParam = stmt.getLeftOp();
        
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnVoidStmt(ReturnVoidStmt stmt) {               
                    units.insertBefore(SootUtils.staticInvocation(setRecorderForItemClick, adapterParam, viewParam, posParam, indexParam), stmt);
                    System.out.println("===========Recorder for OnItemClick is added..");
                }
            });
        }
    }
    
    /**
     * Adds call to getView of an AdapterView to traverse the views in a list item
     * to enable record/replay
     */
    /*private void instrumentOnItemSelectedMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();
        // method: public void onItemClick(AdapterView adapter, View view, int position, long index)
        
        iter.next(); // the identity statement for the method
        // the identity statement for the first parameter - adapter
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); 
        final Value adapterParam = stmt.getLeftOp();    
        // the identity statement for the second parameter - view
        stmt = (JIdentityStmt) iter.next(); 
        final Value viewParam = stmt.getLeftOp(); 
        // the identity statement for the third parameter - position
        stmt = (JIdentityStmt) iter.next(); 
        final Value posParam = stmt.getLeftOp();
        // the identity statement for the fourth parameter - index
        stmt = (JIdentityStmt) iter.next(); 
        final Value indexParam = stmt.getLeftOp();
        
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnVoidStmt(ReturnVoidStmt stmt) {               
                    units.insertBefore(SootUtils.staticInvocation(setRecorderForItemSelected, adapterParam, viewParam, posParam, indexParam), stmt);
                    System.out.println("===========Recorder for OnItemSelected is added..");
                }
            });
        }
    }*/
    
    /**
     * Adds call to getView of an AdapterView to traverse the views in a list item
     * to enable record/replay
     */
    private void instrumentOnTouchEventMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();
        // method: public void onMotionEvent(MotionEvent motionEvent)
        
        iter.next(); // the identity statement for the method
        // the identity statement for the first parameter - motionEvent
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); 
        final Value eventParam = stmt.getLeftOp();    
        
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnStmt(ReturnStmt stmt) {               
                    units.insertBefore(SootUtils.staticInvocation(setRecorderForTouchEvent, eventParam), stmt);
                    
                    //EqExpr eq = Jimple.v().newEqExpr(lss.getKey(), IntConstant.v(i));
                    //Stmt ifStmt = Jimple.v().newIfStmt(eq, pos);
                    
                    //Value op = Jimple.v().newVariableBox(value)  Value(true);
                    //ReturnStmt retStmt = Jimple.v().newReturnStmt(op);
                    //units.add(Jimple.v().newReturnVoidStmt());
                    //units.insertBefore(SootUtils.staticInvocation(setRecorderForTouchEvent, eventParam), stmt); 
                    System.out.println("===========Recorder for OnTouchEvent is added..");
                }
            });
        }
    }
    
    /**
     * Execution of that method is controlled by ase.scheduler
     * (its code is executed in between waitMyTurn() and notifyScheduler())
     */
    private void instrumentMethod(final Body b){
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        if(!iter.hasNext())
            return;
        
        Unit u = iter.next();
        units.insertAfter(SootUtils.staticInvocation(waitForDispatch), u);
        System.out.println("Wait for CPU stmt added..");

        while (iter.hasNext()) {
            u = iter.next();
            u.apply(new AbstractStmtSwitch() {

                public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(notifyDispatcher), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseReturnStmt(ReturnStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(notifyDispatcher), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseRetStmt(RetStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(notifyDispatcher), stmt);
                    System.out.println("Release CPU stmt added..");
                }
                
                public void caseEnterMonitorStmt(EnterMonitorStmt stmt){
                    units.insertAfter(SootUtils.staticInvocation(enterMonitor), stmt);
                    System.out.println("Enter monitor stmt added..");
                }
                
                public void caseExitMonitorStmt(ExitMonitorStmt stmt){
                    units.insertAfter(SootUtils.staticInvocation(exitMonitor), stmt);
                    System.out.println("Exit monitor stmt added..");
                }
            });
        }
    }
    
    private void instrumentOnCreateOptionsMenu(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        iter.next(); // the identity statement for the method
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); // the identity statement for the parameter
        Value param = stmt.getLeftOp();

        units.insertAfter(SootUtils.staticInvocation(setActionBarMenu, param), stmt);
        System.out.println("===========Action bar menu is set..");
    }
    
    private void instrumentOnOptionsItemSelected(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        iter.next(); // the identity statement for the method
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); // the identity statement for the parameter
        Value param = stmt.getLeftOp();

        units.insertAfter(SootUtils.staticInvocation(setRecorderForActionBar, param), stmt);
        System.out.println("===========Action bar recorder is added..");
    }

    private void instrumentOnTabSelected(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        iter.next(); // the identity statement for the method
        JIdentityStmt stmt = (JIdentityStmt) iter.next(); // the identity statement for the parameter
        Value tabParam = stmt.getLeftOp();
        
        units.insertAfter(SootUtils.staticInvocation(setRecorderForActionBarTab, tabParam), stmt);
        System.out.println("===========Action bar tab selection recorder is added..");
    }
}
