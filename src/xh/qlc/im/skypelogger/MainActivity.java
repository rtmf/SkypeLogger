package xh.qlc.im.skypelogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileNotFoundException;

import skypeWebConnector.SkypeWebConnection;

/**
 * A login screen that offers login via email/password.
 */
public class MainActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
   /*private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };*/
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private SkypeWebConnection mSC = null;
	static public PowerManager pm;
	static public PowerManager.WakeLock wl = null;
    static public Thread ct = null;
	static public int navcomticks=100;
    static private int view = 0;

    // UI references.
    private EditText mSkypeIDView;
    private EditText mPasswordView;
    //private View mProgressView;
    //private View mLoginFormView;
    static private SkypeWebConnection sc=null;

    private String name;
    private String passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_login);
    }

    private void loadPrefs() {
        SharedPreferences prefs=prefsFile();
        this.name=prefs.getString("Username","");
        this.passwd=prefs.getString("Password","");
    }

    private void savePrefs() {
        SharedPreferences.Editor prefs=prefsFile().edit();
        prefs.putString("Username",this.name);
        prefs.putString("Password",this.passwd);
        prefs.commit();
    }

    private SharedPreferences prefsFile() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void updateView()
    {
        switch(this.view) {
            case R.layout.activity_login:
                loadPrefs();
                mSkypeIDView = (EditText) findViewById(R.id.name);
                mSkypeIDView.setText(this.name);
                mSkypeIDView.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    public void afterTextChanged(Editable editable) {
                        savePrefs();
                    }
                });
                mPasswordView = (EditText) findViewById(R.id.password);
                mPasswordView.setText(this.passwd);
                mPasswordView.addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    public void afterTextChanged(Editable editable) {
                        name = mSkypeIDView.getText().toString();
                        passwd = mPasswordView.getText().toString();
                        savePrefs();
                    }
                });

                Button loginButton = (Button) findViewById(R.id.button_login);
                loginButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        login();
                    }
                });
                break;
            case R.layout.activity_running:
                Button logoutButton = (Button) findViewById(R.id.button_logout);
                logoutButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logout();
                    }
                });
                break;
        }
    }

    private void setView(int id) {
        this.view=id;
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView(view);
                updateView();
            }
        });
    }

	public void login() {
        name = mSkypeIDView.getText().toString();
        passwd = mPasswordView.getText().toString();
        savePrefs();
        setView(R.layout.activity_logging);

        // Store values at the time of the login attempt.
        new Thread (new Runnable() {
            public void run() {

                sc = new SkypeWebConnection(name,passwd);
                sc.fetchPPFT();
                if (sc.ppft==null) {
                    Log.e("SkypeLogger", "No PPFT");
                    setView(R.layout.activity_login);
                    return;
                }
                sc.fetchT();
                if (sc.magicT==null) {
                    Log.e("SkypeLogger", "No Magic T");
                    setView(R.layout.activity_login);
                    return;
                }
                sc.fetchOauthSkypeToken();
                if (sc.skypeToken==null) {
                    Log.e("SkypeLogger","No Token");
                    setView(R.layout.activity_login);
                    return;
                }
                System.out.println("skypeToken: "+sc.skypeToken);
                sc.leaseClientEndpoint();
                if (!sc.endpointReady) {
                    Log.e("SkypeLogger","Endpoint Not Ready");
                    setView(R.layout.activity_login);
                    return;
                }
                setView(R.layout.activity_running);
                startPolling();

            }
        }).start();
	}

	public final void logout() {
		if (ct != null) {
			ct.interrupt();
		}
		if (wl != null) {
			wl.release();
			wl = null;
		}
		if (sc != null) {
			sc.logout();
		}
        setView(R.layout.activity_login);
	}

	public final void startPolling() {
        if (wl==null)
        {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Datalogger"); // PARTIAL_WAKE_LOCK
            Log.e("PB_IN","lock");
            wl.acquire();
        }
        if (wl.isHeld())
        {
            Log.e("PB_IN","keepalive");
            wl.release();
            System.gc();
            wl.acquire();
        }
		ct = new Thread(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    Log.i("SkypeLogger", "checking for events");
                    try {
                        String events = sc.pollForEvents();
                        try {
                            JSONObject eventInformation = new JSONObject(events);
                            JSONArray eventMessages = eventInformation.optJSONArray("eventMessages");
                            if (eventMessages != null) {
                                for (int i = 0; i < eventMessages.length(); i++) {
                                    JSONObject eventMessage = eventMessages.optJSONObject(i);
                                    if (eventMessage != null) {
                                        if (eventMessage.optString("resourceType", "").contentEquals("NewMessage")) {
                                            JSONObject messageResource = eventMessage.optJSONObject("resource");
                                            if (messageResource != null) {
                                                String messageType = messageResource.optString("messagetype", "");
                                                if (messageType.contentEquals("Text") || messageType.contentEquals("RichText")) {
                                                    final String messageOriginalArrivalTime = messageResource.optString("originalarrivaltime", "nowhen");
                                                    final String messageIMDisplayName = messageResource.optString("imdisplayname", "nobody");
                                                    final String messageContent = messageResource.optString("content", "").trim();
                                                    Log.i("SkypeLogger::MSG", messageOriginalArrivalTime +
                                                            "|" + messageIMDisplayName + ">" + messageContent);
                                                    runOnUiThread(new Runnable() {
                                                                      public void run() {
                                                                          TextView lastMessage = (TextView) findViewById(R.id.text_last_message);
                                                                          lastMessage.setText(messageOriginalArrivalTime +
                                                                                  "|" + messageIMDisplayName + ">" + messageContent);
                                                                      }
                                                                  }
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        login();
                        return;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        ct.start();
	}
}

