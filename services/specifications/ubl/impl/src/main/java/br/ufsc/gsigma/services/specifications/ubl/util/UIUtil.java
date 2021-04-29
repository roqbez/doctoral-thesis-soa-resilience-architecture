package br.ufsc.gsigma.services.specifications.ubl.util;

import javax.swing.JOptionPane;

public class UIUtil {

	public static String createQuestionUI(String title, String message, Object[] labelObjects, String[] options) {

		int result = -1;

		String label = title + "\n\n";

		for (Object o : labelObjects)
			label += "Input: " + o.toString() + "\n";

		while (result == -1)
			result = JOptionPane.showOptionDialog(null, label + "\n" + message, title, JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, null);

		return options[result];

	}

}
