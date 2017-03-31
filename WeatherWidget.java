import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;

public class Practical4 {
	
	public static Scanner userInput = new Scanner(System.in); //used to get user input
	public static ArrayList<Data> dataBase = new ArrayList<Data>(); //used to store values and their identity 	

	public static int index; //used to keep track of section of dataBase to search from
	public static String previousTime; //used to find next day in dataBase
	public static String userChoice1; //keeps track of user choice for current weather or 5 day
	
	public static String getLocation(String userChoice) {
    	String location = " ";
        System.out.println("Getting location");
    	boolean loop = true;
    	while(loop == true) { //if input is incorrect, displays error window and repeats question
    		if (userChoice.equals("1")) { //if user wants to search based on city name
            	String cityName = JOptionPane.showInputDialog("Please enter the name of the city: "); //displays inquiry in another window
                location = "q=" +cityName; //sets up part of URL for city name
                loop = false; //doesn't need to repeat question
       	 	}
    		else if (userChoice.equals("2")) { //if user wants to search based on coordinates
            	String latitude = JOptionPane.showInputDialog("Please enter the Latitude: ");
            	String longitude = JOptionPane.showInputDialog("Please enter the Longitude: ");
            	location = "lat=" +latitude +"&lon=" +longitude; //sets up coordinates for URL
                loop = false;
            }
            else {
           	 	JOptionPane.showMessageDialog(null,"Incorrect Input");
            }
    	}    	
    	String newLocation = deleteSpaces(location); //deletes all spaces that may have been input by user
        return newLocation; //returns location part of URL
    }
    
	public static String deleteSpaces(String location) { //built in shortcut wasn't working, so I made this to delete spaces
        StringBuilder spaceSkipper = new StringBuilder(); 
    	for (int i = 0; i < location.length(); i++) {
			char currentChar = location.charAt(i);
			if (currentChar != ' ') { //add any characters that aren't space characters
				spaceSkipper.append(currentChar);
			}
		}
    	location = spaceSkipper.toString();
		return location; //return spaceless location
	}

	public String makeRESTCall(String strURL) throws MalformedURLException, IOException {
        URL url = new URL(strURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //Specify that we are expecting JSON data to be returned
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        //200 is the 'OK' response code. This method may also return 401 for an unauthorised request, or -1 if the response is not valid HTTP
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        //Create reader to read response from the server
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        //Using a StringBuilder is more time and memory efficient, when the size of the concatenated String could be very large
        StringBuilder buffer = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            buffer.append(output);
        }
        conn.disconnect();

        return buffer.toString();
    }

public static void formatResults(String result) { //takes raw data received from URL, and processes it into storable data
		
		ArrayList<String> stringStore = new ArrayList<String>(); //stores String of both identity and value
		StringBuilder sb = new StringBuilder(); //used to make strings stored in the stringStore
		for (int k = 0; k < result.length(); k++) {
			char currentChar = result.charAt(k); //scans entire raw data character by character
			if (currentChar == '}' || currentChar == ',') { //adds whatever came before end bracket or comma to stringStore
				stringStore.add(sb.toString());
			}
			if (currentChar == '{' || currentChar == ',') { //starts new string following bracket or comma
				sb.setLength(0);
			}
			else {
				sb.append(currentChar); //adds any characters in between
			}
		}
		for (int y = 0; y < stringStore.size(); y++) {
			String noQuotes = stringStore.get(y).replaceAll("\"", ""); //takes away all quotation marks
			stringStore.set(y, noQuotes); //replaces string with the quoteless string
			String[] dataSplitter = new String[2]; //stores identifier and value of individual data 
			dataSplitter = stringStore.get(y).split(":"); //splits it
			if (dataSplitter.length > 1) { //incase there is any unwanted data that made it this far
				Data newData = new Data(dataSplitter[0], dataSplitter[1]); //sets up new data
				dataBase.add(newData); //stores new data
			}
		}
	}
	
