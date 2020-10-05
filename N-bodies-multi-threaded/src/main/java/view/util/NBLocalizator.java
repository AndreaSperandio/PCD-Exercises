package view.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class NBLocalizator {

	private final ResourceBundle bundle;

	public NBLocalizator(final Class<?> clazz) {
		this.bundle = ResourceBundle.getBundle("prop." + clazz.getSimpleName());
	}

	public String getRes(final String key) {
		try {
			return MessageFormat.format(this.bundle.getString(key).replaceAll("'", "''"), (Object[]) null);
		} catch (@SuppressWarnings("unused") final MissingResourceException e) {
			return key + " non found in " + this.bundle.getBaseBundleName();
		}
	}
}
