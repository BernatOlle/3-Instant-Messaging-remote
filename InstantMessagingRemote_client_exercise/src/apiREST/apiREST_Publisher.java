package apiREST;

import com.google.gson.Gson;
import util.Message;
import java.io.*;
import java.net.*;

public class apiREST_Publisher {
  
  public static boolean publish(Message message) {
    try {
      URL url = new URL(Cons.SERVER_REST + "/publisher/publish");
      HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

      ucon.setRequestMethod("POST");
      ucon.setDoInput(true);
      ucon.setDoOutput(true);
      ucon.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      ucon.setRequestProperty("Accept", "application/json; charset=utf-8");

      PrintWriter out = new PrintWriter(ucon.getOutputStream(), true);
      String json = new Gson().toJson(message);
      System.out.println("publish: "+json);
      out.println(json);
      out.flush();
      ucon.connect();

      BufferedReader in = new BufferedReader(new InputStreamReader(ucon.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
