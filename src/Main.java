import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by ashishk on 12/7/15.
 */
public class Main {
    public static void main(String args[]) throws Exception{

        List<Double> equity_returns = new ArrayList<Double>();
        List<Double> gold_returns = new ArrayList<Double>();
        List<Double> gsec_returns = new ArrayList<Double>();
        List<Double> portfolio_standard_deviation = new ArrayList<Double>();
        List<Double> expected_return = new ArrayList<Double>();



        System.out.print("Enter the No. of months as return : ");

        Scanner sc = new Scanner(System.in);
        int Period = sc.nextInt();
        FileWriter fp = new FileWriter("Sample_Portfolio_42_months_Conservative.csv");

        // Return calculation for the each assets

        fp.append("Total_Portfolio_Variance,Portfolio_return,Equity_weight,gold_weight,gsec_weight \n");


        System.out.println("The return period in Year is :" + Period);
        System.out.println("-----------------------------------------------");
        equity_returns = EquityReturns(Period);
        gold_returns = GoldReturns(Period);
        gsec_returns = GsecReturn(Period);


        for (int i = 0; i <equity_returns.size() ; i++) {
            System.out.println("Equity returns   :" + equity_returns.get(i));

        }
        for (int i = 0; i <gold_returns.size() ; i++) {
            System.out.println("Gold returns   :" + gold_returns.get(i));

        }
        for (int i = 0; i <gsec_returns.size() ; i++) {
            System.out.println("Gsec returns   :" + gsec_returns.get(i));

        }

        double return_equity_variance = variance(equity_returns);
        double return_gold_variance = variance(gold_returns);
        double return_gsec_variance = variance(gsec_returns);


        int loop = 0;
        int randomTotal ;
        double portfolio_variance;
        Random rand = new Random();
        double normalised_value=0;
       //do {

            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive

            List<Double> Weight = aggressive();





            List<List<Double>> all_asset = new ArrayList<List<Double>>();
            all_asset.add(equity_returns);
            all_asset.add(gold_returns);
            all_asset.add(gsec_returns);


            double[][] covariacne_matrix = new double[all_asset.size()][all_asset.size()];

            for (int i = 0; i < all_asset.size(); i++) {
                for (int j = 0; j < all_asset.size(); j++) {
                    covariacne_matrix[i][j] = covariance(all_asset.get(i), all_asset.get(j));
                }

            }

            System.out.println("Co-Variance Matrix is : ");
            for (int row = 0; row < covariacne_matrix.length; row++) {
                for (int col = 0; col < covariacne_matrix[row].length; col++) {
                    System.out.printf("%-4.15f\t", covariacne_matrix[row][col]);
                }
                System.out.println();
            }
            portfolio_variance = Math.pow(Weight.get(0), 2) * return_equity_variance + Math.pow(Weight.get(1), 2) * return_gold_variance + Math.pow(Weight.get(2), 2) * return_gsec_variance +
                    2 * Weight.get(0) * Weight.get(1) * covariance(equity_returns, gold_returns) +
                    2 * Weight.get(1) * Weight.get(2) * covariance(gold_returns, gsec_returns) +
                    2 * Weight.get(0) * Weight.get(2) * covariance(equity_returns, gsec_returns);


            System.out.print("Portfolio Variance  :" + portfolio_variance);


            fp.append(Double.toString(portfolio_variance) + ",");

            double return_mean=0.0;

            return_mean = Weight.get(0) * mean(equity_returns) + Weight.get(1) * mean(gold_returns) + Weight.get(2) * mean(gsec_returns);
           portfolio_standard_deviation.add(Math.sqrt(portfolio_variance));
           expected_return.add(return_mean);

            System.out.println(" \t Portfolio Return  : " + return_mean + "  Equity Weight  : " + Weight.get(0) + "  Gold Weight  : " + Weight.get(1) + "  Gsec Weight  : " + Weight.get(2));

            fp.append(Double.toString(return_mean) + ",");
           fp.append(Double.toString(Weight.get(0))+","+Double.toString(Weight.get(1))+","+Weight.get(2)+"\n");

       //}while (loop++<1000);

       fp.append("Slope : "+Double.toString(linear_regression(portfolio_standard_deviation,expected_return)));

        fp.close();
    }

