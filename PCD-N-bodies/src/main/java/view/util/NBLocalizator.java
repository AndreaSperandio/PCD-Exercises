package view.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class NBLocalizator {
	private static final Double BIG_DOUBLE = 100000.0;
	private final ResourceBundle bundle;

	public NBLocalizator(final Class<?> clazz) {
		this.bundle = ResourceBundle.getBundle("prop." + clazz.getSimpleName());
	}

	public String getRes(final String key) {
		return this.getRes(key, (Object[]) null);
	}

	public String getRes(final String key, final Object... params) {
		try {
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					if (params[i] instanceof String) {
						params[i] = String.valueOf(params[i]);
					} else if (params[i] instanceof Boolean) {
						params[i] = Boolean.TRUE.equals(params[i]) ? 1 : 0;
					} else if (params[i] instanceof Double) {
						if ((Double) params[i] < NBLocalizator.BIG_DOUBLE) {
							params[i] = String.format("%.6f", params[i]);
						}
					}
					params[i] = params[i] != null ? params[i].toString() : "-";
				}
			}
			return MessageFormat.format(this.bundle.getString(key).replaceAll("'", "''"), params);
		} catch (@SuppressWarnings("unused") final MissingResourceException e) {
			return key + " non found in " + this.bundle.getBaseBundleName();
		}
	}
}
