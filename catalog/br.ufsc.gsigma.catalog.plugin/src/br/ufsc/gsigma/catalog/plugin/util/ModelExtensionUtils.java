package br.ufsc.gsigma.catalog.plugin.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMProcessInformationExtension;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMTaskInformationExtension;

import com.ibm.btools.blm.ui.attributesview.model.GeneralModelAccessor;
import com.ibm.btools.bom.model.artifacts.Comment;
import com.ibm.btools.bom.model.processes.activities.Activity;
import com.ibm.btools.bom.model.processes.activities.StructuredActivityNode;
import com.ibm.btools.bom.model.resources.RequiredRole;
import com.ibm.btools.bom.model.resources.Role;

public abstract class ModelExtensionUtils {

	@SuppressWarnings("unchecked")
	public static Role getParticipantRole(StructuredActivityNode taskNode) {

		Role participantRole = null;

		if (!CollectionUtils.isEmpty(taskNode.getResourceRequirement())) {

			EList<Object> resourceRequirement = (EList<Object>) taskNode.getResourceRequirement();
			Object resource = !CollectionUtils.isEmpty(resourceRequirement) ? resourceRequirement.get(0) : null;

			if (resource instanceof RequiredRole) {
				participantRole = ((RequiredRole) resource).getRole();
			}
		}
		return participantRole;
	}

	public static BMTaskInformationExtension getTaskInformationExtension(StructuredActivityNode taskNode) {
		Comment commment = (Comment) taskNode.getOwnedComment().get(0);
		BMTaskInformationExtension info = getTaskInformationExtensionFromXML(commment.getBody());
		info.setTaskNode(taskNode);
		return info;
	}

	public static BMTaskInformationExtension getTaskInformationExtension(GeneralModelAccessor ivGeneralModelAccessor) {
		BMTaskInformationExtension info = getTaskInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());
		return info;
	}

	public static BMTaskInformationExtension getTaskInformationExtensionFromXML(String data) {
		if (!StringUtils.isBlank(data)) {
			ByteArrayInputStream in = null;
			try {
				in = new ByteArrayInputStream(data.getBytes("UTF-8"));
				Serializer serializer = new Persister();
				return serializer.read(BMTaskInformationExtension.class, in);
			} catch (Exception e) {
				e.printStackTrace();
				return new BMTaskInformationExtension();
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
					}
			}
		} else {
			return new BMTaskInformationExtension();
		}
	}

	public static String getXMLFromTaskInformationExtension(BMTaskInformationExtension taskInformationExtension) {
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};

		try {

			Serializer serializer = new Persister();

			serializer.write(taskInformationExtension, output);

			return output.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
	}

	public static BMProcessInformationExtension getProcessInformationExtension(StructuredActivityNode processNode) {

		String description = null;

		if (processNode.eContainer() instanceof Activity) {
			Activity activity = (Activity) processNode.eContainer();
			if (!activity.getOwnedComment().isEmpty()) {
				description = ((Comment) activity.getOwnedComment().get(0)).getBody();
			}
		}

		BMProcessInformationExtension info = getProcessInformationExtensionFromXML(description);
		return info;
	}

	public static BMProcessInformationExtension getProcessInformationExtension(GeneralModelAccessor ivGeneralModelAccessor) {
		BMProcessInformationExtension info = getProcessInformationExtensionFromXML(ivGeneralModelAccessor.getDescription());
		return info;
	}

	public static BMProcessInformationExtension getProcessInformationExtensionFromXML(String data) {
		ByteArrayInputStream in = null;
		try {
			in = new ByteArrayInputStream(data.getBytes("UTF-8"));
			Serializer serializer = new Persister();
			return serializer.read(BMProcessInformationExtension.class, in);
		} catch (Exception e) {
			return new BMProcessInformationExtension();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}

	public static String getXMLFromProcessInformationExtension(BMProcessInformationExtension processInformationExtension) {
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};

		try {

			Serializer serializer = new Persister();

			serializer.write(processInformationExtension, output);

			return output.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
	}

}
