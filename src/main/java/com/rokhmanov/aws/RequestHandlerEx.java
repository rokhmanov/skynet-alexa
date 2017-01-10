package com.rokhmanov.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandlerEx {

	private static final Logger log = LoggerFactory.getLogger(RequestHandlerEx.class);
	
    private static final String API_URL = "https://172.31.1.2:4443/api/17/";
           
    public String getMountUsageByNode(String node, String mount) throws Exception {
    	List<LogRecord> nodeRecords = getLogRecordsByNode(node);
    	log.info("Records returned:" + nodeRecords.size());
    	String message = "Not found";
		for (LogRecord logRecord : nodeRecords) {
			if (logRecord.getMount().equalsIgnoreCase(mount)){
				String usagePercent = logRecord.getUsedPercent();
				message = usagePercent.substring(0, usagePercent.length() - 1) + " percent";
				break;
			}
		}
		return message;    	
    }
    
    protected List<LogRecord> getLogRecordsByNode(String node) throws Exception {
    	String executionsURL = API_URL + "job/QWEQWEQWEqweqwe123-qwe123-123123/executions?max=1&format=json&authtoken=ASDasdzxc123asd123";
    	String executionId = retrieveIdFromExecutionJson(httpGetJob(executionsURL));
    	log.info("EXECUTION_ID:" + executionId);
    	String executionOutputURL = API_URL + "execution/" + executionId + "/output?format=json&authtoken=ASDasdzxc123asd123"; 
    	Map<String,List<String>> logs = retrieveLogsFromExecOutputJson(httpGetJob(executionOutputURL));
    	List<String> nodeLogs = logs.get(node);
    	List<LogRecord> ret  = new ArrayList<>();
		for (String record : nodeLogs) {
			String[] cols =  record.split(" +");
			if (cols.length == 6) {
				LogRecord lr = new LogRecord();
				lr.setFilesystem(cols[0]);
				lr.setSize(cols[1]);
				lr.setUsed(cols[2]);
				lr.setAvailable(cols[3]);
				lr.setUsedPercent(cols[4]);
				lr.setMount(cols[5]);
				ret.add(lr);
			}
		}    	
    	return ret;
    }
    
	protected String retrieveIdFromExecutionJson(String executionJson) throws JsonProcessingException, IOException {
		String ret = "";
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(executionJson);			
		JsonNode executionsNode = rootNode.path("executions");
		Iterator<JsonNode> elements = executionsNode.elements();
		while(elements.hasNext()){
			JsonNode execution = elements.next();
			JsonNode id = execution.path("id");
			ret = id.asText();
		}
		return ret;
	}
	
	
	protected Map<String,List<String>> retrieveLogsFromExecOutputJson(String execOutputJson) throws JsonProcessingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(execOutputJson);			
		JsonNode entriesNode = rootNode.path("entries");
		Iterator<JsonNode> entries = entriesNode.elements();
		Map<String,List<String>> data = new HashMap<>();
		String currentHost = "";
		List<String> logs = new ArrayList<>();
		while (entries.hasNext()){
			JsonNode entry = entries.next();
			String node = entry.path("node").asText();
			String log = entry.path("log").asText();
			if (currentHost.equalsIgnoreCase(node)){
				logs.add(log);
			} else {
				if (!currentHost.equals("")){
					if (data.containsKey(currentHost)){
						List<String> existingLogs = data.get(currentHost);
						existingLogs.add(log);
						data.put(currentHost, existingLogs);
					} else {
						data.put(currentHost, logs);																	
					}
				}
				currentHost = node;
				logs = new ArrayList<>();
				logs.add(log);
			}
		}
		return data;
	}	
    
	
	protected String httpGetJob(String url) throws NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
		String ret = "";
	    SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(new TrustAllStrategy());
	    @SuppressWarnings("deprecation")
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	            builder.build(),
	            SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
	    		);
	    CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
	            sslsf).build();
		
	    HttpGet httpGet = new HttpGet(url);
	    CloseableHttpResponse response = httpclient.execute(httpGet);
	    try {
            HttpEntity entity = response.getEntity();
	        InputStream inputStream = entity.getContent();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			StringBuffer resultString = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				resultString.append(line);
			}
			ret = resultString.toString();
	        EntityUtils.consume(entity);
	        inputStream.close();
	    }
	    finally {
	        response.close();
	    }				
	    return ret;
	}
	
}

class TrustAllStrategy implements TrustStrategy {
    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        return true;
    }
}
