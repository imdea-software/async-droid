import java.io.IOException;
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
		System.out.println("Main acticity: " + this.mainActivityName);
	}

	public void installApk() {

		String installerCommand = "monkeyrunner ./scripts/installScript.py "
				+ apkPath;

		try {
			Runtime.getRuntime().exec(installerCommand).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void invokeActivity() {

		String invokerCommand = "monkeyrunner ./scripts/invokeScript.py "
				+ appPackageName + " " + mainActivityName;

		try {
			Runtime.getRuntime().exec(invokerCommand).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void executeInputs() {
		try {
			Runtime.getRuntime().exec("python ./scripts/inputScript.py")
					.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Please enter the followong arguments:");
			System.out
					.println("<fullPathToInstrumentedApkFile> <appPackageName> <appMainActivity>");
			return;
		}
		ApkExecutor ai = new ApkExecutor(args[0], args[1], args[2]);
		ai.installApk();
		ai.invokeActivity();
		ai.executeInputs();

	}

}
