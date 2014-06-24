import java.io.IOException;
import java.lang.ProcessBuilder.*;
import org.python.util.PythonInterpreter;
import org.python.core.*;

public class ApkExecutor {

	private String apkPath;
	private String appPackageName;
	private String mainActivityName;

	public ApkExecutor(String apkPath, String appPackageName,
			String mainActivityName) {
		this.apkPath = apkPath;
		this.appPackageName = appPackageName;
		this.mainActivityName = mainActivityName;

		System.out.println("Apk path: " + this.apkPath);
		System.out.println("Package: " + this.appPackageName);
		System.out.println("Main activity: " + this.mainActivityName);
	}

	public void installApk() {

		System.out.println("Running installer.");
		String installerCommand = "monkeyrunner ./scripts/installScript.py " + apkPath;

		try {
			ProcessBuilder pb = new ProcessBuilder("monkeyRunner", "invokeTestingApk/scripts/installScript.py", apkPath);
//			pb.redirectOutput(Redirect.INHERIT);
//			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			p.waitFor();
			if (p.exitValue() == 1){
				System.out.println("Could not install the application.");
				System.out.println("Some possible problems:");
				System.out.println("1. Your package may not be signed. Please sign it using jarsigner.");
				System.out.println("2. Your package may be already installed.");
				System.out.println("   Uninstall the package by typing the following commands to the command line: ");
				System.out.println("       $ adb uninstall my.example.HelloWorld");
				System.out.println("3. Uninstalling process might not have removed all previous files.");
				System.out.println("   Check the packge name in adb shell by typing the following commands to the command line: ");
				System.out.println("       $ adb shell");
				System.out.println("   (i) If the app is not a default Android app:");
				System.out.println("       root@android:# ls /data/data");
				System.out.println("       If your package is listed, remove it:");
				System.out.println("       root@android:# rm -R /data/data/my.example.HelloWorld ");
				System.out.println("   (ii) If the app is a default Android app:");
				System.out.println("       root@android:# pm list packages");
				System.out.println("   If your package is listed, enter the following commands:");
				System.out.println("       root@android:# remount");
				System.out.println("       root@android:# rm /system/app/PackageName.apk");
				System.out.println("Otherwise try installing on adb shell: $ adb install <FullPathToYourApkFile/Name.apk>");
				System.exit(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void invokeActivity() {
		System.out.println("Invoking activity.");
		String invokerCommand = "monkeyrunner invokeTestingApk/scripts/invokeScript.py " + appPackageName + " " + mainActivityName;

		try {
			Runtime.getRuntime().exec(invokerCommand).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void executeInputs() {
		System.out.println("Executing inputs.");
		try {
			Runtime.getRuntime().exec("python invokeTestingApk/scripts/inputScript.py").waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		if (args.length != 6) {
			System.out.println("Please enter the following arguments:");
			System.out
					.println("<fullPathToInstrumentedApkFile> <appPackageName> <appMainActivity> <Install? (Y/N)> <Invoke? (Y/N)> <Test? (Y/N)>");
			System.out
					.println("Ex: \"/Users/burcu/Desktop/HelloWorld.apk\"  \"my.example.HelloWorld\" \"my.example.HelloWorld.MainActivity\" yes yes yes");
			return;
		}
		ApkExecutor ai = new ApkExecutor(args[0], args[1], args[2]);
		
		if (args[3].equalsIgnoreCase("yes") || args[3].equalsIgnoreCase("y"))
			ai.installApk();
		if (args[4].equalsIgnoreCase("yes") || args[4].equalsIgnoreCase("y"))
			ai.invokeActivity();
		if (args[5].equalsIgnoreCase("yes") || args[5].equalsIgnoreCase("y"))
			ai.executeInputs();

	}

}
