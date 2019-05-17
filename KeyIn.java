package tlh;

import java.util.Scanner;

import static tlh.ETClientApp.out;

public class KeyIn {
	
	 private static Scanner scanner = new Scanner( System.in );
	 
	 public static int getKeyInInteger() throws RuntimeException{
		 String input = scanner.nextLine();
		 int choice = 0;
		 if (input == null  || input.length() == 0 || input.equalsIgnoreCase("x")) {
			 choice = 'x';
		 } else {
			 try {
				 choice = Integer.valueOf(input);
			 } catch (Exception e) {
				 out.println("Invalid input, please enter valid number");
				 choice = getKeyInInteger();
			 }
		 }
		 return choice;
	 }
	 
	 public static double getKeyInDouble() throws RuntimeException{
		 String input = scanner.nextLine();
		 double value = 0;
		 try {
			 value = Double.valueOf(input);
		 } catch (Exception e) {
			 out.println("Invalid input");
			 getKeyInDouble();
		 }
		 return value;
	 }
	 
	 public static String getKeyInString() throws RuntimeException{
		 String input = scanner.nextLine();
		
		 return input;
	 }
	 
	 public static void close() {
		 scanner.close();
	 }
}
