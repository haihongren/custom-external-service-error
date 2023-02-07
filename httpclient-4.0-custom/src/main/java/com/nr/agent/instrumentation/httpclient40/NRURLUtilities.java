package com.nr.agent.instrumentation.httpclient40;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.Config;

public class NRURLUtilities {
	
	private static List<String> url_regs;
	public static List<Integer> codeIgnores;
	
	protected static final String CONFIGKEY = "HttpUrlConnection.urlwhitelist";
	public static final String configName = "HttpUrlConnection.ignores";
	protected static String currentIgnores = null;
	protected static String currentWhitelist = null;
	
	
	static {
		url_regs = new ArrayList<String>();
		codeIgnores = new ArrayList<Integer>();
		Config config = AgentBridge.getAgent().getConfig();
		String ignores = config.getValue(configName);
		AgentBridge.getAgent().getLogger().log(Level.FINE, "Got this list of ignores from the config: {0}", new Object[]{ignores});
		if(ignores != null && !ignores.isEmpty()) {
			setCodeIgnores(ignores);
		}
		String s = config.getValue(CONFIGKEY);
		currentWhitelist = s;
		if(s != null && !s.isEmpty()) {
			setWhiteList(s+",/*");
		}else{
			setWhiteList("/*");
		}
	}
	
