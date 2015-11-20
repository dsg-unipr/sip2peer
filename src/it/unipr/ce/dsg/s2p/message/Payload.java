package it.unipr.ce.dsg.s2p.message;

/*
 * Copyright (C) 2010 University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Designer(s):
 * Marco Picone (picone@ce.unipr.it)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * Michele Amoretti (michele.amoretti@unipr.it)
 * 
 * Developer(s)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * 
 */


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Class <code>Payload</code> is a data structure in the form key/value
 * for peer message (implements an Hashtable).
 * <p> 
 * Class <code>Payload</code> permits different approach to
 * create a payload, through constructors and methods indeed
 * is possible add or remove parameters to payload.
 * 
 * 
 * @author Fabrizio Caramia
 *
 */
public class Payload {

	private Hashtable<String, Object> params;

	/**
	 * Create a new Payload.
	 * 
	 */

	public Payload(){

		this.params = new Hashtable<String, Object>();
	}

	/**
	 * Create a new Payload from a Map.
	 * 
	 * @param params a map object that can be used to initialize the contents of the Payload
	 */
	@SuppressWarnings("rawtypes")
	public Payload(Map params){

		this.params = new Hashtable<String, Object>();
		if(params!=null){
			@SuppressWarnings("unchecked")
			Iterator<Entry<String, Object>> i = params.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<String, Object> e = i.next();
				this.params.put(e.getKey(), e.getValue());
			}

		}

	}

	/**
	 * Create a new Payload from an Object using bean getters. 
	 * All <code>isName</code> and <code>getName</code> of the object are converted in a key/value pairs,
	 * where key is the name after the prefix "get" or "is" and value is the value returned from the getter method. 
	 * 
	 * @param obj an object that can be used to initialize the contents of the Payload
	 */
	public Payload(Object obj){

		this.params = new Hashtable<String, Object>();

		if(obj!=null)
			fillTableParams(obj);

	}

	/*
	 *  Reflects on all of the public methods of the object and save key and
	 *  value into payload parameters
	 */
	private void fillTableParams(Object objToParse){

		//runtime class of an object
		@SuppressWarnings("rawtypes")
		Class objParse  = objToParse.getClass();

		//to set includeSuperClass to false
		boolean includeSuperClass = objParse.getClassLoader() != null;

		//get all the member methods of the class or interface represented by this Class 
		Method[] methods = (includeSuperClass) ?
				objParse.getMethods() : objParse.getDeclaredMethods();

				//load only public method to hashtable 
				for (int i = 0; i < methods.length; i += 1) {
					try {
						Method method = methods[i];
						if (Modifier.isPublic(method.getModifiers())) {
							String name = method.getName();
							String key = "";
							if (name.startsWith("get")) {
								if (name.equals("getClass") || 
										name.equals("getDeclaringClass")) {
									key = "";
								} else {
									key = name.substring(3);
								}
							} else if (name.startsWith("is")) {
								key = name.substring(2);
							}
							if (key.length() > 0 &&
									Character.isUpperCase(key.charAt(0)) &&
									method.getParameterTypes().length == 0) {
								if (key.length() == 1) {
									key = key.toLowerCase();
								} else if (!Character.isUpperCase(key.charAt(1))) {
									key = key.substring(0, 1).toLowerCase() +
											key.substring(1);
								}

								Object result = method.invoke(objToParse, (Object[])null);

								this.params.put(key, result);
							}
						}
					} catch (Exception ignore) {
					}
				}




	}
	/**
	 * Check if the key exists into payload
	 * 
	 * @param key for search its presence
	 * @return true if the key exists
	 */
	public boolean containsKey(String key){

		return params.containsKey(key);

	}

	/**
	 * Check if the value exists into payload
	 * 
	 * @param value for search its presence
	 * @return true if the value exists
	 */
	public boolean containsValue(Object value){

		return params.containsValue(value);
	}

	/**
	 * Get all parameters of payload
	 * 
	 * @return an Hashtable with all parameters
	 */
	public Hashtable<String, Object> getParams(){

		return params;
	}

	/**
	 * Add a parameter into payload
	 * 
	 * @param key a key string
	 * @param value a value string
	 */
	public void addParam(String key, String value){

		this.params.put(key, new String(value));	
	}


	/**
	 * Add a parameter into payload
	 * 
	 * @param key a key string
	 * @param value an int value
	 */
	public void addParam(String key, int value){

		this.params.put(key, value);
	}

	/**
	 * Add a parameter into payload
	 * 
	 * @param key a key string
	 * @param value a boolean value
	 */
	public void addParam(String key, boolean value){

		this.params.put(key, value);
	}

	/**
	 * Remove a parameter associated with a key from payload
	 * 
	 * @param key a key string
	 */
	public void removeParam(String key){

		this.params.remove(key);
	}

	/**
	 * Return an iterator of all keys from payload
	 * 
	 * @return iterator an iterator of keys
	 */
	public Iterator<String> keys(){

		return this.params.keySet().iterator();
	}

	/**
	 * Return a string representation of this <code>Payload</code> object. 
	 * 
	 */
	@Override
	public String toString() {

		return params.toString();
	}

}