    public static double linear_regression(List<Double> std_dev, List<Double> exp_return){
        double linear_regression= 0.0;
        double std_dev_avg =0.0;
        for (int i = 0; i < std_dev.size() ; i++) {
            std_dev_avg += std_dev.get(i);
        }
        std_dev_avg /= std_dev.size();
        double exp_return_avg = 0.0;

        for (int i = 0; i < exp_return.size() ; i++) {
            exp_return_avg += exp_return.get(i);
        }
        exp_return_avg /= exp_return.size();
        double multiplication = 0.0;
        double divi =0.0;
        for(int i=0, j=0; i < std_dev.size() && j < exp_return.size(); i++, j++){
            multiplication += (std_dev.get(i)-std_dev_avg)*(exp_return.get(j)-exp_return_avg);
            divi += Math.pow((std_dev.get(i)-std_dev_avg), 2);
        }
        linear_regression = multiplication/divi;

        return linear_regression;
    }

    public static List<Double> EquityReturns(int Period) throws Exception{
        List <Double> price = new ArrayList<Double>();
        List<Date> List_of_dates = new ArrayList<Date>();



        try (
                // Step 1: Allocate a database "Connection" object
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/test", "root", "root"); // MySQL

                // Step 2: Allocate a "Statement" object in the Connection
                Statement stmt = conn.createStatement();
        ) {
            // Step 3: Execute a SQL SELECT query, the query result
            //  is returned in a "ResultSet" object.
            String strSelect;



            // Step 4: Process the ResultSet by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
            SimpleDateFormat String_to_Date = new SimpleDateFormat("yyyy-MM-dd");



            Calendar c = Calendar.getInstance();
            String Start_period_date = "2014-02-28";
            String End_period_date = "2004-04-01";
            c.setTime(String_to_Date.parse(Start_period_date));
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");

            DateTime start = format.parseDateTime(Start_period_date);
            DateTime end = format.parseDateTime(End_period_date);

            List_of_dates.add(String_to_Date.parse(Start_period_date));
            //System.out.println(Years.yearsBetween(start,end).getYears());

            while(Months.monthsBetween(end, start).getMonths() > Period){
                c.add(Calendar.MONTH, -Period);
                Start_period_date = String_to_Date.format(c.getTime());
                List_of_dates.add(String_to_Date.parse(Start_period_date));
                start = format.parseDateTime(Start_period_date);

            }
            Collections.reverse(List_of_dates);


            //for (int i = 0; i < List_of_dates.size() ; i++) {
            //  System.out.println(List_of_dates.get(i));

            //}

            int rowCount = 0;
            for (int i = 0; i <List_of_dates.size() ; i++) {
                strSelect = "SELECT *\n " +
                        "FROM asset_data \n" +
                        "WHERE `DateTime`< \'" + String_to_Date.format(List_of_dates.get(i)) +"\' OR `DateTime` = \'"+ String_to_Date.format(List_of_dates.get(i)) + "\' \n"+
                        "ORDER BY `DateTime` DESC\n" +
                        "LIMIT 3;" ;
                System.out.println(strSelect);

                ResultSet rset = stmt.executeQuery(strSelect);

                if(rset.next()){
                    price.add(rset.getDouble("price"));
                }
            }

            for (int i = 0; i < List_of_dates.size() ; i++) {
                System.out.println("Equity Date : " + String_to_Date.format(List_of_dates.get(i)) +  "\tPrice : " + price.get(i));

            }



        }
        List<Double> equity_returns =  new ArrayList<Double>();
        equity_returns = ReturnCalculations(price, Period);
        return equity_returns;


    }

