package xh.qlc.im.skypelogger;

import android.app.Application;
import android.util.Log;

public class SkypeLogger extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("SkypeLogger", "Skypelogger starting up!");
	}
}
