package view.util;

import java.net.URL;

import javax.swing.ImageIcon;

import view.TempMonitorView;

public class TMResource {
	private TMResource() {
	}

	public static ImageIcon getStartImage() {
		return new ImageIcon(TMResource.get("start.png"));
	}

	public static ImageIcon getStopImage() {
		return new ImageIcon(TMResource.get("stop.png"));
	}

	public static ImageIcon getResetImage() {
		return new ImageIcon(TMResource.get("reset.png"));
	}

	private static URL get(final String resource) {
		final URL res = TempMonitorView.class.getResource("/img/" + resource);
		return res != null ? res : TempMonitorView.class.getResource("/img/notFound.png");
	}
}