	public static String padRight(String s, int n) {   //Courtesy of RealHowTo on http://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java/391978#391978
        return String.format("%1$-" + n + "s", s);  //for pretty printing
    }
	
	public static void displayDailyWeather() { //Displays today's weather by printing message in another window
		JOptionPane.showMessageDialog(null, padRight("Status: ",  20) +findValue("description", 0) //
		+"\n" +padRight("Temperature: ", 20) +(Math.round(Double.parseDouble(findValue("temp", 0))-273))  +"° C" //rounds converted double and converts from kelvin to celcius
		+"\n" +padRight("Pressure: ", 20) +findValue("pressure", 0) +" hPa"
		+"\n" +padRight("Humidity: ", 20) +findValue("humidity", 0) +"%"
		+"\n" +padRight("Minimum Temp: ", 20) +(Math.round(Double.parseDouble(findValue("temp_min", 0))-273)) +"° C"
		+"\n" +padRight("Maximum Temp: ", 20) +(Math.round(Double.parseDouble(findValue("temp_max", 0))-273)) +"° C"
		+"\n" +padRight("Wind Speed: ", 20) +findValue("speed", 0) +"m/s"
		+"\n" +padRight("Wind Direction: ", 20) +findValue("deg", 0) +"°"
		+"\n" +padRight("Cloud Cover: ", 20) +findValue("all", 0) +"%"
		+"\n" +padRight("Rain: ", 20) +findValue("3h", 0) +" inches the last 3 hours"
		+"\n" +padRight("Sunrise: ", 20) +epochDate(Long.parseLong(findValue("sunrise", 0))) //converts date from epoch seconds to actual date
		+"\n" +padRight("Sunset: ", 20) +epochDate(Long.parseLong(findValue("sunset", 0))),
		"Weather report for " +findValue("name", 0)  +", " +findValue("country" , 0) +" today:", //adds apporpriate title to window
		JOptionPane.PLAIN_MESSAGE);
	}
	
	public static String epochDate(long seconds) { //uses date object to convert seconds to actual date
		Date expiry = new Date(seconds * 1000);
		String timeFromSun = expiry.toString();
		return timeFromSun;
	}
	
	public static String findValue(String identity, int index) { //uses identity string to find value of specific data in dataBase
		String value = "";
		for (int i = index; i < dataBase.size(); i++) { //Index is used for keeping track of the day and time from 
			if (dataBase.get(i).getIdentity().equals(identity)) { //finds first dataBase index where user identity input is found
				value = dataBase.get(i).getValue(); //returns value for the identity the user wanted
				i = dataBase.size(); //stops searching and saves processing time
			}
		}
		if (value.equals("")) {
			value = "Not Available"; //returns not available if value isn't available from API
		}
		return value;
	}
	
	public static void displayFiveDayWeather() { //displays window for each time from each day of five day forecast
		JOptionPane.showMessageDialog(null, "Date and time:        "+dataBase.get(index).getValue() +" o'clock"
		+"\n" +padRight("Status: ",  20) +findValue("description", index)
		+"\n" +padRight("Temperature: ", 20) +(Math.round(Double.parseDouble(findValue("temp", index))-273))  +"° C"
		+"\n" +padRight("Pressure: ", 20) +findValue("pressure", index) +" hPa"
		+"\n" +padRight("Humidity: ", 20) +findValue("humidity", index) +"%"
		+"\n" +padRight("Minimum Temp: ", 20) +(Math.round(Double.parseDouble(findValue("temp_min", index))-273)) +"° C"
		+"\n" +padRight("Maximum Temp: ", 20) +(Math.round(Double.parseDouble(findValue("temp_max", index))-273)) +"° C"
		+"\n" +padRight("Wind Speed: ", 20) +findValue("speed", index) +"m/s"
		+"\n" +padRight("Wind Direction: ", 20) +findValue("deg", index) +"°"
		+"\n" +padRight("Cloud Cover: ", 20) +findValue("all", index) +"%"
		+"\n" +padRight("Rain: ", 20) +findValue("3h", index) +" inches the last 3 hours",
		"Weather report for " +findValue("name", 0)  +", " +findValue("country" , 0) +" at " +":", 
		JOptionPane.PLAIN_MESSAGE);
	}
	
