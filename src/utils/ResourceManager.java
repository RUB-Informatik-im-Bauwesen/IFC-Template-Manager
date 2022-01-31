package utils;

import java.io.InputStream;

import javax.swing.ImageIcon;

public class ResourceManager {
	public static ImageIcon getIcon(String relativePath) {
		return new ImageIcon(ClassLoader.getSystemResource("icons/"+relativePath));
	}
	
	public static InputStream getResource(String relativePath) {		
		return ClassLoader.getSystemResourceAsStream(relativePath);
	}
}
