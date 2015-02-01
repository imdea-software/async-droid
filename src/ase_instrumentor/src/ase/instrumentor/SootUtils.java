package ase.instrumentor;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;

public class SootUtils {
    public static InvokeStmt staticInvocation(SootMethod m, Value arg1, Value arg2, Value arg3) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(), arg1, arg2, arg3));
    }
    
    public static InvokeStmt staticInvocation(SootMethod m) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef()));
    }

    public static InvokeStmt staticInvocation(SootMethod m, Local arg) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(),arg));
    }
    
    public static InvokeStmt staticInvocation(SootMethod m, Value arg) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(),arg));
    }
    
    public static boolean hasParentClass(SootClass clazz, SootClass ancestor) {
        if(clazz == ancestor)
            return true;
        if(clazz.getName().equalsIgnoreCase("java.lang.Object"))
            return false;
        return hasParentClass(clazz.getSuperclass(), ancestor);
    }
}
