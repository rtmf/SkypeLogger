package skypeWebConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkypeWebURLHeaders extends HashMap<String, List<String>> 
{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		public void setIfAbsent(String hnam, String hval)
		{
			if (!this.containsKey(hnam))
				this.append(hnam,hval);
		}
		public void append(String hnam, String hval)
		{
			if (!this.containsKey(hnam))
				this.put(hnam,(new ArrayList<String>()));
			this.get(hnam).add(hval);
		}
		public SkypeWebURLHeaders (String headers)
		{
			BufferedReader rdr = new BufferedReader(new StringReader(headers));
			String line;
			try {
				while ((line = rdr.readLine()) != null)
				{
					int cpos = line.indexOf(':');
					if (cpos<0) continue;
					String hnam = line.substring(0,cpos);
					String hval = line.substring(cpos+1).trim();
					this.append(hnam,hval);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public SkypeWebURLHeaders ()
		{
			this.clear();
		}
		public SkypeWebURLHeaders (Map<String, List<String>> headers)
		{
			for (String hnam: headers.keySet())
			{
				if (hnam == null) continue; // null header is the response string
				this.put(hnam, (new ArrayList<String>()));
				for (String hval: headers.get(hnam))
				{
					this.get(hnam).add(hval);
				}
			}
		}
		public String headers()
		{
			StringBuilder response = new StringBuilder();
			for (String hnam: this.keySet())
			{
				for (String hval: this.get(hnam))
				{
					response.append(hnam);
					response.append(": ");
					response.append(hval);
					response.append("\r\n");
				}
			}
			return response.toString();
		}
}
