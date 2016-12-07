import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.*;
import java.io.PrintWriter;


public class FinalProj {
	HashMap<String, HashMap<String, String>> Nominations = new HashMap<String, HashMap<String, String>>();
	public static void main(String[] args) throws IOException, NumberFormatException, JSONException {
		double minRate = 58.0;
		PrintWriter movies = new PrintWriter("TrainMovies.csv");
		PrintWriter directors = new PrintWriter("TrainDirectors.csv");
		//Loop through the text files w/ movie titles (1990-2014 for training)
		for(int i=1990; i<2015;i++){
			String year = Integer.toString(i);
			//Organize by year. Put year in quotes to isolate from movie titles starting with numbers.
			movies.println("\"" + year + "\"");
			directors.println("\"" + year + "\"");
			File f = new File("Movies by year/" + year+".csv");
			Scanner file = new Scanner(f);
			file.nextLine(); //skips title line
			String line = file.nextLine(); //now at begining of third line, "line" is second line
			while (file.hasNextLine()){
				String[] lineParts = line.split(",");				
				String title = lineParts[0].substring(0,lineParts[0].length());
				//also save notes to determine if part of series later
				String notes = lineParts[lineParts.length-1].substring(0,lineParts[lineParts.length-1].length());
				String director = lineParts[1].substring(0,lineParts[1].length());
				System.out.println(notes + " ** "+director);
				JSONObject json = OMDB(title, year);
				Double tomato=-1.0;
				try{
				tomato = Double.parseDouble((String) json.get("tomatoMeter"));
				} catch(Exception e){
					tomato=0.0;
				}
				if (tomato >= minRate){
					movies.print(title);
					movies.print(",");
					movies.println(notes);
					movies.print(director);
					movies.print(",");
					movies.println(title);
				}
				line = file.nextLine();
			}
		}
		 
		System.out.println(OMDB("Argo","2012").toString());

	}
	
	public void nominationsMap(String file){
		Scanner scan = new Scanner (file);
		line = scan.nextLine();
		while (scan.hasNextLine()){
			//should always go into this if statement
			if (line.subString(0,1).equals("\"")){
				String[] yearAndType = line.split(",");
				String year = yearAndType[0].substring(0,yearAndType[0].length()); //take out quotes
				String type = yearAndType[1]; //category
				if (this.Nominations.containsKey(year)==false){
					this.Nominations.put(year, new HashMap<String, String>());
				}
				line = scan.nextLine();
				//add each movie title/director between categories to hashmap
				//do we need to keep the movie title next to director name??
				while (line.subString(0,1).equals("\"")==false){
					this.Nominations.get(year).put(line, type); //name of movie/director and it's nomination category
					line = scan.nextLine();
				}
			}
		}
	}
					
	public static JSONObject OMDB(String title, String year) throws IOException{
		String urlTitle = title.replace(' ', '+');
		 String urlName = "http://www.omdbapi.com/?t="+urlTitle+"&y="+year+"&type=movie&tomatoes=true&r=json";
		 URL url = new URL(urlName);
		 System.out.println(urlName);
		 
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
