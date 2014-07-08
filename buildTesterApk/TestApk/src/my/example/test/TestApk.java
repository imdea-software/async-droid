package my.example.test;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.robotium.solo.Solo;

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
	private Context context;

	@Override
	protected void setUp() throws Exception {
		context = this.getInstrumentation().getContext();
		//Log.i("CONTEXT", "Target: " + this.getInstrumentation().getTargetContext().getApplicationContext().toString());
		//Log.i("CONTEXT", "Here: " + context.toString());
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testApk() {
		//Context context = getInstrumentation().getTargetContext().getApplicationContext();
		ArrayList<View> myViews = solo.getViews();
		
		for(View v: myViews){
			Log.i("Tester", "view: " + v.toString());
			if(v.isClickable())
				//v.callOnClick();
				solo.clickOnView(v);
		}

	}
	
	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
	
}
