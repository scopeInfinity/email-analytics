package com.serendio.Utils;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;


public class ExtractSentiment {

	private static final String FETCH_URL = "http://127.0.0.1:1501";

	private double sentiments;
	private String content;

	public ExtractSentiment(String content) {
		this.content = content;
	}

	public boolean fetchSentiment() {
		try{
			URL url = new URL(FETCH_URL+"/sentiments");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);

			List<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
			params.add(new Pair("text", content));
			
			OutputStream os = urlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(params));
			writer.flush();
			writer.close();
			os.close();

			urlConnection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	      	StringBuilder stringBuilder = new StringBuilder();
	      	String line = null;
			while ((line = reader.readLine()) != null)
			{
			stringBuilder.append(line + "\n");
			}
			sentiments = extractSentiment(stringBuilder.toString());
			return true;
		}catch(Exception e) {
			System.err.println(e.toString());
		}
		return false;
	}

	public double getSentiment() {
		return sentiments;
	}


	//http://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
	private String getQuery(List<Pair<String,String>> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (Pair<String,String> pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getFirst(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getSecond(), "UTF-8"));
	    }

	    return result.toString();
	}

	private double extractSentiment(String json) {
		Gson gson = new Gson();
		Response response = gson.fromJson(json, Response.class);
		return response.polarity;
	}

	public class Response {
		double polarity;
		double subjectivity;
	}

	private class Pair<T1, T2> {
		private T1 data1;
		private T2 data2;

		Pair(T1 data1, T2 data2) {
			this.data1 = data1;
			this.data2 = data2;
		}

		public T1 getFirst() {
			return data1;
		}
		
		public T2 getSecond() {
			return data2;
		}
	}

}