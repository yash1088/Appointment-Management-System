package Client;/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Prati
 */
public class For_logs {

    public static void client_logs(String client_id, String request, String reply) throws IOException {

        final String dir = System.getProperty("user.dir");
        String file_name = dir;
        if (client_id.substring(0, 3).equals("MTL")) {
            file_name = dir + "\\src\\Logs\\Client\\MTL\\" + client_id + ".txt";

        } else if (client_id.substring(0, 3).equals("QUE")) {

            file_name = dir + "\\src\\Logs\\Client\\QUE\\" + client_id + ".txt";

        } else {

            file_name = dir + "\\src\\Logs\\Client\\SHE\\" + client_id + ".txt";
        }

        Date date_time = new Date();
        String date_format = "yyyy-mm-dd hh:mm:ss a";
        DateFormat date = new SimpleDateFormat(date_format);
        String date_specific = date.format(date_time);

        FileWriter file_dir = new FileWriter(file_name, true);
        PrintWriter print = new PrintWriter(file_dir);
        print.println("Date:" + date_specific+" , " + "Request:" + request+" , " + "Reply:" +" , " + reply);
        print.close();

    }
    public static void server_logs(String client_id, String request, String params, String reply, String server_name) throws IOException {

        final String dir = System.getProperty("user.dir");
        String file_name = dir;
        if (client_id.substring(0, 3).equals("MTL")) {
            file_name = dir + "\\src\\Logs\\Server\\MTL\\" + client_id + ".txt";

        } else if (client_id.substring(0, 3).equals("QUE")) {

            file_name = dir + "\\src\\Logs\\Server\\QUE\\" + client_id + ".txt";

        } else {

            file_name = dir + "\\src\\Logs\\Server\\SHE\\" + client_id + ".txt";
        }

        Date date_time = new Date();
        String date_format = "yyyy-mm-dd hh:mm:ss a";
        DateFormat date = new SimpleDateFormat(date_format);
        String date_specific = date.format(date_time);

        FileWriter file_dir = new FileWriter(file_name, true);
        PrintWriter print = new PrintWriter(file_dir);
        print.println("Date:" + date_specific+" , " + "Action:" + request+" , "+"Parameters:" + params + "Reply:" +" , " + reply + "Server:"+server_name);
        print.close();

    }
}