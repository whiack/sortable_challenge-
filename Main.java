import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/*
 * the result of that need to be outputed
 */
class Listing{
	String title;
	String manufacturer;
	String currency;
	String price;
	
	Listing(String t, String m , String c, String p) {
		title = t;
		manufacturer = m;
		currency = c ;
		price = p;
	}
}


public class Main {
	static HashMap<String,ArrayList<Listing>> result = new HashMap<String,ArrayList<Listing>>();
	//use model, family, manufaturer to find out the product_name
	static HashMap<ArrayList<String>, String> product_name_map = new HashMap<ArrayList<String>, String>();
	//manufacturer and list of {model, family} pair to reduce the search time
	static HashMap<String, ArrayList<ArrayList<String> > > man_map = new HashMap<String, ArrayList<ArrayList<String>>>();
	
	//search sub_string within the String. get info that we need
	public static String getHashKey(String line, String element) {
		
		//the beginning of the information should follow the tag+ ":"
		int name_begin = line.indexOf(element) + element.length() + 3;
		int name_end = 0;

		//if we need to consider "sony canada" and "sony" as the same company 
		//we just need to remove everything after the space, I assume there are no "canada sony"
		for( name_end = name_begin; line.charAt(name_end) != '"' /*&& line.charAt(name_end) != ' '*/; name_end ++) {}
		String key = line.substring(name_begin, name_end);
		return key;
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		File file_products = new File("/Users/blaite/Desktop/sortable_challenge/products.txt");
		Scanner scanner_products = new Scanner(file_products);
		
		File file_list = new File("/Users/blaite/Desktop/sortable_challenge/listings.txt");
		Scanner scanner_list = new Scanner(file_list);
		
		String product_name;
		String manufacturer;
		String model;
		String family;
		
		//get information from products file
		try {
			while(scanner_products.hasNextLine()) {
				String line = scanner_products.nextLine();
				product_name = getHashKey(line, "product_name");
				manufacturer = getHashKey(line, "manufacturer").toLowerCase();
				model = getHashKey(line, "model").toLowerCase();	
				family = getHashKey(line, "family").toLowerCase();
				
				ArrayList<String> mmf = new ArrayList<String>(); //arraylist of manufaturer, model and family
				ArrayList<String> model_family = new ArrayList<String>();
				
				mmf.add(manufacturer);
				mmf.add(model);
				mmf.add(family);
				
				model_family.add(model);
				model_family.add(family);
				
				product_name_map.put(mmf, product_name);
				
				if(man_map.get(manufacturer) != null) {
					ArrayList<ArrayList<String>> mf = man_map.get(manufacturer);
					mf.add(model_family);
					man_map.put(manufacturer, mf);
				}
				else {
					ArrayList<ArrayList<String>> mf = new ArrayList<ArrayList<String>>();
					mf.add(model_family);
					man_map.put(manufacturer, mf);
				}
			}
			
		} finally {
			scanner_products.close();
		}
				
		String title;//will be convert to lower case
		String manufacturer_l;
		String title_o; //the original string to output
		String manufacturer_l_o;
		String currency;
		String price;
		
		//get information from list file
		try {
			while(scanner_list.hasNextLine()) {
				String line = scanner_list.nextLine();
				title_o = getHashKey(line, "title");
				manufacturer_l_o = getHashKey(line, "manufacturer");
				title = title_o.toLowerCase();
				manufacturer_l = manufacturer_l_o.toLowerCase();
				currency = getHashKey(line, "currency");	
				price = getHashKey(line, "price");
				
				//based on manufacturer. looking for the specific item
				//in the products file
				if(man_map.get(manufacturer_l) != null) {
					ArrayList<ArrayList<String>> mf = man_map.get(manufacturer_l);
					
					for(int i = 0 ; i < mf.size(); i ++ ) {
						//I assume model and family will be seperated by space
						String m = ' ' + mf.get(i).get(0) + ' ';
						String f = ' ' + mf.get(i).get(1) + ' ';
						
						if(title.contains(m) && title.contains(f)) {
							ArrayList<String> mmf = new ArrayList<String>();
							
							mmf.add(manufacturer_l);
							//added the model and family without spaces
							mmf.add(mf.get(i).get(0));
							mmf.add(mf.get(i).get(1));

							if(product_name_map.get(mmf) != null) {
								String p_name = product_name_map.get(mmf);								
								Listing l = new Listing(title_o, manufacturer_l_o, currency, price);
								
								if(result.get(p_name) != null) {
									ArrayList<Listing> lsts = result.get(p_name);
									lsts.add(l);
									result.put(p_name,lsts);
								}
								else {
									ArrayList<Listing> lsts = new ArrayList<Listing>();
									lsts.add(l);
									result.put(p_name,lsts);
	
								}
							}
							else {
								// System.out.println(-1);
							}
							
						}
						else {
//							System.out.println("unfound model family");
						}
						
					}
					
				;}
				else {
//					System.out.println("unfound manufacturer");
				}
			}
			
			//making JSONObject
			ArrayList<JSONObject> obj_list = new ArrayList<JSONObject>();
			
			Iterator it = result.entrySet().iterator();
			// PrintWriter writer = new PrintWriter("match_result.txt", "UTF-8");
			for (Map.Entry<String, ArrayList<Listing>> entry : result.entrySet()) {
				JSONObject obj = new JSONObject();
		        String key = entry.getKey().toString();;
		        ArrayList<Listing> value = entry.getValue();
		        
		        obj.put("product name", key);
		        JSONArray lsting = new JSONArray();

		        for(int i = 0 ; i < value.size(); i++ ) {
		        	Listing l = value.get(i);

		        	JSONObject element = new JSONObject();
		        	element.put("title", l.title);
		        	element.put("manufacturer", l.manufacturer);
		        	element.put("currency", l.currency);
		        	element.put("price", l.price);
		        	lsting.add(element);

		        }
		        obj.put("listing", lsting);
	        	String jsonText = JSONValue.toJSONString(obj);
	        	// writer.println(jsonText);
				System.out.println(jsonText);
			}
			// writer.close();
		} finally {
			scanner_products.close();
		}
		
	}
}