    public static List<Double> GoldReturns (int Period) throws Exception{

        List <Double> price = new ArrayList<Double>();
        List<Date> List_of_dates = new ArrayList<Date>();



        try (
                // Step 1: Allocate a database "Connection" object
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/test", "root", "root"); // MySQL

                // Step 2: Allocate a "Statement" object in the Connection
                Statement stmt = conn.createStatement();
        ) {
            // Step 3: Execute a SQL SELECT query, the query result
            //  is returned in a "ResultSet" object.
            String strSelect;



            // Step 4: Process the ResultSet by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
            SimpleDateFormat String_to_Date = new SimpleDateFormat("yyyy-MM-dd");



            Calendar c = Calendar.getInstance();
            String Start_period_date = "2014-02-28";
            String End_period_date = "2004-04-01";
            c.setTime(String_to_Date.parse(Start_period_date));
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");

            DateTime start = format.parseDateTime(Start_period_date);
            DateTime end = format.parseDateTime(End_period_date);

            List_of_dates.add(String_to_Date.parse(Start_period_date));
            //System.out.println(Years.yearsBetween(start,end).getYears());

            while(Months.monthsBetween(end, start).getMonths() > Period){
                c.add(Calendar.MONTH, -Period);
                Start_period_date = String_to_Date.format(c.getTime());
                List_of_dates.add(String_to_Date.parse(Start_period_date));
                start = format.parseDateTime(Start_period_date);

            }
            Collections.reverse(List_of_dates);


//            for (int i = 0; i < List_of_dates.size() ; i++) {
//                System.out.println(List_of_dates.get(i));
//
//            }

            int rowCount = 0;
            for (int i = 0; i <List_of_dates.size() ; i++) {
                strSelect = "SELECT *\n " +
                        "FROM gold_data \n" +
                        "WHERE `Date`< \'" + String_to_Date.format(List_of_dates.get(i)) +"\' OR `Date` = \'"+ String_to_Date.format(List_of_dates.get(i)) + "\' \n"+
                        "ORDER BY `Date` DESC\n" +
                        "LIMIT 3;" ;
                //System.out.println(strSelect);

                ResultSet rset = stmt.executeQuery(strSelect);

                if(rset.next()){
                    price.add(rset.getDouble("Gold"));
                }
            }

            for (int i = 0; i < List_of_dates.size() ; i++) {
                System.out.println("Gold Date : " + String_to_Date.format(List_of_dates.get(i)) +  "\tPrice : " + price.get(i));

            }



        }
        List<Double> gold_returns =  new ArrayList<Double>();
        gold_returns = ReturnCalculations(price, Period);
        return gold_returns;


    }

    public static List<Double> GsecReturn (int Period) throws Exception{

        List <Double> price = new ArrayList<Double>();
        List<Date> List_of_dates = new ArrayList<Date>();



        try (
                // Step 1: Allocate a database "Connection" object
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/test", "root", "root"); // MySQL

                // Step 2: Allocate a "Statement" object in the Connection
                Statement stmt = conn.createStatement();
        ) {
            // Step 3: Execute a SQL SELECT query, the query result
            //  is returned in a "ResultSet" object.
            String strSelect;



            // Step 4: Process the ResultSet by scrolling the cursor forward via next().
            //  For each row, retrieve the contents of the cells with getXxx(columnName).
            SimpleDateFormat String_to_Date = new SimpleDateFormat("yyyy-MM-dd");



            Calendar c = Calendar.getInstance();
            String Start_period_date = "2014-02-28";
            String End_period_date = "2005-11-30";
            c.setTime(String_to_Date.parse(Start_period_date));
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");

            DateTime start = format.parseDateTime(Start_period_date);
            DateTime end = format.parseDateTime(End_period_date);

            List_of_dates.add(String_to_Date.parse(Start_period_date));
            //System.out.println(Years.yearsBetween(start,end).getYears());

            while(Months.monthsBetween(end, start).getMonths() > Period){
                c.add(Calendar.MONTH, -Period);
                Start_period_date = String_to_Date.format(c.getTime());
                List_of_dates.add(String_to_Date.parse(Start_period_date));
                start = format.parseDateTime(Start_period_date);

            }
            Collections.reverse(List_of_dates);


//            for (int i = 0; i < List_of_dates.size() ; i++) {
//                System.out.println(List_of_dates.get(i));
//
//            }

            int rowCount = 0;
            for (int i = 0; i <List_of_dates.size() ; i++) {
                strSelect = "SELECT *\n " +
                        "FROM gsec_data \n" +
                        "WHERE `Date`< \'" + String_to_Date.format(List_of_dates.get(i)) +"\' OR `Date` = \'"+ String_to_Date.format(List_of_dates.get(i)) + "\' \n"+
                        "ORDER BY `Date` DESC\n" +
                        "LIMIT 3;" ;
                //System.out.println(strSelect);

                ResultSet rset = stmt.executeQuery(strSelect);

                if(rset.next()){
                    price.add(rset.getDouble("Close"));
                }
            }

            for (int i = 0; i < List_of_dates.size() ; i++) {
                System.out.println("Gsec Date : " + String_to_Date.format(List_of_dates.get(i)) +  "\tPrice : " + price.get(i));

            }



        }
        List<Double> Gsec_returns =  new ArrayList<Double>();
        Gsec_returns = ReturnCalculations(price, Period);
        return Gsec_returns;


    }


