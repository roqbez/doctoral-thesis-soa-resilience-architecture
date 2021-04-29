package br.ufsc.gsigma.catalog.services.specifications.output;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class ProcessTaskTaxonomy implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute
	private String name;

	@Attribute
	private String taxonomyClassification;

	@Element
	private ProcessTaxonomy parent;

	@Attribute
	private String participantName;

	public ProcessTaskTaxonomy() {

	}

	public ProcessTaskTaxonomy(ProcessTaxonomy parent, String name, String participantName) {
		setParent(parent);
		setParticipantName(participantName);
		setName(name);

	}

	private String formatString(String s) {
		s = capitalizeFully(s, null);
		s = s.substring(0, 1).toLowerCase() + s.substring(1, s.length());
		s = s.replaceAll(" ", "");
		return s;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {

		this.name = name;
		if (name != null)
			if (parent != null)
				this.taxonomyClassification = parent.getTaxonomyClassification() + "/" + formatString(participantName) + "/" + formatString(name);
			else
				this.taxonomyClassification = formatString(name);
		else
			taxonomyClassification = null;
	}

	@XmlTransient
	public ProcessTaxonomy getParent() {
		return parent;
	}

	public void setParent(ProcessTaxonomy parent) {
		this.parent = parent;
	}

	public String getTaxonomyClassification() {
		return taxonomyClassification;
	}

	public void setTaxonomyClassification(String taxonomyClassification) {
		this.taxonomyClassification = taxonomyClassification;
	}

	public String getParticipantName() {
		return participantName;
	}

	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}

	private static String capitalizeFully(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		str = str.toLowerCase();
		return capitalize(str, delimiters);
	}

	private static String capitalize(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);

			if (isDelimiter(ch, delimiters)) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(ch);
		}
		for (int i = 0, isize = delimiters.length; i < isize; i++) {
			if (ch == delimiters[i]) {
				return true;
			}
		}
		return false;
	}
}
