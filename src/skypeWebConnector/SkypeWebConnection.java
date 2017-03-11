package skypeWebConnector;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.TimeZone;


public class SkypeWebConnection {
	public String username;
	public String password;
	public String currhost;
	public String pie;
	public String etm;
	public String ppft;
	public String msprequ_cookie;
	public String mspok_cookie;
	public String cktst_cookie;
	public String magicT;
	public String skypeToken;
	public String registrationToken;
	public String endpointId;
	public Long registrationTokenExpirationTime;
	public String subscriptionPath;
	public String captchaImageURL;
	public String captchaResponseInputName;
	public String cookies;
	public String presenceStatus;
	public Boolean endpointReady;
	public URLEncoder enc;
	
	private final String SKYPEWEB_LOCKANDKEY_APPID="msmsgs@msnmsgr.com";
	private final String SKYPEWEB_LOCKANDKEY_SECRET="Q1P7W2E4J9R8U3S5";

//	private final String SKYPEWEB_CONTACTS_HOST="api.skype.com";                               
//	private final String SKYPEWEB_NEW_CONTACTS_HOST="contacts.skype.com";                      
	private final String SKYPEWEB_DEFAULT_MESSAGES_HOST="client-s.gateway.messenger.live.com"; 
//	private final String SKYPEWEB_LOGIN_HOST="login.skype.com";                                
//	private final String SKYPEWEB_VIDEOMAIL_HOST="vm.skype.com";

	private final String SKYPEWEB_CLIENTINFO_NAME="swx-skype.com";
	private final String SKYPEWEB_CLIENTINFO_VERSION="908/1.13.79";
	private final String SKYPEWEB_CLIENT_INFO_STRING=
			"os=Windows; osVer=8.1; proc=Win32; lcid=en-us; deviceType=1; country=n/a; clientName="+
					SKYPEWEB_CLIENTINFO_NAME+"; clientVer="+SKYPEWEB_CLIENTINFO_VERSION;
	private final String SKYPEWEB_CLIENT_ID_MAGICK="578134";
	private final long SKYPEWEB_MAGICK_NUMBER=0x0E79A9C1;
	
	private final String SKYPEWEB_RESOURCE_SUBSCRIPTION_REQUESTS=
			"{\"interestedResources\":[\"/v1/users/ME/conversations/ALL/properties\",\"/v1/users/ME/conversations/ALL/messages\",\"/v1/users/ME/contacts/ALL\",\"/v1/threads/ALL\"],\"template\":\"raw\",\"channelType\":\"httpLongPoll\"}";

	public SkypeWebURLResponse resp = new SkypeWebURLResponse();
	public SkypeWebURLHeaders heads = new SkypeWebURLHeaders();
	
