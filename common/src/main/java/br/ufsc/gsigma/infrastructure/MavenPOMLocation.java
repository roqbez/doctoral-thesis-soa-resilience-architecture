package br.ufsc.gsigma.infrastructure;

import java.io.File;

import br.ufsc.gsigma.infrastructure.util.FileUtils;

public abstract class MavenPOMLocation {

	private static final String BASE_PATH;

	static {
		String current = new File(".").getAbsolutePath();
		current = FileUtils.adjustAbsolutePath(current);
		BASE_PATH = current.substring(0, current.indexOf("/architecture"));
	}

	public static final String POM_ARCHITECTURE = BASE_PATH + "/architecture/pom.xml";

	public static final String POM_SERVICES_UDDI = BASE_PATH + "/architecture/services/uddi/pom.xml";

	public static final String POM_COMMON_MESSAGING = BASE_PATH + "/architecture/common-messaging/pom.xml";

	public static final String POM_SERVICES_DISCOVERY = BASE_PATH + "/architecture/services/discovery/impl/pom.xml";

	public static final String POM_SERVICES_DEPLOYMENT = BASE_PATH + "/architecture/services/deployment/impl/pom.xml";

	public static final String POM_SERVICES_RESILIENCE = BASE_PATH + "/architecture/services/resilience/impl/pom.xml";

	public static final String POM_SERVICES_EXECUTION_BPEL = BASE_PATH + "/architecture/services/execution/bpel-impl/pom.xml";

	public static final String POM_SERVICES_BINDING = BASE_PATH + "/architecture/services/binding/impl/pom.xml";

	public static final String POM_SERVICES_BPEL_EXPORTER = BASE_PATH + "/architecture/services/bpelexport/impl/pom.xml";

	public static final String POM_SERVICES_UBL = BASE_PATH + "/architecture/services/specifications/ubl/impl/pom.xml";

	public static final String POM_SERVICES_CATALOG = BASE_PATH + "/architecture/catalog/services/impl/pom.xml";

	public static final String POM_SERVICES_CATALOG_PERSISTENCE = BASE_PATH + "/architecture/catalog/services/persistence/impl/pom.xml";

	public static final String POM_SERVICES_CATALOG_SPECIFICATION = BASE_PATH + "/architecture/catalog/services/specifications/impl/pom.xml";

	public static final String POM_SERVICES_CATALOG_BOOTSTRAP = BASE_PATH + "/architecture/catalog/services/bootstrap/pom.xml";

}
