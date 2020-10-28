package view.util;

import java.net.URL;

import javax.swing.ImageIcon;

import view.NBodiesView;

public class NBResource {
	private NBResource() {
	}

	public static ImageIcon getStartImage() {
		return new ImageIcon(NBResource.get("start.png"));
	}

	public static ImageIcon getStopImage() {
		return new ImageIcon(NBResource.get("stop.png"));
	}

	public static ImageIcon getClearImage() {
		return new ImageIcon(NBResource.get("clear.png"));
	}

	private static URL get(final String resource) {
		final URL res = NBodiesView.class.getResource("/img/" + resource);
		return res != null ? res : NBodiesView.class.getResource("/img/notFound.png");
	}
}
