package chats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


public class PruebaPropiedades {
	public static void main(String[] args) {
		FileInputStream in;
		Properties props = null;
		try {
			in = new FileInputStream("Propiedades.txt");
			props = new Properties();
			props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("Archivo de propiedades no encontrado...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("No se pudieron cargar las propiedades...");
			e.printStackTrace();
		}
		Enumeration<?> prop_names = props.propertyNames();
		while (prop_names.hasMoreElements()) {
			String prop_name = (String) prop_names.nextElement();
			String property = props.getProperty(prop_name);
			System.out.println("propiedad '" + prop_name + "' es '" + property
					+ "'");
		}
	}
}
