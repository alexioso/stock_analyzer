import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class readHTML{

	private static String regex = "\\w*<td>(\\S+)</td>";



		public static void main(String args[]) throws IOException{ 
			BufferedReader br = new BufferedReader(new FileReader("template.html"));

                        String s;
                        
			while ((s = br.readLine()) != null) {
                            String[] line = s.split("<td>");
                            if(line.length == 1){System.out.println(line[0]}
                            else{
                                System.out.print(line[0];
                                if(line[1].contains("center"){
                                {
                                    System.out.print("<center>"+"WHOOPESH"+
                                }
                            }

                        }

			br.close();






		}














}
