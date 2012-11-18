package org.sexmeter.android;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;

import android.location.Location;

public class RoccoAPI extends GenericAPI {
	public final static String DOMAIN = "localhost";
	public final static String SITE_BASE_URL = "http://" + DOMAIN;
	public final static String BASE_URL = "http://192.168.43.121:8080/";

	private DefaultHttpClient mClient;
	private Location currentLocation;

	public static RoccoAPI INSTANCE = new RoccoAPI();

	private RoccoAPI() {
		// try {
		// InetAddress.getByName(DOMAIN);
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// }
		BasicHttpParams params = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 8080));
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory
				.getSocketFactory();
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		this.mClient = new DefaultHttpClient(cm, params);
	}

	public void addStatistic(String km, String device_id)
			throws ClientProtocolException, IOException {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("km", km));
		params.add(new BasicNameValuePair("device_id", device_id));

		if (this.currentLocation != null) {
			params.add(new BasicNameValuePair("lat", this.currentLocation
					.getLatitude() + ""));
			params.add(new BasicNameValuePair("lng", this.currentLocation
					.getLongitude() + ""));
		}

		HttpGet request = makeRequest("add", params);
		this.mClient.execute(request);
	}
	
	public String getPersonalStatistics(String device_id) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id", device_id));
		HttpGet request = makeRequest("me", params);
		HttpResponse response = null;
		try {
			response = this.mClient.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parseResponseToString(response);
	}

	public void setCurrentLocation(Location location) {
		this.currentLocation = location;
	}

	public Location getCurrentLocation() {
		return this.currentLocation;
	}

	//
	// private String executeRequest(List<NameValuePair> params) throws
	// ClientProtocolException, IOException {
	// HttpGet request = makeRequest(params);
	// HttpResponse response = null;
	// try {
	// response = this.mClient.execute(request);
	// } catch (Exception e) {
	// }
	// if (response != null)
	// return parseResponseToString(response);
	// else
	// return "";
	// }

	private HttpGet makeRequest(String method, List<NameValuePair> params) {
		String paramString = URLEncodedUtils.format(params, "utf-8");
		System.out.println(BASE_URL + method + "?" + paramString);
		HttpGet request = new HttpGet(BASE_URL + method + "?" + paramString);
		return request;
	}
}