	public static void setWhiteList(String s) {
		url_regs.clear();
		StringTokenizer st = new StringTokenizer(s, ",");
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.replace("*", ".*");
			url_regs.add(token);
			AgentBridge.getAgent().getLogger().log(Level.FINE, "Added {0} to HttpUrlConnection whitelist", new Object[]{token});
		}

	}
	
	
	public static void reportConnectionMetrics(URI requestURI, int responseCode) {
		String protocol=requestURI.getScheme();
		String authority=requestURI.getAuthority();
		String path=requestURI.getPath();
		String host=requestURI.getHost();
		int port = requestURI.getPort();
		String portStr= (port == -1) ? "" : ":"+String.valueOf(port);

    	String whitelisted = null;
    	if (path != null) {
			whitelisted = getWhitelistedUrl(path);
			AgentBridge.getAgent().getLogger().log(Level.FINE, "whitelisted: {0} path: {1}",whitelisted,path);
			if (whitelisted != null && !whitelisted.isEmpty()) {
				path = whitelisted;
			}
		}
//		boolean ignoreThisCode = codeIgnores.contains(responseCode);
//
//		if (ignoreThisCode) {
//			AgentBridge.getAgent().getLogger().log(Level.FINE, "ignoreThisCode: {0} ",responseCode);
//		}
//
//		if(!ignoreThisCode && responseCode >= 400) {
//        	String urlString;
//        	if(path != null) {
//        		urlString = protocol + "://" + authority + path;
//        	} else {
//        		urlString = protocol + "://" + authority;
//        	}
//        	int index = urlString.indexOf('?');
//        	if(index > -1) {
//        		urlString = urlString.substring(0,index);
//        	}
//        	String errorStr = "HTTP " + responseCode +" received from " + urlString;
//        	NRHTMLException e = new NRHTMLException(errorStr);
//        	Map<String, String> errorMap = new HashMap<String, String>();
//        	errorMap.put("ResponseCode", Integer.toString(responseCode));
//        	errorMap.put("Host", authority);
//        	errorMap.put("URL", urlString);
//        	AgentBridge.publicApi.noticeError(e,errorMap);
//        }
    	String finalpath = path == null ? "" : path;

//    	String temp = path == null ? "" : path;
//    	if(whitelisted != null && !whitelisted.isEmpty()) {
//    		temp = whitelisted;
//    	}
//
//
//    	int index = temp.indexOf('?');
//    	if(index > -1) {
//    		temp = temp.substring(0, index);
//    	}
//    	index = temp.indexOf('/');
//    	if(index > -1) {
//    		temp = temp.substring(0, index);
//    	}
//
//    	String urlString;
//    	if(!temp.startsWith("/")) {
//    		urlString = protocol + "_"+ authority +"/"+temp;com.newrelic.instrumentation.httpclient-4.0-custom matched
//    	} else {
//    		urlString = protocol + "_"+ authority +temp;
//    	}
		AgentBridge.getAgent().getLogger().log(Level.FINE, "httpclient40 responseCode: {0} ",responseCode);
        if(responseCode >= 500) {
        	String metricName = "Custom/External-Returns/"+host+portStr+finalpath+"/5xx-Errors";
			AgentBridge.getAgent().getLogger().log(Level.FINE, "httpclient40 metricName: {0} host: {1} finalpath: {2} port: {3}", metricName,host,finalpath,portStr);
        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        	metricName = "Custom/External-Returns/5xx-Errors";
        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        } else if(responseCode >= 400 && responseCode < 500) {

        	String metricName = "Custom/External-Returns/"+host+portStr+finalpath+"/4xx-Errors";
			AgentBridge.getAgent().getLogger().log(Level.FINE, "httpurlconnection metricName: {0} host: {1} finalpath: {2} port: {3}", metricName,host,finalpath,portStr);

        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        	metricName = "Custom/External-Returns/4xx-Errors";
        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        } else {
        	String metricName = "Custom/External-Returns/"+host+portStr+finalpath+"/Normal";
			AgentBridge.getAgent().getLogger().log(Level.FINE, "httpurlconnection metricName: {0} host: {1} finalpath: {2} port: {3}", metricName,host,finalpath,portStr);

        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        	metricName = "Custom/External-Returns/Normal";
        	
        	AgentBridge.getAgent().getMetricAggregator().incrementCounter(metricName);
        }
		
	}

	public static List<String> getWhitelist() {
		return url_regs;
	}
	
	public static void setWhitelist(List<String> l) {
		url_regs = l;
	}
	
	public static String getWhitelistedUrl(String path) {
		List<UrlMatch> matches = new ArrayList<UrlMatch>();
			
		for(String regex : url_regs) {
			int depth = depth(regex);
				
			if(path.matches(regex)) {
				UrlMatch urlMatch = new UrlMatch(regex.replace(".*", "*"), depth);
				boolean add = matches.add(urlMatch);
			}
		}
		if(!matches.isEmpty()) {
			String whitelistUrl = null;
			int depth = -1;
			for(UrlMatch match : matches) {
				if(whitelistUrl == null) {
					whitelistUrl = match.regex;
					depth = match.depth;
				} else {
					if(match.depth > depth) {
						whitelistUrl = match.regex;
					}
				}
			}
			return whitelistUrl;
		}
		
		return null;
	}
	
	private static int depth(String path) {
		StringTokenizer st = new StringTokenizer(path, "/");
		
		return st.countTokens();
	}
	

	public static void setCodeIgnores(String ignores) {
		currentIgnores = ignores;
		StringTokenizer st = new StringTokenizer(ignores, ",");
		codeIgnores = new ArrayList<Integer>();
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!token.contains("-")) {
				Integer code = null;
				try {
					code = Integer.parseInt(token);
				} catch (NumberFormatException e) {
					AgentBridge.getAgent().getLogger().log(Level.FINEST,"Error parsing code from {0}", token);
				}
				if (code != null) {
					codeIgnores.add(code);
					AgentBridge.getAgent().getLogger().log(Level.FINER, "added {0} to ignores list",code);
				}
			} else {
				StringTokenizer st2 = new StringTokenizer(token,"-");
				if(st2.countTokens() == 2) {
					String tmp1 = st2.nextToken();
					String tmp2 = st2.nextToken();
					
					Integer start = Integer.parseInt(tmp1);
					Integer end = Integer.parseInt(tmp2);
					
					for(int i=start;i<=end;i++) {
						codeIgnores.add(i);
						AgentBridge.getAgent().getLogger().log(Level.FINER, "added {0} to ignores list",i);
					}
				}
			}
		}
	}
	
	private static class UrlMatch {
		protected String regex;
		protected int depth;
		
		protected UrlMatch(String r, int d) {
			regex = r;
			depth = d;
		}
	}
		
}
