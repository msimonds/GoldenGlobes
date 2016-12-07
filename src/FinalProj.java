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
		File movieFile = new File("TrainMovies.csv");
		PrintWriter movies = new PrintWriter(movieFile);
		File dirFile = new File("TrainDirectors.csv");
		PrintWriter directors = new PrintWriter(dirFile);
		String bestPicAtts= "Director,Writer1,Writer2,Actor1,Actor2,Actor3,TotalAwards,Series,TomatoRev, AudienceRev,Income,Genre";
		movies.println(bestPicAtts);
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
			int fuckme = 9;
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
					String series = "N";
					if(notes.contains("sequel")||notes.contains("installation")||notes.contains("series")){
						series = "Y";
					}
					String instanceStr = getData(json,series);
					movies.println(instanceStr);					
				}
				line = file.nextLine();
				fuckme=fuckme+1;
				if(fuckme==15){
					movies.close();
				}
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
	
	//this method gets called once a jsonobject has been returned by the REST call
	//it grabs relevant data from the json and returns the string to be printed in our instance list
	public static String getData(JSONObject movie, String sequel) throws JSONException{
		StringBuffer str = new StringBuffer();
		str = str.append(movie.getJSONObject("Director").toString()+",");
		String[] writers = movie.getJSONObject("Writer").toString().split(",");
		if(writers.length>1){
			str = str.append(stripParens(writers[0]) + ","+stripParens(writers[1])+",");
		}
		String[] allActors = movie.getJSONObject("Actors").toString().split(",");
		str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+","+stripParens(allActors[2])+",");
		
		String awards[] = movie.getJSONObject("Awards").toString().split(" ");
		int numAwards = 0;
		for(String s : awards){
			if(Character.isDigit(s.charAt(0))){
				numAwards = numAwards + Integer.parseInt(s);
			}
		}
		str = str.append(numAwards+","+sequel+"," +movie.getJSONObject("tomatoMeter"));
		
		
		
		
		return str.toString();
	}
	
	public static String stripParens(String str){
		int index =  str.indexOf('(');
		String newS=str;
		if(index>=0){
			newS = str.substring(index, str.length());
		}
		
		return newS;
	}

}
