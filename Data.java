
public class Data {
	private String identity;
	private String value;
	
	public static String padRight(String s, int n) {   //Courtesy of RealHowTo on http://stackoverflow.com/questions/388461/how-can-i-pad-a-string-in-java/391978#391978
        return String.format("%1$-" + n + "s", s);  //for pretty printing
    }
	
	public void setIdentity(String theIdentity) {
		this.identity = theIdentity;
	}
	
	public void setValue(String theValue) {
		this.value = theValue;
	}
	
	public String getIdentity() {
		return this.identity;
	}
	
	public String getValue() {
		return this.value;
	}

	public void printData() {
		System.out.println(padRight("Identity: " +identity, 21)  +" value: " +value);
	}
	
	public Data(String theIdentity, String theValue) {
		identity = theIdentity;
		value = theValue;
	}
}