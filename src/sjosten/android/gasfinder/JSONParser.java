package sjosten.android.gasfinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {	
	public static String getJSONFromURL(String url) {
		InputStream input = null;
		
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		} catch(IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		
		Scanner scan = new Scanner(input);
		String json = "";
		while(scan.hasNextLine()) {
			json += scan.nextLine() + "\n";
		}
		
		scan.close();
		
		return json;
	}
	
	public static String getEncodedString(String json) {
		try {
			JSONObject jsonObj = new JSONObject(json);
			JSONArray routeArray = jsonObj.getJSONArray("routes");
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject polylines = routes.getJSONObject("overview_polyline");
			return polylines.getString("points");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
}
