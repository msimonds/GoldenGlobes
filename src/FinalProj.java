import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;

import org.json.*;
import java.io.PrintWriter;


public class FinalProj {
	static HashMap<String, HashMap<String, String>> Nominations = new HashMap<String, HashMap<String, String>>();
	
	public static void main(String[] args) throws IOException, NumberFormatException, JSONException {
		nominationsMap("Golden Globe Nominations 1991-2016.txt");
		HashMap<String, HashMap<String, String>> n = Nominations;
		double minRate = 53.0;
		File movieFile = new File("TrainMovies.csv");
		PrintWriter movies = new PrintWriter(movieFile);
		File dirFile = new File("TrainDirectors.csv");
		PrintWriter directors = new PrintWriter(dirFile);
		String bestPicAtts= "Director,Writer1,Writer2,Actor1,Actor2,Actor3,TotalAwards,Series,TomatoRev,AudienceRev,Income,Genre1,Genre2,Genre3,Label";
		String dirAtts= "Director,Writer1,Writer2,Actor1,Actor2,Actor3,Genre1,Genre2,Genre3,Label";
		
		movies.println(bestPicAtts);
		directors.println(dirAtts);
		File mTestFile = new File("TestMovies.csv");
		PrintWriter movieTest = new PrintWriter(mTestFile);
		File dTestFile = new File("TestDirectors.csv");
		PrintWriter directorsTest = new PrintWriter(dTestFile);
		movieTest.println(bestPicAtts);
		directorsTest.println(dirAtts);
		//Loop through the text files w/ movie titles (1990-2014 for training)
		for(int i=2004; i<2010;i++){
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
				String title = lineParts[0].substring(1,lineParts[0].length()-1);
				//also save notes to determine if part of series later
				String notes = lineParts[lineParts.length-1].substring(1,lineParts[lineParts.length-1].length()-1);
				String director = lineParts[1].substring(1,lineParts[1].length()-1);
				//System.out.println(notes + " ** "+director);
				JSONObject json = OMDB(title, year);
				Double tomato=-1.0;
				try{
					tomato = Double.parseDouble((String) json.get("tomatoMeter"));
				} catch(Exception e){
					tomato=0.0;
				}
				Double imdb=-1.0;
				if (tomato==0.0){
					try{
						imdb = Double.parseDouble((String) json.get("imdbRating"));
					} catch(Exception e){
						imdb = 0.0;
					}
				}
				if (title.equals("Nine")||title.equals("Bobby")||title.equals("The Producers")||title.equals("The Phantom of the Opera")||title.equals("Ready to Wear")||title.equals("Patch Adams")){
					tomato = 56.0;
				}
				String genre;
				try{
					genre = json.get("Genre").toString();
				} catch(Exception e){
     					genre = "irrelevant";                           
                                }
				if ((tomato >= minRate || imdb >= 5.1) && !genre.contains("Documentary")){
					String series = "N";
					if(notes.contains("sequel")||notes.contains("installation")||notes.contains("series")){
						series = "Y";
					}
					String instanceStr = getDataMovies(json,series);
	               			//String instanceDirStr = getDataDir(json);
					//System.out.println(instanceStr);
					String thisMovieLabel;
					String thisDirLabel;
                     			if (Nominations.get(year).containsKey(title.toLowerCase())){
                             			if (Nominations.get(year).get(title.toLowerCase()).contains("Drama")){
                                     			thisMovieLabel = "yesDrama";
                             			} else {
                                     			thisMovieLabel = "yesComedy";
                             			}
                     			} else {
                             			thisMovieLabel = "no";
                     			}
					String dirPlusMovie = director+","+title;
					if (Nominations.get(year).containsKey(dirPlusMovie.toLowerCase())){
                                        	thisDirLabel = "yes";
					} else {
                                                thisDirLabel = "no";
                                        }
					//For training instances and then for test instances
					if(i<2015){
						movies.print(instanceStr);                           
                    				movies.println(thisMovieLabel + "    (" + title + ")");
						//directors.print(instanceDirStr);
						//directors.println(thisLabel);
					}else{
						movieTest.print(instanceStr);                           
                    				movieTest.println(thisMovieLabel);
						//directorsTest.print(instanceDirStr);
						//directorsTest.println(thisLabel);
					}
				}
				line = file.nextLine();
			}
		} 
		movies.close();
	}
	
	public static void nominationsMap(String file) throws FileNotFoundException{
		File f = new File(file);
		Scanner scan = new Scanner (f);
		String line = scan.nextLine();
		while (scan.hasNextLine()){
			//if broke out if inner while loop because hit empty line
			//skip that line and go to next year
			if (line.length()==0){
				line = scan.nextLine();
			}
			//should always go into this if statement
			//two different types of double quotes in this text file
			if (line.substring(0,1).equals("\"")||line.substring(0,1).equals("“")){
				String[] yearAndType = line.split(",");
				String year = Integer.toString(Integer.parseInt(yearAndType[0].substring(1,yearAndType[0].length()-1))-1); //take out quotes
				String type = yearAndType[1]; //category
				if (Nominations.containsKey(year)==false){
					Nominations.put(year, new HashMap<String, String>());
				}
				line = scan.nextLine(); //move on to first item in this category
				//add each movie title/director between categories to hashmap
				//do we need to keep the movie title next to director name??
				while (scan.hasNextLine()&&line.length()>0&&line.substring(0,1).equals("\"")==false&&line.substring(0,1).equals("“")==false){
					Nominations.get(year).put(line.toLowerCase(), type); //name of movie/director and it's nomination category
					line = scan.nextLine();
				}
			}
		}
	}
					
	public static JSONObject OMDB(String title, String year) throws IOException{
		 String urlTitle = title.replace(' ', '+');
		 urlTitle = urlTitle.replace("&","'\'&");
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
			e.printStackTrace();
			System.out.println("json not working");
		}
		  return json;
		
	}
	
	//this method gets called once a jsonobject has been returned by the REST call
	//it grabs relevant data from the json and returns the string to be printed in our instance list
	public static String getDataMovies(JSONObject movie, String sequel) throws JSONException{
		StringBuffer str = new StringBuffer();
		str = str.append(movie.get("Director").toString()+",");
		
		String[] writers = movie.get("Writer").toString().split(",");
		if(writers.length>1){
			str = str.append(stripParens(writers[0]) + ","+stripParens(writers[1])+",");
		} else{
			str=str.append(stripParens(writers[0])+", ,");
		}
		
		String[] allActors = movie.get("Actors").toString().split(",");
		if(allActors.length>2){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+","+stripParens(allActors[2])+",");
		} else if(allActors.length==2){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+", ,");
		} else if(allActors.length==1){
			str = str.append(stripParens(allActors[0]) + ", , ,");
		} else{
			str = str.append(", , , ,");
		}
		
		String awards[] = movie.get("Awards").toString().split(" ");
		int numAwards = 0;
		for(String s : awards){
			if(Character.isDigit(s.charAt(0))){
				numAwards = numAwards + Integer.parseInt(s);
			}
		}
		
		str = str.append(numAwards+","+sequel+",");		
		str = str.append(movie.get("tomatoMeter")+","+movie.get("tomatoUserMeter")+",");
		
		String boxof = movie.get("BoxOffice").toString().substring(1);
		System.out.println(boxof);
		if(Character.isDigit(boxof.charAt(0))){			 
			 str = str.append((Double.parseDouble(boxof.replaceAll(",", ""))+",").toString());
		}else{
			str = str.append(" ,");
		}
		
		String[] genres = movie.get("Genre").toString().split(",");		
		int count =0;
		for(String x : genres){
			if(count<3){
				str.append(stripParens(x) +",");
				count=count+1;
			}			
		}
		while(count<3){
			str.append(" " +",");
			count=count+1;
		}		
		return str.toString();
	}
	
	public static String getDataDir(JSONObject movie) throws JSONException{
		StringBuffer str = new StringBuffer();
		str = str.append(movie.get("Director").toString()+",");
		
		String[] writers = movie.get("Writer").toString().split(",");
		if(writers.length>1){
			str = str.append(stripParens(writers[0]) + ","+stripParens(writers[1])+",");
		} else{
			str=str.append(stripParens(writers[0])+", ,");
		}
		
		String[] allActors = movie.get("Actors").toString().split(",");
		if(allActors.length>2){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+","+stripParens(allActors[2])+",");
		} else if(allActors.length==2){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+", ,");
		} else if(allActors.length==1){
			str = str.append(stripParens(allActors[0]) + ", , ,");
		} else{
			str = str.append(", , , ,");
		}		
		
		String[] genres = movie.get("Genre").toString().split(",");		
		int count =0;
		for(String x : genres){
			if(count<3){
				str.append(x +",");
				count=count+1;
			}			
		}
		while(count<3){
			str.append(" " +",");
			count=count+1;
		}		
		return str.toString();
	}
	
	public static String stripParens(String str){
		int index =  str.indexOf('(');
		String newS=str;
		if(index>=0){
			newS = str.substring(0, index);
		}
		if(newS.charAt(0)==' '){
			newS = newS.substring(1);
		}
		if(newS.charAt(newS.length()-1)==' '){
			newS = newS.substring(0, newS.length()-1);
		}
		return newS;
	}

}