	public static int findDayInfo() { //finds next time/day index for five day forecast and returns it
		for (int i = index; i < dataBase.size(); i++) {
			if (dataBase.get(i).getIdentity().equals("dt_txt")) { //finds next part where identity is date
				if (!dataBase.get(i).getValue().equals(previousTime)) { //if it's not equal to the previous time printed, returns that index
					index = i;
					i = dataBase.size(); //stops looking
				}
			}
		}
		return index;
	}
	
	public static void main(String[] args) {
        Practical4 client = new Practical4();
      
        try {
        	boolean locationFound = false;
        	while (locationFound == false) {
            	boolean validChoice = false;
            	System.out.println("Displaying Inquiries");
            	StringBuilder URLBuilder = new StringBuilder();
            	String userChoice1 = "";
            	while (validChoice == false) { //Loops if input is incorrect
            		userChoice1 = JOptionPane.showInputDialog("Choose an information type (1: Current weather, 2: 5-day forecast): ");
                	if (userChoice1.equals("1")) { //Creates first part of URL for today's weather
                		URLBuilder.append("http://api.openweathermap.org/data/2.5/weather?");
                		validChoice = true;
                	}
                	else if (userChoice1.equals("2")) { //creates first part of URL for five day forecast
                		URLBuilder.append("http://api.openweathermap.org/data/2.5/forecast?");
                		validChoice = true;
                	}
                	else {
                   	 	JOptionPane.showMessageDialog(null,"Incorrect Input");
                	}
            	}    
            	System.out.println();
            	boolean validChoice1 = false;
            	while (validChoice1 == false) {
            		String userChoice = JOptionPane.showInputDialog("Choose an option  1: City name 2: geographical coordinates ");
                	if (userChoice.equals("1") || userChoice.equals("2")) {
                		URLBuilder.append(getLocation(userChoice)); //creates URL for location depending on user input
                		userInput.close();
                     	URLBuilder.append("&appid=081deb30631bc70d0862eb0262276daf"); //finishes URL with ID
                    	String URL = URLBuilder.toString();
                        String result = client.makeRESTCall(URL); //makes rest call to the created URL
                        locationFound = true; //stops looping 
                        System.out.println("\nResponse from Server...");
                        formatResults(result); //converts raw data into usable data objects
                        if (userChoice1.equals("1")) {
                        	System.out.println("Displaying Weather");
                        	displayDailyWeather(); //Displays the current weather info
                        	System.out.println("Weather Displayed");
                        }
                        if (userChoice1.equals("2")) {
                        	index = 0;
                        	System.out.println("Displaying Weather");
                        	for (int k = index; k < dataBase.size(); k++) { //goes through date & times to find specific forecast for 5 days
                        		if (index != 0) {
                        			displayFiveDayWeather(); //Displays information for every three hours for five days
                        		}
                        		findDayInfo(); //searches dataBase for next date/time and returns its index
                        		previousTime = dataBase.get(index).getValue();
                        	}
                        	System.out.println("Weather Displayed");
                        }
                        validChoice1 = true;
                	}
                	else {
                   	 	JOptionPane.showMessageDialog(null,"Incorrect Input");          
                   	}
            	}
        	}
        }
        catch(NullPointerException e) {
       	 	JOptionPane.showMessageDialog(null,"City not found correctly");
        }
        catch(NumberFormatException e) {
        	
        }
        catch(MalformedURLException e) {
            System.out.println("Problem encountered with the URL" +e.getMessage());
            System.out.println();
        }
        catch(IOException e) {
            System.out.println("IOException encountered during communication: " +e.getMessage());
            System.out.println();
        }
    }
}