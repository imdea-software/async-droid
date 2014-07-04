package my.example.test;

import java.util.ArrayList;

import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

@SuppressWarnings("unchecked")
public class TestApk extends ActivityInstrumentationTestCase2 {

	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "my.example.HelloWorld.MainActivity";
	private static Class launcherActivityClass;
	static {

		try {
			launcherActivityClass = Class
					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public TestApk() throws ClassNotFoundException {
		super(launcherActivityClass);
	}

	private Solo solo;

	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testDisplayBlackBox() {

		ArrayList<View> myViews = solo.getViews();
		
		for(View v: myViews){
			Log.i("Tester", "view: " + v.toString());
			if(v.isClickable())
				//v.performClick();
				v.callOnClick();
			if(v.getWindowVisibility() != View.VISIBLE){
				Log.i("VIEW", "not visible : " + v.toString());
			}else{
				Log.i("VIEW", "is visible : " + v.toString());
			}
		}

	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

}