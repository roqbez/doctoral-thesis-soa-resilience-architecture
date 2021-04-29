package br.ufsc.gsigma.architecture.experiments.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class BackupElasticSearch {

	public static void main(String[] args) throws IOException, InterruptedException {
		backupElasticSearch(new File("d:/teste-es.tar.gz"));
	}

	public static void backupElasticSearch(File destFile) throws IOException, InterruptedException {

		File parent = destFile.getParentFile();

		if (parent != null) {
			parent.mkdirs();
		}

		String aux = destFile.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(":", "");
		String fileName = "/mnt/" + aux.substring(0, 1).toLowerCase() + aux.substring(1);

		String dockerUtilsFolder = new File(new File(new File(".").getAbsolutePath()).getParentFile().getParentFile().getParentFile(), "docker-utils").getAbsolutePath().replaceAll("\\\\", "/").replaceAll(":", "");

		dockerUtilsFolder = "/mnt/" + dockerUtilsFolder.substring(0, 1).toLowerCase() + dockerUtilsFolder.substring(1);

		Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", "export DOCKER_OPTS='-H tcp://150.162.6.131:2376' ; cd " + dockerUtilsFolder + " ; ./docker-backup-volume.sh logging-elk_data " + fileName });

		while (p.isAlive()) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			}

		}

		p.waitFor();

	}

}
