package br.ufsc.gsigma.infrastructure.util.docker;

import com.github.dockerjava.core.command.LogContainerCmdImpl;

public class CustomLogContainerCmd extends LogContainerCmdImpl {

	public CustomLogContainerCmd(Exec exec, String containerId) {
		super(exec, containerId);
	}

	// public String sinceTimestamp;
	//
	// public String getSinceTimestamp() {
	// return sinceTimestamp;
	// }
	//
	// public LogContainerCmd withSinceTimestamp(String sinceTimestamp) {
	// this.sinceTimestamp = sinceTimestamp;
	// return this;
	// }

}