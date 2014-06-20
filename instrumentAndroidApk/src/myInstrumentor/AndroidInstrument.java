package myInstrumentor;

import java.util.ArrayList;
import java.util.List;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;


public class AndroidInstrument {
	
	// args: <fullPathToApkFile> <fullPathToAndroidSDKPlatformsFolder> <fullPathToThisSrcFile> -output-format <anySootOutputFormat>
	// we use dex for output format
	public static void main(String[] args) {
		
		// options are provided
		// We need to set -android-jars and classpath before loading the basic classes
		List<String> processDirs = new ArrayList<>();
		processDirs.add(args[0]);
		Options.v().set_src_prec(Options.src_prec_apk);
//		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_android_jars(args[1]);
		Options.v().set_soot_classpath(args[2]); 
		Options.v().set_prepend_classpath(true);
		Options.v().set_process_dir(processDirs);
		Options.v().set_debug(true);

		// required for the analysis of some apks
		soot.SootResolver.v().resolveClass("java.util.Iterator", SootClass.SIGNATURES);
		
		Scene.v().addBasicClass("myScheduler.MyScheduler");
		Scene.v().addBasicClass("myScheduler.ThreadData");
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new MyBodyTransformer()));
        
		// set output format
		String[] sootArgs = new String[2];
		sootArgs[0] = args[3];
		sootArgs[1] = args[4];
        soot.Main.main(sootArgs);
  
	}

}


