package br.ufsc.gsigma.catalog.plugin.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "br.ufsc.gsigma.catalog.plugin.i18n.messages";

	public static String Business_items;
	public static String Organizations;
	public static String Resources;
	public static String Processes;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String getString(String paramString) {
		try {
			return ((String) Messages.class.getField(paramString).get(null));
		} catch (Exception e) {
		}
		return paramString;
	}

}
