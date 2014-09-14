package myInstrumentor;

import java.util.ArrayList;
import java.util.List;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

public class AndroidInstrument {

    // args: fullPathToApkFile fullPathToAndroidSDKPlatformsFolder
    public static void main(String[] args) {

        List<String> processDirs = new ArrayList<String>();
        processDirs.add(args[0]);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_android_jars(args[1]);
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(processDirs);
        Options.v().set_debug(true);

        soot.SootResolver.v().resolveClass("java.util.Iterator", SootClass.SIGNATURES);

        // Scene.v().addBasicClass("myScheduler.MyScheduler");
        // Scene.v().addBasicClass("myScheduler.ThreadData");
        // Scene.v().addBasicClass("myScheduler.PendingThreads");

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new MyBodyTransformer()));

        // problem when no options passed to soot
        // problem when all options are passed by args too
        // this works fine but to be revisited later
        soot.Main.main(new String[]{"-output-format", "dex"});
    }

}
