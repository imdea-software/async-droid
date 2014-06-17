import java.util.ArrayList;
import java.util.List;

import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.options.Options;


public class AndroidInstrument {
	
	// args: fullPathToApkFile fullPathToAndroidSDKPlatformsFolder fullPathToThisSrcFile 
	public static void main(String[] args) {
		
		List<String> processDirs = new ArrayList<>();
		processDirs.add(args[0]);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(args[1]);
		Options.v().set_soot_classpath(args[2]); 
		Options.v().set_prepend_classpath(true);
		Options.v().set_process_dir(processDirs);

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new MyBodyTransformer()));

        // problem when no options passed to soot
        // problem when all options are passed by args too
        // this works fine but to be revisited later
        String[] sootArgs = new String[2];
        sootArgs[0] = "-process-dir";
		sootArgs[1] = args[0];
		System.out.println(sootArgs[0]);
        soot.Main.main(sootArgs);
		

	}


}
