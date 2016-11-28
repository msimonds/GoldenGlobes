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
import java.io.PrintWriter;


public class FinalProj {
	
	public static void main(String[] args) throws IOException {
		double minRate = 58.0;
		PrintWriter out = new PrintWriter("TrainMovies.csv");
		//Loop through the text files w/ movie titles
		for(int i=1990; i<2016;i++){
			String year = Integer.toString(i);
			Scanner file = new Scanner(year + ".csv");
			file.nextLine();
			line = file.nextLine();
			while (file.hasNextLine()){
				String[] lineParts = line.split(",");
				String title = lineParts[0].substring(1,String.length()-1);
				JSONObject json = OMDB(title, year);
				if (Double.parseDouble(json.get("tomatoMeter")) >= minRate){
					out.println(title);
				}
			}
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
