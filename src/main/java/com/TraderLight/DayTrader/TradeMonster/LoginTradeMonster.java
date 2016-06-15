package com.TraderLight.DayTrader.TradeMonster;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.TraderLight.DayTrader.StockTrader.Logging;

public class LoginTradeMonster {
	
	   public static final Logger log = Logging.getLogger(true);
	   private String username;
	   private String password;
	   private String urlstr;
	   private String sourceApp;
	   private final String USERKEY = "j_username";
	   private final String PASSKEY = "j_password";
	   
	   public LoginTradeMonster(String username, String password, String urlstr, String sourceApp) {
		   this.username = username;
		   this.password = password;
		   this.urlstr = urlstr;
		   this.sourceApp = sourceApp;
	   }
		
		
		public void login() throws IOException, ParseException {
		  
			  URL  url = new URL (urlstr);
			  URLConnection  urlConn = url.openConnection();
			    
			  urlConn.setDoInput (true); // Let the run-time system (RTS) know that we want input.
			  urlConn.setDoOutput (true); // Let the RTS know that we want to do output.
			  urlConn.setUseCaches (false); // No caching, we want the real thing.
			  
			  urlConn.setRequestProperty("User-Agent", "Mozilla/5.0");
			  urlConn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml");
			  urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			  String cookie = "JSESSIONID="+TMSessionControl.getInitialCookie();
			  urlConn.setRequestProperty("Cookie", cookie);
			  urlConn.setRequestProperty("Connection", "keep-alive");
			  			  
			  log.info("Sending login request to url " + urlstr);
			  
			  // Send POST output.
			  DataOutputStream printout = new DataOutputStream (urlConn.getOutputStream ());
			  StringBuffer data=new StringBuffer();
			  data.append(URLEncoder.encode(USERKEY, "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8"));
			  data.append("&");
			  data.append(URLEncoder.encode(PASSKEY, "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8"));				
			  printout.writeBytes(data.toString());
			  printout.flush();
			  printout.close();
			    
			  // Get response data.
			  String resp = "";
			  String line;
			  BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			  while ((line = reader.readLine()) != null) {
			        resp += line;
			  }
			  reader.close();
			  log.info("Response is: " + resp);				
			  List<String> cookies = new ArrayList<String>();
			  cookies = urlConn.getHeaderFields().get("Set-Cookie");
				
			  if (cookies != null) {
				    TMSessionControl.setCookie(cookies);
				    //log.info("cookie is not null");
					for (String str : cookies) {
						log.info("Cookie" + str);
					}
			  } else {
					//log.info("cookie is null");
					cookies = new ArrayList<String>();
					cookies.add("JSESSIONID="+TMSessionControl.getInitialCookie());
					TMSessionControl.setCookie(cookies);
			  }
			  
			  JSONParser parser = new JSONParser();
			  Object obj = null;
			  obj = parser.parse(resp);			  
			  JSONObject omap = (JSONObject) obj;
			  TMSessionControl.setSessionid((String)omap.get("sessionId"));
			  TMSessionControl.setToken((String)omap.get("token"));
			  TMSessionControl.setUser((Long)omap.get("userId"));
			  TMSessionControl.setSourceApp(sourceApp);
						
		}
		
		public   void logout() throws IOException {
					
			String urlstr = "https://services.optionshouse.com/j_acegi_logout";
			URL  url = new URL (urlstr);
		    URLConnection  urlConn = url.openConnection();
			urlConn.setDoInput (true); // Let the run-time system (RTS) know that we want input.
		    urlConn.setDoOutput (true); // Let the RTS know that we want to do output.
		    urlConn.setUseCaches (false); // No caching, we want the real thing.
		    urlConn.setRequestProperty("Content-Type", "application/xml");
		    urlConn.setRequestProperty("token", TMSessionControl.getToken());
		    urlConn.setRequestProperty("JSESSIONID", TMSessionControl.getSessionid());
		    urlConn.setRequestProperty("cookie", TMSessionControl.getSessionid());
		    urlConn.connect();
		    log.info("Sent log out ");
		}
		

}
