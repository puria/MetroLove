package org.sexmeter.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An abstract class that gives you some facility for generic JSON Services.
 * 
 * @author <a href="mailto:puria.nafisi@axant.it">Puria Nafisi Azizi</a>
 * 
 */
public abstract class GenericAPI {
	private static final String BASE_URL = "http://www.test.com/";

	/**
	 * Returns the base url of the service from wich you build the service (eg.
	 * http://www.test.com/).
	 * 
	 * @return BASE_URL
	 */
	public static String getBaseUrl() {
		return BASE_URL;
	}

	protected static JSONObject parseResponseToJSON(HttpResponse response) throws JSONException {
		return new JSONObject(parseResponseToString(response));
	}

	protected static String parseResponseToString(HttpResponse response) {
		BufferedReader in = null;
		try {
			InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), "windows-1252");
			in = new BufferedReader(isr, 8 * 1024);
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String content = new String(sb);
			return content;
		} catch (IllegalStateException e) {
			return "";
		} catch (IOException e) {
			return "";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
