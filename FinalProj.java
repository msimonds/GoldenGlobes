import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.*;


public class FinalProj {
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//Loop through the text files w/ movie titles
		for(int i=1990; i<2016;i++){
			//Open a file and loop through the movie titles. 
			
		}
		 
		System.out.println(OMDB("Argo","2012").toString());

	}
	
	public static JSONObject OMDB(String title, String year) throws IOException{
		 String urlName = "http://www.omdbapi.com/?t="+title+"&y="+year+"&type=movie&tomatoes=true&r=json";
		 URL url = new URL(urlName);	
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();
		  conn.setRequestMethod("GET");
		  conn.setDoOutput(true);
		  conn.setDoInput(true);
		  conn.setUseCaches(false);
		  conn.setAllowUserInteraction(false);
		  conn.setRequestProperty("Content-Type",
		      "application/x-www-form-urlencoded");		 
/*
		  // Create the form content
		  OutputStream out = conn.getOutputStream();
		  Writer writer = new OutputStreamWriter(out, "UTF-8");
		  writer.write(x);
		  writer.close();
		  out.close();
		  */

		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }

		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
		    sb.append(line);
		  }
		  rd.close();

		  conn.disconnect();
		  
		  JSONObject json=null;
		  try {
			 json= new JSONObject(sb.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("json not working");
		}
		  return json;
		
	}

}
