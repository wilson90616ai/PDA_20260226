package com.senao.warehouse.http;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HttpClient {
	private static final String SERVER_RESPONSE_ERROR = "ERROR";
	private DefaultHttpClient httpclient;
	private HttpHost host;
	private HttpContext localContext;
	private ServerResponse message = new ServerResponse();
	private Gson gson = new Gson();

	public HttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, Integer.parseInt(AppController.getProperties("Connection_Timeout")));
		HttpConnectionParams.setSoTimeout(params, Integer.parseInt(AppController.getProperties("So_Timeout")));
		httpclient = new DefaultHttpClient(params);
		host = new HttpHost(AppController.getServerInfo().split(":")[0], Integer.parseInt(AppController.getServerInfo().split(":")[1]), "http");
	}

	public HttpClient(String hostName, int port) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, Integer.parseInt(AppController.getProperties("Connection_Timeout")));
		HttpConnectionParams.setSoTimeout(params, Integer.parseInt(AppController.getProperties("So_Timeout")));
		httpclient = new DefaultHttpClient(params);
		host = new HttpHost(hostName, port, "http");
	}

	public ServerResponse doPost(String msg, String uri, int timeout) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		httpclient = new DefaultHttpClient(params);
		return doPost(msg, uri);
	}

	public ServerResponse doPost(String msg, String uri) {
		String ack;

		try {
			AppController.debug("Sending data to " + host + uri);
			AppController.debug("送出的 data:"+ msg);

			if(msg.length() > 4000) {
				for(int i=0;i<msg.length();i+=4000){
					if(i+4000<msg.length())
						AppController.debug("送出的 data1:"+ msg.substring(i, i+4000));
					else
						AppController.debug("送出的 data2:"+ msg.substring(i, msg.length()));
				}
			} else{
				//AppController.debug("送出的 data3:"+ msg);
			}

			HttpPost httppost = new HttpPost(host.toURI() + uri);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("data", msg));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			httppost.addHeader("Accept-Language", "zh-TW,zh-Hans;q=0.8,en-US;q=0.5,en;q=0.3");
			httppost.addHeader("Accept", "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, */*");
			httppost.addHeader("DNT", "1");
			httppost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)");
			httppost.addHeader("Accept-Encoding", "gzip, deflate");
			AppController.debug("Sending URI " + httppost.getURI());

			/*
			 * URI uri1 = new
			 * URI("http://127.0.0.1:8080/TimsWab/Upload/Data/NEW/TICKET");
			 * httppost.setURI(uri1);
			 */
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			ack = EntityUtils.toString(entity, "UTF-8");

			AppController.debug("Service status =" + statusCode);
			AppController.debug("Response entity =" + ack);

			/*if(ack.length()>4000) {
				Log.i("onSuccess", ack.substring(0, 4000));
				Log.i("onSuccess", ack.substring(3900, ack.length()));
			}

			int len=4000;

			if(ack.length()>len){
				for(int i=0;i<ack.length();i+=len){
					if(i+len<ack.length()){
						Log.i("onSuccess", ack.substring(i, i+len));
					}else{
						Log.i("onSuccess", ack.substring(i, ack.length()));
					}
				}
			}*/

			if (statusCode != 200) {
				message.setCode(String.valueOf(statusCode));
				message.setMessage(response.toString());
				return message;
			} else {
				message = gson.fromJson(ack, ServerResponse.class);
			}
		} catch (Exception e) {
			AppController.debug( "Exception:"+e.getMessage());
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
			return message;
		}

		return message;
	}

	public ServerResponse doPostByOtherHost(String msg, String uri) {
		String ack;

		try {
			AppController.debug("Sending data to " + uri);
			AppController.debug("送出的 data:"+ msg);

			if(msg.length() > 4000) {
				for(int i=0;i<msg.length();i+=4000){
					if(i+4000<msg.length())
						AppController.debug("送出的 data1:"+ msg.substring(i, i+4000));
					else
						AppController.debug("送出的 data2:"+ msg.substring(i, msg.length()));
				}
			} else{
				//AppController.debug("送出的 data3:"+ msg);
			}

			HttpPost httppost = new HttpPost( uri);

			//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			//nameValuePairs.add(new BasicNameValuePair("data", msg));
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			JSONObject json = new JSONObject(msg);
			StringEntity strEntity = new StringEntity(json.toString());
			httppost.setEntity(strEntity);

			//httppost.addHeader("Accept-Language", "zh-TW,zh-Hans;q=0.8,en-US;q=0.5,en;q=0.3");
			//httppost.addHeader("Accept", "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, */*");
			httppost.addHeader("Accept","application/json");
			httppost.addHeader("Content-Type","application/json; charset=utf8");
			//httppost.addHeader("DNT", "1");
			//httppost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)");
			//httppost.addHeader("Accept-Encoding", "gzip, deflate");
			AppController.debug("Sending URI " + httppost.getURI());

			/*
			 * URI uri1 = new
			 * URI("http://127.0.0.1:8080/TimsWab/Upload/Data/NEW/TICKET");
			 * httppost.setURI(uri1);
			 */
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			ack = EntityUtils.toString(entity, "UTF-8");

			AppController.debug("Service status =" + statusCode);
			AppController.debug("Response entity =" + ack);

			if (statusCode != 200) {
				message.setCode(String.valueOf(statusCode));
				message.setMessage(response.toString());
				return message;
			} else {
				message = gson.fromJson(ack, ServerResponse.class);
				AppController.debug("Response message =" + message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
			return message;
		}

		return message;
	}

	public String doGetByFortinet(String msg, String uri) {
		String response_str;

		try {
			AppController.debug("Sending data to " + uri);
			AppController.debug("送出的 data:"+ msg);

			if(msg.length() > 4000) {
				for(int i=0;i<msg.length();i+=4000){
					if(i+4000<msg.length())
						AppController.debug("送出的 data1:"+ msg.substring(i, i+4000));
					else
						AppController.debug("送出的 data2:"+ msg.substring(i, msg.length()));
				}
			} else{
				//AppController.debug("送出的 data3:"+ msg);
			}

			HttpGet httpget = new HttpGet(uri);

			//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			//nameValuePairs.add(new BasicNameValuePair("data", msg));
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			//httppost.addHeader("Accept-Language", "zh-TW,zh-Hans;q=0.8,en-US;q=0.5,en;q=0.3");
			//httppost.addHeader("Accept", "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, */*");
			httpget.addHeader("Accept","application/json");
			httpget.addHeader("Content-Type","application/json; charset=utf8");
			//httppost.addHeader("DNT", "1");
			//httppost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)");
			//httppost.addHeader("Accept-Encoding", "gzip, deflate");
			AppController.debug("ResponseGet Sending URI " + httpget.getURI());

			/*
			 * URI uri1 = new
			 * URI("http://127.0.0.1:8080/TimsWab/Upload/Data/NEW/TICKET");
			 * httppost.setURI(uri1);
			 */
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			int statusCode = response.getStatusLine().getStatusCode();
			response_str = EntityUtils.toString(entity, "UTF-8");

			//AppController.debug("ResponseGet Service status =" + statusCode);
			//AppController.debug("ResponseGet entity =" + response_str);

			if (statusCode != 200) {
				return response.toString();
			} else {
				//AppController.debug("ResponseGet response_str =" + response_str);
				return response_str;
			}
		} catch (Exception e) {
			AppController.debug("ResponseGet Exception =" + e);
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public ServerResponse doPostFile(File file, String uri, String fileDescription) throws Exception {
		try {
			return executeMultiPartRequest(host + uri, file, file.getName(), fileDescription);
		} catch (Exception e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
			return message;
		}
	}

	public ServerResponse executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription) throws Exception {
		HttpPost postRequest = new HttpPost(urlString);
		ContentBody fileBody = new FileBody(file, "application/octect-stream");

		//The usual form parameters can be added this way
		MultipartEntity multiPartEntity = new MultipartEntity();
		multiPartEntity.addPart(
				"fileDescription",
				new StringBody(fileDescription != null ? fileDescription
						.replace(Environment.getExternalStorageDirectory()
								.toString(), "") : ""));
		multiPartEntity.addPart("fileName",
				new StringBody(fileName != null ? fileName : file.getName(),
						Charset.forName(HTTP.UTF_8)));
		multiPartEntity.addPart("attachment", fileBody);

		/*
		 * Need to construct a FileBody with the file that needs to be attached
		 * and specify the mime type of the file. Add the fileBody to the
		 * request as an another part. This part will be considered as file part
		 * and the rest of them as usual form-data parts
		 */

		postRequest.setEntity(multiPartEntity);
		AppController.debug("executing request " + postRequest.getRequestLine());

		return executeRequest(postRequest);
	}

	/**
	 * A generic method to execute any type of Http Request and constructs a
	 * response object
	 * 
	 * @param requestBase
	 *            the request that needs to be exeuted
	 * @return server response as <code>String</code>
	 */
	private ServerResponse executeRequest(HttpRequestBase requestBase) {
		InputStream responseStream = null;

		try {
			HttpResponse response = httpclient.execute(requestBase);

			if (response != null) {
				HttpEntity entity = response.getEntity();
				int statusCode = response.getStatusLine().getStatusCode();
				String ack = EntityUtils.toString(entity, "UTF-8");

				AppController.debug("Service status =" + statusCode);
				AppController.debug("Response entity =" + ack);

				if (statusCode != 200) {
					message.setCode(String.valueOf(statusCode));
					message.setMessage(response.toString());
					return message;
				} else {
					message = gson.fromJson(ack, ServerResponse.class);
				}

				/*
				 * HttpEntity responseEntity = response.getEntity();
				 * 
				 * if (response.getStatusLine().getStatusCode() != 200) {
				 * message.setNum(String.valueOf(response.getStatusLine()
				 * .getStatusCode())); message.setMessage(response.toString());
				 * return message; } else { if (responseEntity != null) {
				 * responseStream = responseEntity.getContent(); if
				 * (responseStream != null) { BufferedReader br = new
				 * BufferedReader( new InputStreamReader(responseStream));
				 * String responseLine = br.readLine(); String
				 * tempResponseString = ""; while (responseLine != null) {
				 * tempResponseString = tempResponseString + responseLine +
				 * System.getProperty("line.separator"); responseLine =
				 * br.readLine(); } br.close(); AppController.debug("Server response = "
				 * + responseLine); message = gson.fromJson(responseLine,
				 * ServerResponse.class); } } }
				 */
			} else {
				message.setCode(SERVER_RESPONSE_ERROR);
				return message;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			message.setCode(SERVER_RESPONSE_ERROR);
			message.setMessage(e.getMessage());
		} finally {
			if (responseStream != null) {
				try {
					responseStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//httpclient.getConnectionManager().shutdown();

		return message;
	}
}