    public static List<Double> ReturnCalculations(List<Double> values, int Period){
        int len = values.size();
        List<Double> returns = new ArrayList<Double>();
        for (int i = 1; i < (len) ; i++) {
            returns.add(Math.pow(values.get(i)/values.get(i-1), 12.0/Period) - 1 );
        }
        return returns;
    }

    public static double mean(List<Double> a){

        double return_mean = 0;
        for (int i = 0; i < a.size(); i++) {
            return_mean = return_mean + a.get(i);

        }
        return return_mean/a.size();


    }

    public static double variance(List<Double> a){
        double vari = 0;
        double mean = mean(a);
        for (int i = 0; i < a.size() ; i++) {
            vari = vari + (a.get(i)-mean)*(a.get(i) -mean);
        }
        vari = vari / (a.size());
        return vari;

    }
    public static double coorel (List<Double> array1, List<Double> array2){
        int i = array1.size();
        int j = array2.size();
        double mean1 = mean(array1);
        double mean2 = mean(array2);

        double cov = 0;
        double divi =0;
/*
        for(int i1=0, j1 =0; i1 < i && j1 < i ; i1++, j++){
            cov += (array1[i1]-mean1)*(array2[j1]-mean2);
            divi += (array1[i1]-mean1)*(array2[j1]-mean2)*(array1[i1]-mean1)*(array2[j1]-mean2);
        }
        return (cov/Math.sqrt(divi));*/

        return  cov;

    }

    public static double covariance(List<Double> array1, List<Double> array2){
        int i = array1.size();
        int j = array2.size();
        double mean1=mean(array1);
        double mean2=mean(array2);

        double cov = 0;

        for(int i1=0, j1 =0; i1 < i && j1 < j ; i1++, j1++){
            cov += (array1.get(i1)-mean1)*(array2.get(j1)-mean2);
        }
        cov = (double)cov / (i);
        return cov;
    }

    public static List<Double> conservative () {

        //E - 10-30%; D - 50-80%; G - 0-20%
        Random rand = new Random();
        List<Double> randomNumber = new ArrayList<Double>();
        while (true) {
            randomNumber.clear();

            randomNumber.add(randomInRange(0.1, 0.3));
            randomNumber.add(randomInRange(0.0, 0.2));
            randomNumber.add(1.0 - randomNumber.get(0) - randomNumber.get(1));
            if ((randomNumber.get(randomNumber.size()-1) >= 0.5) && (randomNumber.get(randomNumber.size()-1) <= 0.8))
            break;
        }


        return randomNumber;



    }

    public static List<Double> aggressive () {

        //E - 10-30%; D - 50-80%; G - 0-20%
        //E - 50-80%; D - 10-30%; G - 0-20%

        Random rand = new Random();
        List<Double> randomNumber = new ArrayList<Double>();
        while(true){
            randomNumber.clear();
            randomNumber.add(randomInRange(0.5, 0.8));
            randomNumber.add(randomInRange(0.0, 0.2));
            randomNumber.add(1.0 - randomNumber.get(0)-randomNumber.get(1));
            if ((randomNumber.get(randomNumber.size()-1) >= 0.1) && (randomNumber.get(randomNumber.size()-1) <= 0.3))
                break;
        }


        return randomNumber;
    }

    public static double randomInRange(double min, double max) {
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextDouble() * (max-min)) + min;
    }


}