	public SkypeWebConnection(String username, String password)
	{
		this.username=username;
		this.password=password;
		resetConnection();
	}
	public void resetConnection()
	{
		this.magicT=null;
		this.ppft=null;
		this.cktst_cookie=null;
		this.mspok_cookie=null;
		this.msprequ_cookie=null;
		this.pie=null;
		this.etm=null;
		this.skypeToken=null;
		this.currhost=SKYPEWEB_DEFAULT_MESSAGES_HOST;
		this.endpointId=null;
		this.registrationToken=null;
		this.registrationTokenExpirationTime=-1L;
		this.subscriptionPath=null;
		this.cookies=null;
		this.presenceStatus="Online";
		this.endpointReady=false;
	}
	public SkypeWebURLResponse fetchURLPlain(String host, String method, String path, String headers, String body)
	{
		SkypeWebURLHeaders hmap = new SkypeWebURLHeaders(headers);
		return this.fetchURLPlain(host, method, path, hmap, body);
	}
	public SkypeWebURLResponse fetchURLPlain(String host, String method, String path, SkypeWebURLHeaders headers, String body)
	{
		return this.fetchURLFull("http",host,method,path,headers,body);
	}
	public SkypeWebURLResponse fetchURLSSL(String host, String method, String path, String headers, String body)
	{
		SkypeWebURLHeaders hmap = new SkypeWebURLHeaders(headers);
		return this.fetchURLSSL(host, method, path, hmap, body);
	}
	public SkypeWebURLResponse fetchURLSSL(String host, String method, String path, SkypeWebURLHeaders headers, String body)
	{
		return this.fetchURLFull("https",host,method,path,headers,body);
	}
	public SkypeWebURLResponse fetchURLFull(String proto, String host, String method, String path, SkypeWebURLHeaders headers, String body)
	{
		URL url;
		try {
			url = new URL(proto,host,path);
			return this.fetchURL(method, url, headers, body);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public SkypeWebURLResponse fetchURL(String method, String urlstr, SkypeWebURLHeaders headers, String body)
	{
		URL url;
		try {
			url = new URL(urlstr);
			return this.fetchURL(method, url, headers, body);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public SkypeWebURLResponse fetchURL(String method, URL url, SkypeWebURLHeaders headers, String body)
	{
		/*
		 * Port will always be default
		 * HTTP ver is always 1.0
		 * This will ALWAYS be an SSL fetch
		 * the backend will modify a few headers, let's see which
		 */
		HttpURLConnection con = null;
		StringBuilder bodyBuilder = new StringBuilder();

		try
		{
			headers.setIfAbsent("Host", url.getHost());
			SkypeWebURLResponse resp = new SkypeWebURLResponse();
			con = (HttpURLConnection) url.openConnection();
			for (String hnam: headers.keySet())
			{
				if (hnam==null) continue;
				Boolean first=true;
				for (String hval: headers.get(hnam))
				{
					if (first)
					{
						con.setRequestProperty(hnam, hval);
						first=false;
					}
					else
					{
						con.addRequestProperty(hnam, hval);
					}
				}
			}
			con.setDoInput(true);
			con.setConnectTimeout(2000);
			con.setInstanceFollowRedirects(false);
			con.setDefaultUseCaches(false);
			con.setUseCaches(false);
			con.setRequestMethod(method);

			Boolean hasOutputStream=(!method.matches("GET"));
			if (hasOutputStream)
			{
				headers.setIfAbsent("Content-Length",Integer.toString(body.getBytes().length));
				con.setDoOutput(true);
				OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
				osw.write(body);
				osw.flush();
				osw.close();
			}
			BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String responseLine;
			while ((responseLine = isr.readLine()) != null)
			{
				bodyBuilder.append(responseLine);
				bodyBuilder.append("\n"); // TODO - verify this is correct
			}
			isr.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				System.out.printf("[%d] %s\n", con.getResponseCode(), con.getResponseMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		resp.body=bodyBuilder.toString();
		resp.headers=new SkypeWebURLHeaders(con.getHeaderFields());
		resp.response=resp.headers.headers()+"\r\n"+resp.body;
		try
		{
			resp.statusCode=con.getResponseCode();
			resp.statusMessage=con.getResponseMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}
    public void clearHeaders()
    {
    	heads.clear();
    	heads.append("BehaviourOverride","redirectAs404");
		heads.append("Accept", "*/*");
		heads.append("Connection", "close");
    }
	
    public String msAuthHash(String base) {
    	// XXX TODO this is REALLY UGLY beyond just its actual nature
    	try {
            int i;
            StringBuffer data=new StringBuffer();
            data.append(base);
            data.append(SKYPEWEB_LOCKANDKEY_SECRET);
            MessageDigest digest;
            byte[] sha256;
                digest = MessageDigest.getInstance("SHA-256");
                sha256 = digest.digest(data.toString().getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for (i = 0; i < 16; i++) {
                String hex = Integer.toHexString(0xff & sha256[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            int[] shai = new int[4];
            for (i = 0; i < 4 ; i++)
            {
                String iString=hexString.substring(i*8,(i+1)*8);
                shai[i]=(int)(Long.parseLong(iString,16) & 0x7fffffff);
            }
            data = new StringBuffer();
            hexString = new StringBuffer();
            data.append(base);
            data.append(SKYPEWEB_LOCKANDKEY_APPID);
            for (i = 0; i < (8-(data.toString().getBytes("UTF-8").length%8)); i++) {
                data.append("0");
            }
            byte[] dbyt=data.toString().getBytes("UTF-8");
            int[] chli = new int[dbyt.length/8];
            for (i = 0; i < dbyt.length; i++) {
                String hex = Integer.toHexString(0xff & dbyt[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            for (i = 0; i < chli.length ; i++) {
                String iString=hexString.substring(i * 8, (i + 1) * 8);
                chli[i]=(int)(Long.parseLong(iString,16) & 0x7fffffff)
                ;
            }
            long high=0;
            long low=0;
            long temp=0;
            for (i = 0; i < chli.length-1 ; i+=2) {
                temp=chli[i];
                temp=(SKYPEWEB_MAGICK_NUMBER*temp) % 0x7fffffff;
                temp+=high;
                temp=shai[0]*temp+shai[1];
                temp%=0x7fffffff;
                high=chli[i+1];
                high=(high*temp) % 0x7fffffff;
                high=shai[2]*high+shai[3];
                high%=0x7fffffff;
                low+=high+temp;
            }
            high=(high+shai[1])%0x7fffffff;
            low=(low+shai[3])%0x7fffffff;
            //maybe little-endian?
            shai[0]^=low;
            shai[1]^=high;
            shai[2]^=low;
            shai[3]^=high;
            hexString=new StringBuffer();
            for (i=0; i<4; i++) {
                String hex=Integer.toHexString(shai[i]);
                for (int j=0; j<(4-hex.length());j++)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public String subSeqIfExist(String from, String start, String end)
    {
    	int a=0;
    	String b=from;
    	if (start != null)
    	{
    		a=from.indexOf(start);
    		if (a<0) return null;
    		b=from.substring(a+start.length());
    	}
    	int c=b.length();
    	if (end != null)
    	{
    		c=b.indexOf(end);
    		if (c<0) return null;
    	}
    	return b.substring(0,c);
    }    
    public String getTimeZoneField()
    {
        int z= TimeZone.getDefault().getRawOffset();
        String zf="+|";
        if (z<0) {
            z=-z;
            zf="-|";
        }
        z/=60000;
        zf+=Integer.toString(z/60)+"|"+Integer.toString(z%60);
        return zf;
    }
    public Long timeInSeconds()
    {
    	return System.currentTimeMillis()/1000;
    }
    public String timeStringInSeconds()
    {
    	return timeInSeconds().toString();
    }
	public void fetchPPFT() {
		clearHeaders();
		heads.clear();
		//System.out.println(heads.headers());
		resp=fetchURLSSL("login.skype.com","GET","/login/oauth/microsoft?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com",heads,"");
		if (resp.headers==null) return;
		if (resp.headers.containsKey("Location")) {
			String url = resp.headers.get("Location").get(0);
			String host = subSeqIfExist(url,"https://","/");
			String path = url.substring(host.length()+9);
			//System.out.println("Host: "+host+"Path: "+path);
			resp=fetchURLSSL(host,"GET",path,heads,"");
		}
		msprequ_cookie=subSeqIfExist(resp.response,"Set-Cookie: MSPRequ=",";");
		mspok_cookie=subSeqIfExist(resp.response,"Set-Cookie: MSPOK=",";");
		ppft = subSeqIfExist(resp.body,"name=\"PPFT\" id=\"i0327\" value=\"", "\"");
		cktst_cookie = "G"+System.currentTimeMillis();
	}
	public void fetchT()
	{
		clearHeaders();
		heads.append("Cookie", "MSPRequ="+msprequ_cookie+";MSPOK="+mspok_cookie+";CkTst="+cktst_cookie+";");
		String request="login="+username+"&passwd="+password+"&PPFT="+ppft+"&";
		resp=fetchURLSSL("login.live.com","POST",
				"/ppsecure/post.srf?wa=wsignin1.0&wp=MBI_SSL&wreply=https%3A%2F%2Flw.skype.com%2Flogin%2Foauth%2Fproxy%3Fclient_id%3D578134%26redirect_uri%3Dhttps%253A%252F%252Fweb.skype.com%252F%26site_name%3Dlw.skype.com",heads,request);
		magicT=subSeqIfExist(resp.body,"=\"t\" value=\"","\"");
	}
	public void fetchOauthSkypeToken()
	{
		clearHeaders();
		String request="t="+enc.encode(magicT)+"&site_name=lw.skype.com&oauthPartner=999&client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com";
		resp=fetchURLSSL("login.skype.com","POST","/login/microsoft?client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com",heads,request);
		//System.out.println(resp.response);
		skypeToken = subSeqIfExist(resp.body,"=\"skypetoken\" value=\"", "\"");
	}
/*    public void fetchPIEAndETM()
    {
    	clearHeaders();
		heads.append("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
    	resp=fetchURLSSL("login.skype.com","GET","/login?method=skype&client_id=578134&redirect_uri=https%3A%2F%2Fweb.skype.com&username="+username,"","");
    	System.out.println(resp.body);
		pie = subSeqIfExist(resp.body,"name=\"pie\" id=\"pie\" value=\"","\"");
    	etm = subSeqIfExist(resp.body,"name=\"etm\" id=\"etm\" value=\"","\"");
    }
    public Boolean fetchSkypeTokenOrCaptcha()
    {
    	//returns true if we found a captcha to solve
    	return this.fetchSkypeTokenOrCaptchaWithCaptcha(null);
    }
    public Boolean fetchSkypeTokenOrCaptchaWithCaptcha(String captchaResponse)
    {
    	//returns true if we found a captcha to solve
    	clearHeaders();
		heads.append("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
    	heads.append("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
    	String request= "username="+username+"&password="+password+"&timezone_field="+
    					getTimeZoneField()+"&pie="+pie+"&etm="+etm+"&js_time="+
    					System.currentTimeMillis()+"&client_id="+SKYPEWEB_CLIENT_ID_MAGICK+
    					"&redirect_uri=https://web.skype.com/";
    	if (captchaResponse!=null && captchaImageURL!=null && captchaResponseInputName!=null)
    	{
    		request=request+"&captcha_provider=Hip&"+
    					"&fid="+subSeqIfExist(captchaImageURL,"&fid=","&id")+
    					"&hip_token="+subSeqIfExist(captchaImageURL,"?hid=","&fid")+
    					"&hip_type="+subSeqIfExist(captchaImageURL,"&type=","&hdid")+
    					"&hip_solution="+captchaResponse;
    	}
		cookies=null;
		System.out.println("Fetching token...");
    	resp=fetchURLSSL("login.skype.com","POST","/login?client_id="+SKYPEWEB_CLIENT_ID_MAGICK+
    					"&redirect_uri=https%3A%2F%2Fweb.skype.com",heads,request);
    	System.out.println(resp.body);
    	skypeToken = subSeqIfExist(resp.body,"=\"skypetoken\" value=\"", "\"");
    	if (skypeToken==null)
    	{
    		String hipURL = subSeqIfExist(resp.body,"var skypeHipUrl = \"","\";");
    		if (hipURL!=null)
    		{
    			clearHeaders();
    			resp=fetchURL("GET",hipURL,heads,"");
    			captchaResponseInputName=subSeqIfExist(resp.body,"ispSolutionElement:'","',");
    			captchaImageURL=subSeqIfExist(resp.body,"imageurl:'","',");
    			return true;
    		}
    	}
    	else
    	{
    		cookies=null;
    		if (resp.headers.containsKey("Set-Cookie"))
    		{
    			StringBuilder cookieBaker = new StringBuilder();
    			for (String cookie: resp.headers.get("Set-Cookie"))
    			{
    				cookieBaker.append(cookie.substring(0,cookie.indexOf(";")+1));
    			}
    			cookies=cookieBaker.toString();
    		}
    	}
    	return false;
    }*/
    public void fetchNewEndpoint()
    {
    	clearHeaders();
    	endpointReady = false;
    	registrationToken = null;
    	endpointId = null;
    	registrationTokenExpirationTime = -1L;
    	String t = timeStringInSeconds();
    	heads.append("LockAndKey","appid="+SKYPEWEB_LOCKANDKEY_APPID+"; time="+
    				t+"; lockAndKeyResponse="+msAuthHash(t));
    	heads.append("ClientInfo",SKYPEWEB_CLIENT_INFO_STRING);
    	heads.append("Content-Type","application/json");
    	heads.append("Authentication", "skypetoken="+skypeToken);
    	resp=fetchURLSSL(currhost,"POST","/v1/users/ME/endpoints",heads,"{}");
    	if (resp.headers.containsKey("Set-RegistrationToken"))
    	{
    		String registrationTokenInfoHeader = resp.headers.get("Set-RegistrationToken").get(0);
    		registrationToken = subSeqIfExist(registrationTokenInfoHeader,null,";");
    		endpointId = subSeqIfExist(registrationTokenInfoHeader,"endpointId=",null);
    		registrationTokenExpirationTime = Long.parseLong(subSeqIfExist(registrationTokenInfoHeader,"expires=",";"));
    	}
    	if (resp.headers.containsKey("Location"))
    	{
    		try {
				URL url=new URL(resp.headers.get("Location").get(0));
				String newhost=url.getHost();
				if (!currhost.contentEquals(newhost))
				{
		    		//we've been load-balanced.
					currhost=newhost;
					fetchNewEndpoint();
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    public void commonHeaders()
    {
    	renewLeaseIfExpired();
    	heads.append("RegistrationToken", registrationToken);
    	heads.append("Accept-Language", "en-US, en, C");
    	heads.append("Referer", "https://web.skype.com/main");
    	heads.append("ClientInfo",SKYPEWEB_CLIENT_INFO_STRING);
    	heads.append("Accept", "application/json; ver=1.0;");
    }
    public void contentHeaderJSON()
    {
    	heads.append("Content-Type","application/json");    	
    }
    public void contentHeaderForm()
    {
    	heads.append("Content-Type", "application/x-www-form-urlencoded");    	
    }
    public void subscribeEndpoint()

    {
    	clearHeaders();
    	commonHeaders();
    	contentHeaderJSON();
    	this.subscriptionPath=null;
    	resp=fetchURLSSL(currhost,"POST","/v1/users/ME/endpoints/SELF/subscriptions",heads,
    			SKYPEWEB_RESOURCE_SUBSCRIPTION_REQUESTS);
    	if (resp.headers.containsKey("Location"))
    	{
    		try {
				URL url=new URL(resp.headers.get("Location").get(0));
				subscriptionPath = url.getFile();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    public String pollForEvents() throws FileNotFoundException
    {
    	clearHeaders();
    	commonHeaders();
    	contentHeaderForm();
    	resp=fetchURLSSL(currhost,"POST",subscriptionPath+"/poll",heads,"");
		//if (resp.statusCode==404) throw new FileNotFoundException("method returned 404, probably need relog");
    	return resp.body;
    }
    public void announceEndpoint()
    {
    	clearHeaders();
    	commonHeaders();
    	contentHeaderJSON();
    	resp=fetchURLSSL(currhost,"PUT","/v1/users/ME/endpoints/"+endpointId+
    			"/presenceDocs/messagingService",heads,
    			"{\"id\":\"messagingService\", \"type\":\"EndpointPresenceDoc\", \"selfLink\":\"uri\", \"privateInfo\":{\"epname\":\"skype\"}, \"publicInfo\":{\"capabilities\":\"\", \"type\":1, \"typ\":1, \"skypeNameVersion\":\"908/1.13.79/swx-skype.com\", \"nodeInfo\":\"xx\", \"version\":\"908/1.13.79\"}}"
    		);
    	endpointReady=true;
    }
    public void leaseClientEndpoint()
    {
		fetchNewEndpoint();
		if (endpointId != null)
		{
			subscribeEndpoint();
			if (subscriptionPath != null)
			{
				announceEndpoint();
				sendPresenceStatus();
			}
		}
    }
    public void renewLeaseIfExpired()
    {
    	if (timeInSeconds()>=registrationTokenExpirationTime)
    	{
    		leaseClientEndpoint();
    	}
    }
    public void setPresenceStatus(String status)
    {
    	presenceStatus=status;
    	if (endpointReady)
    	{
    		sendPresenceStatus();
    	}
    }
    public void sendPresenceStatus()
    {
    	clearHeaders();
    	commonHeaders();
    	contentHeaderJSON();
    	resp=fetchURLSSL(currhost,"PUT","/v1/users/ME/presenceDocs/messagingService",heads,
    			"{\"status\":\""+presenceStatus+"\"}");
    }
    public void sendMessage(String buddy, String message)
    {
    	clearHeaders();
    	commonHeaders();
    	contentHeaderJSON();
    	resp=fetchURLSSL(currhost,"POST","/v1/users/ME/conversations/8:"+buddy+"/messages",heads,
					"{\"clientmessageid\":"+timeStringInSeconds()+",\"content\":\""+message+
    			"\",\"messagetype\":\"RichText\",\"contenttype\":\"text\"}");
    }
    public void logout()
    {
    	clearHeaders();
    	commonHeaders();
    	heads.append("Cookie", cookies);
    	resp=fetchURLSSL("login.skype.com","GET","/logout",heads,"");
    }
}
