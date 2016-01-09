/**
 * Created by ashishk on 1/2/16.
 */

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DateCalculation {

    String[] assets;
    boolean setAsset () throws SQLException{
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


            strSelect = "select (count(*)) from mytable;";

            ResultSet r = stmt.executeQuery(strSelect);
            if (r.next())
                endDateCalculation = r.getInt("Date");

            System.out.println("the end date for " + asset_name + "  :" + endDateCalculation);
        return true;

    }
    public static void main(String args[]) throws Exception{

        List<Date> dates = dateCalculation(12,3);
        for (int i = 0; i < dates.size(); i++) {
            System.out.println(dates.get(i));
        }




    }

    public static List<Date> dateCalculation(int period, int delta) throws Exception{

        List<Date> d = new ArrayList<Date>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat String_to_Date = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = "2004-04-01";
        String endDate = String_to_Date.format(c.getTime());
        System.out.println("Start Date : " + startDate + "\nEnd Date :"+endDate);
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime start = format.parseDateTime(startDate);
        DateTime end = format.parseDateTime(endDate);
        System.out.println("Months between start and end : "+ Months.monthsBetween(start, end).getMonths());
        while(Months.monthsBetween(start,end).getMonths() > 12) {
            c.setTime(String_to_Date.parse(startDate));
            d.add(c.getTime());
            c.add(Calendar.MONTH, period);
            d.add(c.getTime());
            c.add(Calendar.MONTH, (delta-period));
            c.setTime(c.getTime());
            startDate = String_to_Date.format(c.getTime());
            start = format.parseDateTime(startDate);
        }

        return d;


    }
}
