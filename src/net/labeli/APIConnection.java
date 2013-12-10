package net.labeli;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.net.JSONParser;

import com.labeli.activities.Product;
import com.labeli.user.UserProperties;

public abstract class APIConnection {

	private static String ip = "labeli.org/api";

	// USER
	private static String urlUsers = "http://" + ip + "/user/login";

	// STOCKS
	private static String urlGetCanettes = "http://" + ip + "/stocks/get";
	private static String urlNewCanettes = "http://" + ip + "/stocks/new";
	private static String urlUpdateCanettes = "http://" + ip + "/stocks/update";
	private static String urlSetCount = "http://" + ip + "/stocks/setCount";
	private static String urlDeleteProduct = "http://" + ip + "/stocks/delete";

	// LOCAL
	private static String urlGetLocal = "http://" + ip + "/local/get";
	private static String urlChangeLocal = "http://" + ip + "/local/toggle";

	// - TAGS
	public static final String TAG_CODE = "code";
	public static final String TAG_NAME = "name";
	public static final String TAG_COUNT = "count";
	public static final String TAG_PRICE = "price";
	public static final String TAG_PERMISSION = "Level";
	public static final String TAG_LOGO = "logo";
	public static final String TAG_USER = "username";
	public static final String TAG_PASSWORD = "password";

	// OTHERS

	public static final int BOOLEAN_TRUE = 0;
	public static final int BOOLEAN_FALSE = 1;
	public static final int ERROR_VALUE = -1;

	private static JSONParser jParser;

	private static JSONObject makeHttpRequestToObject(String url, String method, List<NameValuePair> params){
		jParser = new JSONParser();
		return jParser.makeHttpRequest(url, method, params);
	}

	private static JSONArray makeHttpRequestToArray(String url, String method, List<NameValuePair> params){
		jParser = new JSONParser();
		return jParser.makeHttpRequestArray(url, method, params);
	}

	private static boolean makeHttpRequestToBoolean(String url, String method, List<NameValuePair> params){
		jParser = new JSONParser();
		return convertToBoolean(jParser.makeHttpRequestString(url, method, params));
	}

	/**
	 * Vérifie si la chaîne est un booléen. Elle supprime tout les espaces et les \n de la chaine avant la vérification.
	 * @param bool
	 * @return
	 */
	private static boolean convertToBoolean(String bool){
		return Boolean.valueOf(bool.replace(" ", "").replace("\n", ""));
	}

	public static int getPermissionLevelForUser(String user, String password){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(TAG_USER, user));
		params.add(new BasicNameValuePair(TAG_PASSWORD, password));

		JSONObject json = makeHttpRequestToObject(urlUsers, "GET", params);
		int level = ERROR_VALUE;

		if (json!=null){
			try {
				level = json.getInt("level");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return level;
	}
	
	public static boolean verifyUser(String user, String password){
		if (getPermissionLevelForUser(user, password) >= UserProperties.PERMISSION_USER)
			return true;
		return false;
	}

	public static Vector<Product> getProducts() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Vector<Product> productsList = new Vector<Product>();

		JSONArray json = makeHttpRequestToArray(urlGetCanettes, "GET", params);

		if (json == null)
			return null;
		try {
			for (int i = 0; i < json.length(); i++) {
				JSONObject tmp = json.getJSONObject(i);

				Product p = new Product(tmp.getString(TAG_CODE), tmp.getString(TAG_NAME), tmp.getInt(TAG_COUNT), tmp.getDouble(TAG_PRICE));
				productsList.add(p);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return productsList;
	}

	public static boolean addProduct(String name, String count, String code, String price) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(TAG_NAME, name));
		params.add(new BasicNameValuePair(TAG_CODE, code));
		params.add(new BasicNameValuePair(TAG_PRICE, price));
		params.add(new BasicNameValuePair(TAG_LOGO, ""));

		boolean success = makeHttpRequestToBoolean(urlNewCanettes, "GET", params);

		if (success){
			return setCount(code, count);
		}

		return false;
	}

	public static boolean setCount(String code, String count){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(TAG_CODE, code));
		params.add(new BasicNameValuePair(TAG_COUNT, count));

		return makeHttpRequestToBoolean(urlSetCount, "GET", params);
	}

	public static boolean updateProduct(String code, String type, String nombre) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(TAG_CODE, code));
		params.add(new BasicNameValuePair(TAG_NAME, type));
		params.add(new BasicNameValuePair(TAG_COUNT, nombre));

		return makeHttpRequestToBoolean(urlUpdateCanettes, "GET", params);
	}

	public static Product getProductWithCode(String code) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("code", code));
		Product tmp = null;

		JSONObject result = makeHttpRequestToObject(urlGetCanettes, "GET", params);
		
		try {
			if (result != null){
				tmp = new Product(result.getString(TAG_CODE), result.getString(TAG_NAME), 
						result.getInt(TAG_COUNT), result.getDouble(TAG_PRICE));

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return tmp;
	}

	public static boolean deleteProduct(String code){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(TAG_CODE, code));

		return makeHttpRequestToBoolean(urlDeleteProduct, "GET", params);
	}

	public static boolean getState(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		return makeHttpRequestToBoolean(urlGetLocal, "GET", params);
	}

	public static boolean toggleState(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		return makeHttpRequestToBoolean(urlChangeLocal, "GET", params);
	}

}
