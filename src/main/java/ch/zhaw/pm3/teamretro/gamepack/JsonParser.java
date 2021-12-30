package ch.zhaw.pm3.teamretro.gamepack;

import org.json.JSONObject;

/**
 * An abstraction over the org.json parser.
 */
public class JsonParser {

	/**
	 * The from us preferred indent used to pretty print the json strings.
	 */
	public static final int INDENT = 2;

	/**
	 * This constructor is private by definition of this being a purely static
	 * class.
	 */
	private JsonParser() {

	}

	/**
	 * Will convert the string of the parameter to a correct json object.
	 * 
	 * @param jsonString the json data to convert
	 * @return the deserialized object
	 */
	public static JSONObject stringToJSONObject(String jsonString) {
		return new JSONObject(jsonString);
	}

	/**
	 * Will serialize the given object to a indented json string.
	 * 
	 * @param object the object to serialize
	 * @return the serialized json string
	 */
	public static String objToJson(Object object) {
		return new JSONObject(object).toString(INDENT);
	}
}
