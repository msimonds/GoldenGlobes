import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
	/* HashMap storing Golden Globe nominations. Organized by: Year --> Title/Name --> Category */
	static HashMap<String, HashMap<String, String>> Nominations = new HashMap<String, HashMap<String, String>>();
	
	/**
     	 * Builds all of our data sets for this project.
     	 * @param args
     	 */
	public static void main(String[] args) throws IOException, NumberFormatException, JSONException {
		///BUILDING NOMINATIONS HASHMAP///
		nominationsMap("Golden Globe Nominations 1991-2016.txt");
		//HashMap<String, HashMap<String, String>> n = Nominations;
		
		///ATTRIBUTES///
		String bestPicAtts= "Director,Writer1,Writer2,Actor1,Actor2,Actor3,TotalAwards,Series,TomatoRev,AudienceRev,Income,Genre1,Genre2,Genre3,Label";
		String dirAtts= "Director,Writer1,Writer2,Actor1,Actor2,Actor3,Genre1,Genre2,Genre3,TomatoRev,IMDbRat,Label";
		
		///CREATING FILES FOR EACH DATA SET///
		//training
		File movieFile = new File("TrainMovies.csv");
		PrintWriter movies = new PrintWriter(movieFile);
		File dirFile = new File("TrainDirectors.csv");
		PrintWriter directors = new PrintWriter(dirFile);
		//testing
		File mTestFile = new File("TestMovies.csv");
		PrintWriter movieTest = new PrintWriter(mTestFile);
		File dTestFile = new File("TestDirectors.csv");
		PrintWriter directorsTest = new PrintWriter(dTestFile);
		//predicting
		File mPredFile = new File("PredMovies.csv");
                PrintWriter moviePred = new PrintWriter(mPredFile);
                File dPredFile = new File("PredDirectors.csv");
                PrintWriter directorsPred = new PrintWriter(dPredFile);
		
		///PRINTING ATTRIBUTES TO EACH FILE///
		movies.println(bestPicAtts);
		directors.println(dirAtts);
		movieTest.println(bestPicAtts);
		directorsTest.println(dirAtts);
		moviePred.println(bestPicAtts);
                directorsPred.println(dirAtts);
		
		//LOOPING THROUGH MOVIE FILES AND BUILD TRAINING, TESTING, AND PREDICTION SETS ACCORDINGLY///
		for(int i=1990; i<2017;i++){
			String year = Integer.toString(i);
			//minimum accepted tomato rating
			double minRate = 53.0;
			File f = new File("Movies by year/" + year+".csv");
			Scanner file = new Scanner(new FileInputStream(f), "UTF-8");
			file.nextLine(); //skips title line
			String line = file.nextLine(); //line variable now represents the second line
			while (file.hasNextLine()){
				//split by comma
				String[] lineParts = line.split(",");	
				//title of movie
				String title = lineParts[0].substring(1,lineParts[0].length()-1);
				//use notes to determine whether movie is part of a series or not
				String notes = lineParts[lineParts.length-1].substring(1,lineParts[lineParts.length-1].length()-1);
				//director of movie
				String director = lineParts[1].substring(1,lineParts[1].length()-1);
				
				///JSON OBJECT FOR THIS MOVIE///
				JSONObject json = OMDB(title, year);
				Double tomato=-1.0;
				///GETTING TOMATO RATING///
				try{
					tomato = Double.parseDouble((String) json.get("tomatoMeter"));
				} catch(Exception e){
					tomato=0.0;
				}
				///GETTING IMDB RATING///
				Double imdb=-1.0;
				if (tomato==0.0){
					try{
						imdb = Double.parseDouble((String) json.get("imdbRating"));
					} catch(Exception e){
						imdb = 0.0;
					}
				}
				///DEALING WITH EXCEPTIONS///
				if (title.equals("Burlesque")||title.equals("Alice in Wonderland")||title.equals("Nine")||title.equals("Bobby")||title.equals("The Producers")||title.equals("The Phantom of the Opera")||title.equals("Ready to Wear")||title.equals("Patch Adams")||title.equals("The Sheltering Sky")||title.equals("Natural Born Killers")||title.equals("The Tourist")){
					tomato = 56.0;
				}
				///GETTING GENRE///
				String genre;
				try{
					genre = json.get("Genre").toString();
				} catch(Exception e){
     					genre = "irrelevant";                           
                                }
				///CREATING INSTANCES FOR MOVIES FITTING OUR CRITERIA///
				if ((tomato >= minRate || imdb >= 5.1) && !genre.contains("Documentary")&& !genre.contains("Adult")){
					String series = "N";
					//determining label for series attribute
					if(notes.contains("sequel")||notes.contains("installation")||notes.contains("series")){
						series = "Y";
					}
					///BUILDING STRING REPRESENTING THIS INSTANCE///
					String instanceStr = getDataMovies(json,series);
	               			String instanceDirStr = getDataDir(json);
					///LABELS///
					String thisMovieLabel;
					String thisDirLabel;
					//Golden Globe award year is one year ahead of movie publication year
					String newYear = Integer.toString(i+1);
					//For training instances, test instances, and prediction instances, respectively.
					if(i<2016){
						///SETTING LABELS///
						if (Nominations.get(newYear).containsKey(title.toLowerCase())){
                             				if (Nominations.get(nyear).get(title.toLowerCase()).contains("Drama")){
                                     				thisMovieLabel = "yesDrama";
                             				} else {
                                     				thisMovieLabel = "yesComedy";
                             				}
                     				} else {
                             				thisMovieLabel = "no";
                     				}
						String dirPlusMovie = director+","+title;
						if (Nominations.get(nyear).containsKey(dirPlusMovie.toLowerCase())){
                                        		thisDirLabel = "yes";
						} else {
                                                	thisDirLabel = "no";
                                        	}
						///PRINTING INSTANCES TO TRAINING AND TESTING FILES///
						if (i < 2015){
                                                	movies.print(instanceStr);          
                                                	movies.println(thisMovieLabel);
                                                	directors.print(instanceDirStr);
                                                	directors.println(thisDirLabel);
						} else {
							movieTest.print(instanceStr);                                         
                                                	movieTest.println(thisMovieLabel);
                                                	directorsTest.print(instanceDirStr);
                                                	directorsTest.println(thisDirLabel);
						}
					///PRINTING INSTANCES TO PREDICTION FILE///
                                        } else {
                                                moviePred.print(instanceStr);
                                                moviePred.println("no");
                                                directorsPred.print(instanceDirStr);
                                                directorsPred.println("no");
                                        }	
					
				}
				line = file.nextLine();
			}
		} 
		///CLOSING ALL PRINTWRITERS///
		movies.close();
                directors.close();
                movieTest.close();
                directorsTest.close();
                moviePred.close();
                directorsPred.close();
	}
	
	/**
     	 * Build the nominations HashMap based on given file, which lists past nominations from 1991-2016, organized by year.
     	 * @param file
     	 */
	public static void nominationsMap(String file) throws FileNotFoundException{
		File f = new File(file);
		Scanner scan = new Scanner(new FileInputStream(file), "UTF-8");
		String line = scan.nextLine();
		while (scan.hasNextLine()){
			//if we broke out if inner while loop because we hit an empty line,
			//skip that line and go to next year
			if (line.length()==0){
				line = scan.nextLine();
			}
			//String t = line.substring(0,1);
			int beg=1;
			if(line.charAt(1)=='"'){
				beg=2;
			}
			//should always go into this if statement
			//two different types of double quotes in this text file
			if (line.substring(0,1).equals("\"")||line.charAt(1)=='"'){
				String[] yearAndType = line.split(",");
				//year
				String year = yearAndType[0].substring(beg,yearAndType[0].length()-1); //take out quotes
				//category
				String type = yearAndType[1];
				//add year to HashMap
				if (Nominations.containsKey(year)==false){
					Nominations.put(year, new HashMap<String, String>());
				}
				line = scan.nextLine(); //move on to first item in this category
				//add each movie title/director between categories to hashmap
				while (scan.hasNextLine()&&line.length()>0&&line.substring(0,1).equals("\"")==false&&line.substring(0,1).equals("“")==false){
					Nominations.get(year).put(line.toLowerCase(), type); //name of movie/director and its nomination category
					line = scan.nextLine();
				}
			}
		}
	}
		
	/**
     	 * Gets the corresponding JSONObject from OMDb server based on given movie title and year of release.
     	 * @param title
	 * @param year
     	 * @return JSONObject based on given movie title and year of release.
     	 */
	public static JSONObject OMDB(String title, String year) throws IOException{
		 String urlTitle = title.replace(' ', '+');
		 urlTitle = urlTitle.replace("&","'\'&");
		 String urlName = "http://www.omdbapi.com/?t="+urlTitle+"&y="+year+"&type=movie&tomatoes=true&r=json";
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
			e.printStackTrace();
			System.out.println("json not working");
		}
		  return json;
		
	}
	
	/**
     	 * Grabs relevant data from the given JSONObject and returns the string to be printed to our movies instance list.
     	 * @param movie
	 * @param sequel
     	 * @return String representing an instance for the given movie, without the label.
     	 */
	public static String getDataMovies(JSONObject movie, String sequel) throws JSONException{
		StringBuffer str = new StringBuffer();
		///DIRECTOR///
		str = str.append(movie.get("Director").toString().split(",")[0]+",");
		///WRITERS///
		String[] writers = movie.get("Writer").toString().split(",");
		if(writers.length>1){
			str = str.append(stripParens(writers[0]) + ","+stripParens(writers[1])+",");
		} else{
			str=str.append(stripParens(writers[0])+", ,");
		}
		///CAST///
		String[] allActors = movie.get("Actors").toString().split(",");
		if(allActors.length>=3){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+","+stripParens(allActors[2])+",");
		} else if(allActors.length==2){
			str = str.append(stripParens(allActors[0]) + ","+stripParens(allActors[1])+", ,");
		} else if(allActors.length==1){
			str = str.append(stripParens(allActors[0]) + ", , ,");
		} else{
			str = str.append(", , , ,");
		}
		///PAST AWARDS///
		String awards[] = movie.get("Awards").toString().split(" ");
		int numAwards = 0;
		for(String s : awards){
			if(Character.isDigit(s.charAt(0))){
				numAwards = numAwards + Integer.parseInt(s);
			}
		}
		///SEQUEL///
		str = str.append(numAwards+","+sequel+",");
		///TOMATO RATINGS///
		str = str.append(movie.get("tomatoMeter")+","+movie.get("tomatoUserMeter")+",");
		///GROSS INCOME///
		String boxof = movie.get("BoxOffice").toString().substring(1);
		System.out.println(boxof);
		if(Character.isDigit(boxof.charAt(0))){			 
			str = str.append((Double.parseDouble(boxof.replaceAll(",", ""))+",").toString());
		}else{
			str = str.append(" ,");
		}
		///GENRES///
		String[] genres = movie.get("Genre").toString().split(",");		
		int count =0;
		for(String genre : genres){
			if(count<3){
				str.append(stripParens(genre) +",");
				count=count+1;
			}			
		}
		while(count<3){
			str.append(" " +",");
			count=count+1;
		}	
		
		return str.toString();
	}
	
	/**
     	 * Grabs relevant data from the given JSONObject and returns the string to be printed to our directors instance list.
     	 * @param movie
     	 * @return String representing an instance for the given movie, without the label.
     	 */
	public static String getDataDir(JSONObject movie) throws JSONException{
		StringBuffer str = new StringBuffer();
		///DIRECTOR///
		str = str.append(movie.get("Director").toString()+",");
		///WRITERS///
		String[] writers = movie.get("Writer").toString().split(",");
		if(writers.length>1){
			str = str.append(stripParens(writers[0]) + ","+stripParens(writers[1])+",");
		} else{
			str=str.append(stripParens(writers[0])+", ,");
		}
		///CAST///
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
		///GENRES///
		String[] genres = movie.get("Genre").toString().split(",");		
		int count =0;
		for(String x : genres){
                        if(count<3){
                                str.append(stripParens(x) + ",");
                                count=count+1;
                        }
                }
                while(count<3){
                        str.append(" " +",");
                        count=count+1;
                }
		///TOMATO RATING///
                str = str.append(movie.get("tomatoMeter")+ ",");
		///IMDB RATING///
                str = str.append(movie.get("imdbRating")+ ",");
		
                return str.toString();
	}
	
	/**
     	 * Removes parantheses and extra leading spaces from given string.
     	 * @param str
     	 * @return String without parentheses and extra leading spaces.
     	 */
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
