/*
Brad Foster
Aleksander Braksator
CPE 365
Dekhtyar
*/

import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import static java.lang.Math.*;
import java.text.*;

public class getQuery 
{
	private static Connection conn;
	private static int target = 1;
	private static String db;

	public static String readQuery(String filename)
	{
		try
		{
			File file = new File(filename);
			Scanner sc = new Scanner(file);
			String ln = "";

			while(sc.hasNextLine())
			{
				ln = ln + sc.nextLine();
				ln = ln + "\n";
			}
			return ln;
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return "Error Reading Individial1";
		}

	}

	public static String analyzeMonths(double[] arr){
		double totalDiff = arr[arr.length - 1] - arr[0];
		if(totalDiff < 0){
			return "Negative Growth";
		}
		else{
			int count = 0;
			double diff[] = new double[arr.length-1];
			for(int i = 0; i < arr.length - 1; i++){
				diff[i] = (arr[i+1] - arr[i])/arr[i];              
			} 
			double percDiff;
			double sum = 0.0;
			for(double d : diff){
				sum += d;
			}
			percDiff = sum;
			if(percDiff < 0.05){
				return("Showing resilience");
			}
			else if(percDiff < 0.15){
				return("Steady Growth");
			}
			else if(percDiff < 3){
				return("Moderately outperforming");
			}
			else if(percDiff < 0.4){
				return("Impressive Growth");
			}
			else{
				return("Tremendously Outperforming");
			}
		}
	}

	public static double maxArr(ArrayList<Double> arr){
	    double max = 0.0;
	    for(double d:arr){
	        if (d > max){
	            max = d;
	        }
	    }
	    return max;
	}

	public static double minArr(ArrayList<Double> arr){
	    double min = 111111110.0;
	    for(double d:arr){
	        if (d < min){
	            min = d;
	        }
	    }
	    return min;
	}


	public static double sd(ArrayList<Double> table){
	    double mean = mean(table);
	    double temp = 0;
	    for(int i = 0; i < table.size(); i++){
	        double val = table.get(i);
	        double sqDiff = Math.pow(val-mean,2);
	        temp += sqDiff;
	    }
	    double meanOfDiffs = (double) temp/(double)(table.size());
	    return Math.sqrt(meanOfDiffs);
	}      

	public static double mean(ArrayList<Double> table){
	    double count = 0.0;
	    for(double d : table){
	        count += d;
	    }
	    return count/(double)table.size();
	}

	public static ArrayList<String> individualStockQuery5(String ticker) throws SQLException
	{
		String[] dates = {"2014-8-01", "2015-01-01", "2015-06-01", "2015-08-01", "2016-01-01", "2016-05-01", "2016-08-01"};
                ArrayList<String> results = new ArrayList<String>();
		for(int i = 1; i < dates.length; i++){
			String startDate = dates[i-1];
			String endDate = dates[i];
			Statement s5 = conn.createStatement();
                        Statement s = conn.createStatement();
			s5.executeUpdate("use "+db);
			String iq5 = "SELECT Close, Volume\n"+
				"FROM AdjustedPrices\n"+
				"WHERE Ticker = '" + ticker +"' AND\n"+
				"Day >= '" + startDate + "' AND\n"+
				"Day <= '" + endDate + "'";
			String avgVolQuery = "SELECT Avg(Volume)\n"+
				"FROM AdjustedPrices\n"+
				"WHERE Ticker = '" + ticker + "' AND\n"+
				"Day >= '" + startDate + "' AND\n"+
				"Day <= '" + endDate +"'";
			ResultSet result = s5.executeQuery(iq5);
			ResultSet a = s.executeQuery(avgVolQuery);
			a.next();
			double avgVol = a.getDouble(1);
                        boolean f = result.next();
                        ArrayList<Double> prices = new ArrayList<Double>();
                        ArrayList<Double> vol = new ArrayList<Double>();
                        while(f){
                             prices.add(result.getDouble(1));
                             vol.add(result.getDouble(2));
                             f = result.next(); 
                        }
                        //Normalize the price and volume arrayList contents
                        ArrayList<Double> normPrices = new ArrayList<Double>();
                        ArrayList<Double> normVol = new ArrayList<Double>();
                        double meanVol = mean(vol);
                        double meanPrice = mean(prices);
                        double sdVol = sd(vol);
                        double sdPrice = sd(prices);
                        for(double v : vol){
                            normVol.add((v - meanVol)/sdVol);                            
                        }
                        for(double p : prices){
                            normPrices.add((p - meanPrice)/sdPrice);
                        } 
               
                        //implement decision tree
                        int lastDay = normVol.size() - 1;
                        ArrayList<Double> diff = new ArrayList<Double>();
                        for(int d = 0; d < 5; d++){
                            diff.add(normPrices.get(lastDay - d) - normPrices.get(lastDay - d -1));
                        }
                       double last5Diff = diff.get(0) + diff.get(1) + diff.get(2);
                       //If stock has large amount of volume currently
                       if(vol.get(lastDay) >= avgVol){
                            //If stock price has been on the rise in last 5 days
                            if(last5Diff > 0.25){
                               //If volume is non-decreasing, HOLD
                               if(normVol.get(lastDay) >= 0){
                                   results.add(ticker + " on " + dates[i] + ": " + "HOLD");
                                   continue;
                               }

                               //If volume is decreasing, SELL
                               else{
                                   results.add(ticker + " on " + dates[i] + ": " + "SELL");
                                   continue;                                      
                               }
                            }
                            //If stock price has been steady last 5 days
                            else if(last5Diff > -0.25){
                                //If volume is non-decreasing, BUY
                                if(normVol.get(lastDay) >= 0){
                                    results.add(ticker + " on " + dates[i] + ": " + "BUY");
                                    continue;
                                }
                                //If volume is decreasing, SELL
                                else{
                                    results.add(ticker + " on " + dates[i] + ": " + "SELL");
                                    continue;
                                }
                            }
                            //If stock price is sinking
                            else{
                                //If volume is non-decreasing, BUY
                                if(normVol.get(lastDay) >= 0){
                                    results.add(ticker + " on " + dates[i] + ": " + "BUY");
                                    continue;
                                }
                                //If volume is decreasing, HOLD, there may be hope in long run since volume is pretty high
                                else{
                                    results.add(ticker + " on " + dates[i] + ": " + "HOLD");
                                    continue;
                                }  
                            }
                        }
                        //If stock has lower than average volume
                        else{
                            //If stock price has been on the rise in last 5 days
                            if(last5Diff > 0.25){
                               //If volume is non-decreasing, HOLD
                               if(normVol.get(lastDay) >= 0){
                                   results.add(ticker + " on " + dates[i] + ": " + "HOLD");
                                   continue;
                               }
                               //If volume is decreasing, SELL
                               else{
                                   results.add(ticker + " on " + dates[i] + ": " + "HOLD");
                                   continue;
                               }
                            }
                            //If stock price has been steady last 5 days
                            if(last5Diff > -0.25){

                                //If volume is non-decreasing, BUY
                                if(normVol.get(lastDay) >= 0){
                                    results.add(ticker + " on " + dates[i] + ": " + "BUY");
                                    continue;
                                }
                                //If volume is decreasing HOLD
                                else{
                                    results.add(ticker + " on " + dates[i] + ": " + "HOLD");
                                    continue;
                                }
                            }
                            //If stock price is sinking
                            else{
                                //If volume is non-decreasing, BUY
                                if(normVol.get(lastDay) >= 0){
                                    results.add(ticker + " on " + dates[i] + ": " + "BUY");
                                    continue;
                                }
                                //If volume is decreasing, SELL
                                else{
                                    results.add(ticker + " on " + dates[i] + ": " + "SELL");
                                    continue;
                                }
                            }
                        }
		}
            return results;
	}

	public static ArrayList<String> individualStockQuery6(String ticker) throws SQLException{
	    ArrayList<String> shortTerm = new ArrayList<String>();
	    ArrayList<String> longTerm = new ArrayList<String>();
	    String[] dates = {"2015-01-01", "2015-06-01", "2015-08-01", "2016-01-01", "2016-05-01", "2016-08-01", "2016-12-31"};
	    ArrayList<String> results = individualStockQuery5(ticker);
	    for(int i = 0; i < dates.length - 1; i++){
	        String decision = results.get(i);
	        String startDate = dates[i];
	        String endDate = dates[i+1];
	        Statement s6 = conn.createStatement();
	        s6.executeUpdate("use "+db);
	        String iq5 = "SELECT High\n"+
	                     "FROM AdjustedPrices\n"+
	                     "Where Ticker = '"+ticker+"' AND\n"+
	                     "Day >= '"+startDate+"' AND Day <= '"+endDate+"'";
	        ResultSet priceSet = s6.executeQuery(iq5);
	        int f = 0;
	        boolean g = priceSet.next();
	        ArrayList<Double> prices = new ArrayList<Double>();
	        while(f < 10){
	            prices.add(priceSet.getDouble(1));
	            f++;
	            g = priceSet.next();
	        }
	        //Check Short term
	        double maxPrice = maxArr(prices);
	        double minPrice = minArr(prices);
	        if(decision.contains("BUY")){
	            if(maxPrice - prices.get(0) < 0){
	                shortTerm.add("BAD BUY");
	                System.out.println(prices.toString() + ".........." + maxPrice);
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) > 0.1){
	                shortTerm.add("AMAZING BUY");
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) > 0.05){
	                shortTerm.add("Good Short Term Growth");
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) > 0.025){
	                shortTerm.add("Moderate Short Term Growth");
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) >= 0.0){
	                shortTerm.add("Little Short Term Growth");
	            }
	            else{shortTerm.add("PISS POOR BUY");}
	        }           
	        else if(decision.contains("SELL")){
	            if(prices.get(0) - minPrice < 0){
	                shortTerm.add("BAD SELL");
	            }
	            else if(maxPrice - prices.get(0) < 0){
	                shortTerm.add("GREAT SHORT TERM SELL");
	            }
	            else if((prices.get(0) - minPrice)/prices.get(0) > 0.1){
	                shortTerm.add("GREAT SELL");
	            }
	            else if((prices.get(0) - minPrice)/prices.get(0) > 0.05){
	                shortTerm.add("Good short term sale");
	            }
	            else if((prices.get(0) - minPrice)/prices.get(0) > 0.25){
	                shortTerm.add("Moderate short term loss, OK Sale");
	            }
	            else if((prices.get(0) - minPrice)/prices.get(0) >= 0.0){
	                shortTerm.add("Could have held onto");
	            }
	            else{shortTerm.add("PISS POOR SELL");}
	        }
	        else if(decision.contains("HOLD")){
	            if((prices.get(0) - minPrice)/prices.get(0) > 0.1){
	                 shortTerm.add("BAD HOLD");
	                 
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) > 0.1){
	                 shortTerm.add("GOOD SHORT TERM HOLD. SELL SOON");
	            }
	            else if((maxPrice - prices.get(0))/prices.get(0) > 0.05){
	                 shortTerm.add("OK HOLD. CAN KEEP");
	            }
	                
	            else if((prices.get(0) - minPrice)/prices.get(0) > 0.05){
	                 shortTerm.add("So far not good hold");
	            }
	            else if((prices.get(0) - minPrice)/prices.get(0) > 0.025 || (maxPrice - prices.get(0))/prices.get(0) > 0.025){
	                 shortTerm.add("OK HOLD");
	            }
	            else{shortTerm.add("KEEP HOLDING");}
	        }
	        else{shortTerm.add("PUPWAH");}
	    }
	    return shortTerm;
	}

	public static void writeString(String s, FileWriter fw) throws IOException
	{
		for (int index = 0; index < s.length(); index++) 
		{
		    fw.write(s.charAt(index));
		}
	}

	public static void writeDate(java.sql.Date d, FileWriter fw) throws IOException
	{
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
		String s = df.format(d);

		writeString(s, fw);
	}

	public static void writeInt(int i, FileWriter fw) throws IOException
	{
		Integer integer = (Integer)i;
		String s = integer.toString();

		writeString(s, fw);
	}

	public static void writeLong(long l, FileWriter fw) throws IOException
	{
		Long longy = (Long)l;
		String s = longy.toString();

		writeString(s, fw);
	}

	public static void writeFloat(float f, FileWriter fw) throws IOException
	{
		Float floaty = (Float)f;
		String s = floaty.toString();

		writeString(s, fw);
	}


        public static void g1tohtml(ResultSet g1result, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g1.html"));
                int col = 1;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                switch (col)
                                {
                                        case 1 : writeInt(g1result.getInt(1), fw);
                                                col ++;
                                                break;
                                        case 2 : writeInt(g1result.getInt(2), fw);
                                                col ++;
                                                break;
                                        case 3 : writeInt(g1result.getInt(3), fw);
                                                col ++;
                                                break;
                                        case 4 : writeInt(g1result.getInt(4), fw);
                                                col ++;
                                                col = 1;
                                                if(!g1result.isLast())
                                                {
                                                        g1result.next();
                                                }
                                                break;
                                }
                        }

                        else
                        {
                                fw.write(character);
                        }
                }
        }


        public static void g2tohtml(ResultSet g2result, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g2.html"));
                int col = 1;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                switch (col)
                                {
                                        case 1 : writeString(g2result.getString(1), fw);
                                                col ++;
                                                break;
                                        case 2 : writeString(g2result.getString(2), fw);
                                                col ++;
                                                break;
                                        case 3 : writeLong(g2result.getLong(3), fw);
                                                col ++;
                                                col = 1;
                                                if(!g2result.isLast())
                                                {
                                                        g2result.next();
                                                }
                                                break;
                                }
                        }

                        else
                        {
                                fw.write(character);
                        }
                }
        }

        public static void g3atohtml(ResultSet g3result, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g3a.html"));
                int col = 1;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                switch (col)
                                {
                                        case 1 : writeString(g3result.getString(1), fw);
                                                col ++;
                                                break;
                                        case 2 : writeInt(g3result.getInt(2), fw);
                                                col ++;
                                                break;
                                        case 3 : writeFloat(g3result.getFloat(3), fw);
                                                col ++;
                                                col = 1;
                                                if(!g3result.isLast())
                                                {
                                                        g3result.next();
                                                }
                                                break;
                                }
                        }

                        else
                        {
                                fw.write(character);
                        }
                }
        }
    public static void g3btohtml(ResultSet g3result, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g3b.html"));
                int col = 1;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                switch (col)
                                {
                                        case 1 : writeString(g3result.getString(1), fw);
                                                col ++;
                                                break;
                                        case 2 : writeInt(g3result.getInt(2), fw);
                                                col ++;
                                                break;
                                        case 3 : writeFloat(g3result.getFloat(3), fw);
                                                col ++;
                                                col = 1;
                                                if(!g3result.isLast())
                                                {
                                                        g3result.next();
                                                }
                                                break;
                                }
                        }

                        else
                        {
                                fw.write(character);
                        }
                }
        }

    public static void g4tohtml(ResultSet g4result, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g4.html"));
                int col = 1;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                switch (col)
                                {
                                        case 1 : writeString(g4result.getString(col), fw);
                                                col ++;
                                                col = 1;
                                                if(!g4result.isLast())
                                                {
                                                        g4result.next();
                                                }
                                                break;
                                }
                        }

                        else
                        {
                                fw.write(character);
                        }
                }
        }




	public static void g5tohtml(ArrayList<String> arr, FileWriter fw) throws IOException, SQLException
        {
                BufferedReader r = new BufferedReader(new FileReader("g5.html"));
                int col = 0;
                int c;

                while((c = r.read()) != -1)
                {
                        char character = (char) c;

                        if(character == '*')
                        {
                                writeString(arr.get(col), fw);
                                col++;
                        }
                        else
                        {
                                fw.write(character);
                        }
                }
        }

        public static void i1tohtml(ResultSet i1result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i1.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i1result.getString(1), fw);
						col ++;
						break;
					case 2 : writeDate(i1result.getDate(2), fw);
						col ++;
						break;
					case 3 : writeDate(i1result.getDate(3), fw);
						col = 1;
						if(!i1result.isLast())
						{
							i1result.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i2tohtml(ResultSet i2result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i2.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i2result.getString(1), fw);
						col ++;
						break;
					case 2 : writeInt(i2result.getInt(2), fw);
						col ++;
						break;
					case 3 : writeFloat(i2result.getFloat(3), fw);
						col ++;
						break;
					case 4 : writeFloat(i2result.getFloat(4), fw);
						col ++;
						break;
					case 5 : writeLong(i2result.getLong(5), fw);
						col ++;
						break;
					case 6 : writeFloat(i2result.getFloat(6), fw);
						col ++;
						break;
					case 7 : writeFloat(i2result.getFloat(7), fw);
						col = 1;
						if(!i2result.isLast())
						{
							i2result.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i3atohtml(ResultSet i3aresult, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i3a.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i3aresult.getString(1), fw);
						col ++;
						break;
					case 2 : writeFloat(i3aresult.getFloat(2), fw);
						col ++;
						break;
					case 3 : writeFloat(i3aresult.getFloat(3), fw);
						col ++;
						break;
					case 4 : writeFloat(i3aresult.getFloat(4), fw);
						col = 1;
						if(!i3aresult.isLast())
						{
							i3aresult.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i3btohtml(ResultSet i3bresult, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i3b.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i3bresult.getString(1), fw);
						col ++;
						break;
					case 2 : writeFloat(i3bresult.getFloat(2), fw);
						col = 1;
						if(!i3bresult.isLast())
						{
							i3bresult.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i4tohtml(ResultSet i4result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i4.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i4result.getString(1), fw);
						col ++;
						break;
					case 2 : writeInt(i4result.getInt(2), fw);
						col ++;
						break;
					case 3 : writeInt(i4result.getInt(3), fw);
						col ++;
						break;
					case 4 : writeFloat(i4result.getFloat(4), fw);
						col = 1;
						if(!i4result.isLast())
						{
							i4result.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i5tohtml(ArrayList<String> i5result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i5.html"));
		int c;
		int i = 0;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				writeString(i5result.get(i), fw);
				i++;
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i6tohtml(ArrayList<String> i6result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i6.html"));
		int c;
		int i = 0;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				writeString(i6result.get(i), fw);
				i++;
			}

			else
			{
				fw.write(character);
			}
		}
	}


	public static void i7atohtml(ResultSet i7aresult, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i7a.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeString(i7aresult.getString(1), fw);
						col = 1;
						if(!i7aresult.isLast())
						{
							i7aresult.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i7btohtml(ResultSet i7bresult, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i7b.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeInt(i7bresult.getInt(1), fw);
						col ++;
						break;
					case 2 : writeInt(i7bresult.getInt(2), fw);
						col ++;
						break;
					case 3 : writeString(i7bresult.getString(3), fw);
						col ++;
						break;
					case 4 : writeFloat(i7bresult.getFloat(4), fw);
						col ++;
						break;
					case 5 : writeString(i7bresult.getString(5), fw);
						col ++;
						break;
					case 6 : writeFloat(i7bresult.getFloat(6), fw);
						col ++;
						break;
					case 7 : writeFloat(i7bresult.getFloat(7), fw);
						col = 1;
						if(!i7bresult.isLast())
						{
							i7bresult.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i7ctohtml(ResultSet i7cresult, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i7c.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeInt(i7cresult.getInt(1), fw);
						col ++;
						break;
					case 2 : writeInt(i7cresult.getInt(2), fw);
						col ++;
						break;
					case 3 : writeString(i7cresult.getString(3), fw);
						col ++;
						break;
					case 4 : writeLong(i7cresult.getLong(4), fw);
						col ++;
						break;
					case 5 : writeString(i7cresult.getString(5), fw);
						col ++;
						break;
					case 6 : writeLong(i7cresult.getLong(6), fw);
						col ++;
						break;
					case 7 : writeLong(i7cresult.getLong(7), fw);
						col = 1;
						if(!i7cresult.isLast())
						{
							i7cresult.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void i8tohtml(ResultSet i8result, FileWriter fw) throws IOException, SQLException
	{
		BufferedReader r = new BufferedReader(new FileReader("i8.html"));
		int col = 1;
		int c;

		while((c = r.read()) != -1)
		{
			char character = (char) c;

			if(character == '*')
			{
				switch (col)
				{
					case 1 : writeInt(i8result.getInt(1), fw);
						col ++;
						break;
					case 2 : writeString(i8result.getString(2), fw);
						col ++;
						break;
					case 3 : writeFloat(i8result.getFloat(3), fw);
						col ++;
						break;
					case 4 : writeString(i8result.getString(4), fw);
						col ++;
						break;
					case 5 : writeFloat(i8result.getFloat(5), fw);
						col ++;
						break;
					case 6 : writeFloat(i8result.getFloat(6), fw);
						col = 1;
						if(!i8result.isLast())
						{
							i8result.next();
						}
						break;
				}
			}

			else
			{
				fw.write(character);
			}
		}
	}

	public static void main(String args[]) throws IOException, SQLException
	{

		BufferedReader br = new BufferedReader(new FileReader("credentials.in"));

		String user = br.readLine();
		String password = br.readLine();
		db = br.readLine();

		br.close();

		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception ex)
		{
			System.out.println("Driver not found");
			System.out.println(ex);
		};

		String url = "jdbc:mysql://cslvm74.csc.calpoly.edu/"+user+"?";

		conn = null;
		try { 
			conn = DriverManager.getConnection(url +"user="+user+"&password="+password);

		}
		catch (Exception ex)
		{
			System.out.println("Could not open connection");
			System.out.println(ex);
		};

		System.out.println("Connected");
		//GeneralQuery1();
		//GeneralQuery4();
		//GeneralQuery5();`

		String ticker = args[0];
		String ticker2 = args[1];

		System.out.println("TICKER IS " + ticker);
		System.out.println();

		String outputFileName = ticker + ".html";

		FileWriter fw = new FileWriter(outputFileName);

		writeString("<!DOCTYPE html> \n", fw);
		writeString("<html> \n", fw);
		writeString("<body> \n", fw);
                writeString("<h1>"+ticker+" Stock Analysis</h1>", fw);

//GENERAL 1 ----------------------------------------------------------------

		System.out.println("Executing Query: General 1");

		try
		{
			Statement sg1 = conn.createStatement();
			sg1.executeUpdate("use "+db);
			String gq1 = "SELECT C1 AS 'start of 2016',\n"+
				"C2 AS 'end of 2016',\n"+
				"C3 AS 'Price increase during 2016',\n"+
				"C4 AS 'Price deccrease during 2016'\n"+
				"FROM \n"+
				"(SELECT COUNT(*) AS C1 \n"+
				"FROM Prices \n"+
				"WHERE Day = (SELECT MIN(Day) FROM Prices WHERE Year(Day) = 2016)) T1, \n"+
				"(SELECT COUNT(*) AS C2\n"+
				"FROM Prices\n"+
				"WHERE Day = (SELECT MAX(Day) FROM Prices WHERE Year(Day) = 2016)) T2,\n"+
				"(SELECT COUNT(*) AS C3\n"+
				"FROM Prices p1, Prices p2\n"+
				"WHERE p1.Day = (SELECT MAX(Day) FROM Prices WHERE Year(Day) = 2015) AND\n"+
				"p2.Day = (SELECT MAX(Day) FROM Prices WHERE Year(Day) = 2016) AND\n"+
				"p1.Ticker = p2.Ticker AND\n"+
				"p1.Close < p2.Close) T3,\n"+
				"(SELECT COUNT(*) AS C4\n"+
				"FROM Prices p1, Prices p2\n"+
				"WHERE p1.Day = (SELECT MAX(Day) FROM Prices WHERE Year(Day) = 2015) AND\n"+
				"p2.Day = (SELECT MAX(Day) FROM Prices WHERE Year(Day) = 2016) AND\n"+
				"p1.Ticker = p2.Ticker AND\n"+
				"p1.Close > p2.Close) T4";

			ResultSet result1 = sg1.executeQuery(gq1);
			boolean f = result1.next();
                        g1tohtml(result1, fw);

			while(f)
			{
				String r1 = result1.getString(1);
				String r2 = result1.getString(2);
				String r3 = result1.getString(3);
				String r4 = result1.getString(4);
				System.out.println(r1+","+r2+","+r3+","+r4);
				f=result1.next();
			}
		}

		catch (Exception eg1)
		{
			eg1.printStackTrace();
		}

		System.out.println();

//GENERAL 2 ----------------------------------------------------------------
		System.out.println("Executing Query: General 2");
		String g2 = "";
		g2 = readQuery("general2.SQL");

		try
		{
			Statement sg2 = conn.createStatement();

			sg2.executeUpdate("use nyse");
			ResultSet g2result = sg2.executeQuery(g2);
			boolean f = g2result.next();

			System.out.println("Ticker, Name, TotalVolumeTraded");
                        g2tohtml(g2result, fw);
			while(f)
			{
				String t = g2result.getString(1);
				String n = g2result.getString(2);
				long v = g2result.getLong(3);
				System.out.println(t + ", " + n + ", " + v);
				f = g2result.next();
			}
		}

		catch (Exception eg2)
		{
			eg2.printStackTrace();
		}

		System.out.println();

//GENERAL 3a ----------------------------------------------------------------
		System.out.println("Executing Query: General 3a");
		String g3a = "";
		g3a = readQuery("general3a.SQL");

		try
		{
			Statement sg3a = conn.createStatement();

			sg3a.executeUpdate("use nyse");
			ResultSet g3aresult = sg3a.executeQuery(g3a);
			boolean f = g3aresult.next();

			System.out.println("Ticker, Year, AbsoluteIncrease");
                        g3atohtml(g3aresult, fw);
			while(f)
			{
				String t = g3aresult.getString(1);
				int y = g3aresult.getInt(2);
				float ai = g3aresult.getFloat(3);
				System.out.println(t + ", " + y + ", " + ai);
				f = g3aresult.next();
			}
		}

		catch (Exception eg3a)
		{
			eg3a.printStackTrace();
		}

		System.out.println();

//GENERAL 3b ----------------------------------------------------------------
		System.out.println("Executing Query: General 3b");
		String g3b = "";
		g3b = readQuery("general3b.SQL");

		try
		{
			Statement sg3b = conn.createStatement();

			sg3b.executeUpdate("use nyse");
			ResultSet g3bresult = sg3b.executeQuery(g3b);
			boolean f = g3bresult.next();

			System.out.println("Ticker, Year, RelativeIncrease");
                        g3btohtml(g3bresult, fw);
			while(f)
			{
				String t = g3bresult.getString(1);
				int y = g3bresult.getInt(2);
				float ri = g3bresult.getFloat(3);
				System.out.println(t + ", " + y + ", " + ri);
				f = g3bresult.next();
			}
		}

		catch (Exception eg3b)
		{
			eg3b.printStackTrace();
		}

		System.out.println();

//GENERAL 4 -------------------------------------------------------------------

		System.out.println("Executing Query: General 4");

		try
		{
			Statement sg4 = conn.createStatement();
			sg4.executeUpdate("use "+db);
			String gq4 = "SELECT a1.Ticker\n"+
			             "FROM AdjustedPrices a1, AdjustedPrices a2, AdjustedPrices aa1, AdjustedPrices aa2\n"+
			             "WHERE a1.Day = (SELECT MIN(Day) FROM AdjustedPrices WHERE YEAR(Day) = 2016) AND\n"+
			             "a2.Day = a1.Day AND a2.Ticker <> a1.Ticker AND\n"+
			             "aa1.Day = (SELECT MAX(Day) FROM AdjustedPrices WHERE YEAR(Day) = 2016) AND\n"+
			             "aa2.Day = aa1.Day AND a1.Ticker = aa1.Ticker AND\n"+
			             "a2.Ticker = aa2.Ticker\n"+
			             "GROUP BY a1.Ticker\n"+
			             "ORDER BY AVG(aa1.Close/aa2.Close - a1.Open/a2.Open) DESC\n"+
			             "LIMIT 10";
			ResultSet r = sg4.executeQuery(gq4);
			boolean f = r.next();
			g4tohtml(r, fw);
                        while(f)
			{
			    System.out.println(r.getString(1));
			    f = r.next();
			}
		}

		catch (Exception eg4)
		{
			eg4.printStackTrace();
		}

		System.out.println();

//GENERAL 5 -------------------------------------------------------------------

		System.out.println("Executing Query: General 5");

		try
		{
			Statement s5 = conn.createStatement();
			s5.executeUpdate("use "+db);
			String gq5 = "SELECT Sector, month(day), AVG(Open), AVG(Volume)\n"+
				"FROM Securities NATURAL JOIN AdjustedPrices\n"+
				"WHERE Year(Day) = 2016 AND Sector <> 'Telecommunications Services'\n"+
				"GROUP BY Sector, month(day)\n";
			ResultSet result = s5.executeQuery(gq5);
			double cD[] = new double[12];
			double cS[] = new double[12];
			double energy[] = new double[12];
			double health[] = new double[12];
			double ind[] = new double[12];
			double finance[] = new double[12];
			double materials[] = new double[12];
			double util[] = new double[12];
			double iT[] = new double[12];
			double rE[] = new double[12];

			boolean f = result.next();
			while(f){
				int month = result.getInt(2);

				if(result.getString(1).equals("Health Care")){ 
					health[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Industrials")){ 
					ind[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Consumer Discretionary")){
					cD[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Information Technology")){
					iT[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Consumer Staples")){
					cS[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Utilities")){
					util[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Financials")){
					finance[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Real Estate")){ 
					rE[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Materials")){
					materials[month-1] = result.getDouble(3);
				}
				else if(result.getString(1).equals("Energy")){
					energy[month-1] = result.getDouble(3);
				}
				else{System.out.print("ur code sucks\n"+result.getString(1));}
				f = result.next();
			} 
                        ArrayList<String> rg5 = new ArrayList<String>();
                        rg5.add("Consumer Discretionary"); 
			rg5.add(analyzeMonths(cD));
                        rg5.add("Consumer Staples");
			rg5.add(analyzeMonths(cS));
                        rg5.add("Energy");
			rg5.add(analyzeMonths(energy));
                        rg5.add("Health Care");
			rg5.add(analyzeMonths(health));
                        rg5.add("Industrials");
			rg5.add(analyzeMonths(ind));
                        rg5.add("Financials");
			rg5.add(analyzeMonths(finance));
                        rg5.add("Real Estate");
			rg5.add(analyzeMonths(rE));
                        rg5.add("Utilities");
			rg5.add(analyzeMonths(util));
                        rg5.add("Industrial Technology");
			rg5.add(analyzeMonths(iT));
                        rg5.add("Materials");
			rg5.add(analyzeMonths(materials));
                        g5tohtml(rg5, fw);
		}

		catch (Exception eg5)
		{
			eg5.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 1 ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 1");
		String i1 = "";
		i1 = readQuery("individual1.SQL");
                writeString("<h2>Individual Stock Report for "+ticker+"</h2>",fw);
                writeString("<hr>",fw);
		try
		{
			PreparedStatement ps1 = conn.prepareStatement(i1);

			ps1.setString(1, ticker);
			ps1.setString(2, ticker);

			ps1.executeUpdate("use nyse");
			ResultSet i1result = ps1.executeQuery();
			boolean f = i1result.next();

			System.out.println("Ticker, EarliestDate, LatestDate");

			i1tohtml(i1result, fw);


			while(f)
			{
				String s = i1result.getString(1);
				java.sql.Date d1 = i1result.getDate(2);
				java.sql.Date d2 = i1result.getDate(3);
				System.out.println(s + ", " + d1 + ", " + d2);
				f = i1result.next();
			}
		}

		catch (Exception ei1)
		{
			ei1.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 2 ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 2");
		String i2 = "";
		i2 = readQuery("individual2.SQL");
                writeString("<hr>",fw);

		try
		{
			PreparedStatement ps2 = conn.prepareStatement(i2);

			ps2.setString(1, ticker);
			ps2.setString(2, ticker);
			ps2.setString(3, ticker);
			ps2.setString(4, ticker);
			ps2.setString(5, ticker);

			ps2.executeUpdate("use nyse");
			ResultSet i2result = ps2.executeQuery();
			boolean f = i2result.next();

			i2tohtml(i2result, fw);

			System.out.println("Ticker, Year, AbsoluteChange, RelativeChange, TotalVolume, AverageVolume, AverageClosing");

			while(f)
			{
				String s = i2result.getString(1);
				int y = i2result.getInt(2);
				float ach = i2result.getFloat(3);
				float rc = i2result.getFloat(4);
				long tv = i2result.getLong(5);
				float av = i2result.getFloat(6);
				float acl = i2result.getFloat(7);
				System.out.println(s + ", " + y + ", " + ach + ", " + rc + ", " + tv + ", " + av + ", " + acl);
				f = i2result.next();
			}
		}

		catch (Exception ei2)
		{
			ei2.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 3a ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 3a");
		String i3a = "";
		i3a = readQuery("individual3a.SQL");
                writeString("<hr>",fw);
		PreparedStatement ps3a = conn.prepareStatement(i3a);
                try
		{
			ps3a.setString(1, ticker);
			ps3a.setString(2, ticker);
			ps3a.setString(3, ticker);

			ps3a.executeUpdate("use nyse");
			ResultSet i3aresult = ps3a.executeQuery();
			boolean f = i3aresult.next();

			System.out.println("Ticker, AverageClosing, HighestPrice, LowestPrice");

			i3atohtml(i3aresult, fw);

			while(f)
			{
				String s = i3aresult.getString(1);
				float acl = i3aresult.getFloat(2);
				float hp = i3aresult.getFloat(3);
				float lp = i3aresult.getFloat(4);
				System.out.println(s + ", " + acl + ", " + hp + ", " + lp);
				f = i3aresult.next();
			}
		}

		catch (Exception ei3a)
		{
			ei3a.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 3b ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 3b");
		String i3b = "";
		i3b = readQuery("individual3b.SQL");

		try
		{
			PreparedStatement ps3b = conn.prepareStatement(i3b);

			ps3b.setString(1, ticker);

			ps3b.executeUpdate("use nyse");
			ResultSet i3bresult = ps3b.executeQuery();
			boolean f = i3bresult.next();

			System.out.println("Month, AverageVolume");

			i3btohtml(i3bresult, fw);


			while(f)
			{
				int m = i3bresult.getInt(1);
				float av = i3bresult.getFloat(2);
				System.out.println(m + ", " + av);
				f = i3bresult.next();
			}
		}

		catch (Exception ei3b)
		{
			ei3b.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 4 ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 4");
		String i4 = "";
		i4 = readQuery("individual4.SQL");
                writeString("<hr>",fw);
                PreparedStatement ps4 = conn.prepareStatement(i4);
		try
		{
			ps4.setString(1, ticker);
			ps4.setString(2, ticker);
			ps4.setString(3, ticker);
			ps4.setString(4, ticker);
			ps4.setString(5, ticker);
			ps4.setString(6, ticker);

			ps4.executeUpdate("use nyse");
			ResultSet i4result = ps4.executeQuery();
			boolean f = i4result.next();

			System.out.println("Ticker, Year, Month, PriceChange");

			i4tohtml(i4result, fw);

			while(f)
			{
				String s = i4result.getString(1);
				int y = i4result.getInt(2);
				int m = i4result.getInt(3);
				float pc = i4result.getFloat(4);
				System.out.println(s + ", " + y + ", " + m + ", " + pc);
				f = i4result.next();
			}
		}

		catch (Exception ei4)
		{
			ei4.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 5 ----------------------------------------------------------------
                writeString("<hr>",fw);

		System.out.println("Executing Query: Individual 5");
		i5tohtml(individualStockQuery5(ticker), fw);
		System.out.println(individualStockQuery5(ticker).toString());

		System.out.println();


//INDIVIDUAL 6 ----------------------------------------------------------------
                writeString("<hr>",fw);

		System.out.println("Executing Query: Individual 6");
		i6tohtml(individualStockQuery6(ticker), fw);
		System.out.println(individualStockQuery6(ticker).toString());

		System.out.println();

//INDIVIDUAL 7a ----------------------------------------------------------------
                writeString("<hr>",fw);

		System.out.println("Executing Query: Individual 7a");
		String i7a = "";
		i7a = readQuery("individual7a.SQL");

		try
		{
			Statement s7 = conn.createStatement();

			s7.executeUpdate("use nyse");
			ResultSet i7aresult = s7.executeQuery(i7a);
			boolean f = i7aresult.next();

			System.out.println("Ticker");

			i7atohtml(i7aresult, fw);

			while(f)
			{
				String s = i7aresult.getString(1);
				System.out.println(s);
				f = i7aresult.next();
			}
		}

		catch (Exception ei7a)
		{
			ei7a.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 7b ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 7b");
		String i7b = "";
		i7b = readQuery("individual7b.SQL");

		try
		{
			PreparedStatement ps7b = conn.prepareStatement(i7b);

			ps7b.setString(1, ticker);
			ps7b.setString(2, ticker);
			ps7b.setString(3, ticker);

			ps7b.executeUpdate("use nyse");
			ResultSet i7bresult = ps7b.executeQuery();
			boolean f = i7bresult.next();

			System.out.println("Year, Month, Ticker1, Ticker1Change, Ticker2, Ticker2Change, ChangeDifference");

			i7btohtml(i7bresult, fw);

			while(f)
			{
				int y = i7bresult.getInt(1);
				int m = i7bresult.getInt(2);
				String t1 = i7bresult.getString(3);
				float t1c = i7bresult.getFloat(4);
				String t2 = i7bresult.getString(5);
				float t2c = i7bresult.getFloat(6);
				float cd = i7bresult.getFloat(7);
				System.out.println(y + ", " + m + ", " + t1 + ", " + t1c + ", " + t2 + ", " + t2c + ", " + cd);
				f = i7bresult.next();
			}
		}

		catch (Exception e7b)
		{
			e7b.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 7c ----------------------------------------------------------------
		System.out.println("Executing Query: Individual 7c");
		String i7c = "";
		i7c = readQuery("individual7c.SQL");

		try
		{
			PreparedStatement ps7c = conn.prepareStatement(i7c);

			ps7c.setString(1, ticker);

			ps7c.executeUpdate("use nyse");
			ResultSet i7cresult = ps7c.executeQuery();
			boolean f = i7cresult.next();

			System.out.println("Year, Month, Ticker1, Ticker1Volume, Ticker2, Ticker2Volume, VolumeDifference");

			i7ctohtml(i7cresult, fw);

			while(f)
			{
				int y = i7cresult.getInt(1);
				int m = i7cresult.getInt(2);
				String t1 = i7cresult.getString(3);
				long t1v = i7cresult.getLong(4);
				String t2 = i7cresult.getString(5);
				long t2v = i7cresult.getLong(6);
				long vd = i7cresult.getLong(7);
				System.out.println(y + ", " + m + ", " + t1 + ", " + t1v + ", " + t2 + ", " + t2v + ", " + vd);
				f = i7cresult.next();
			}
		}

		catch (Exception e7c)
		{
			e7c.printStackTrace();
		}

		System.out.println();

//INDIVIDUAL 8 ----------------------------------------------------------------
                writeString("<hr>",fw);

		System.out.println("Executing Query: Individual 8");
		String i8 = "";
		i8 = readQuery("individual8.SQL");
                String betterStock = "NULL";
		try
		{
			PreparedStatement ps8 = conn.prepareStatement(i8);

			ps8.setString(1, ticker);
			ps8.setString(2, ticker2);

			ps8.executeUpdate("use nyse");
			ResultSet i8result = ps8.executeQuery();
			boolean f = i8result.next();

			System.out.println("Year, Ticker1, Ticker1PriceChange, Ticker2, Ticker2PriceChange, Difference");

			i8tohtml(i8result, fw);
			while(f)
			{
				int y = i8result.getInt(1);
				String t1 = i8result.getString(2);
				float t1c = i8result.getFloat(3);
				String t2 = i8result.getString(4);
				float t2c = i8result.getFloat(5);
				float d = i8result.getFloat(6);
                                if(d > 0){
                                    betterStock = args[0];
                                }
                                else{betterStock = args[1];}
				System.out.println(y + ", " + t1 + ", " + t1c + ", " + t2 + ", " + t2c + ", " + d);
				f = i8result.next();
			}
		}

		catch (Exception e8)
		{
			e8.printStackTrace();
		}

		System.out.println();
                writeString("<p> Better Stock in 2016: " + betterStock + "</p>", fw);

		writeString("</body> \n", fw);
		writeString("</html> \n", fw);

		fw.close();
	}
}
