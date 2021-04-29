package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.AuthCmd;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CommitCmd;
import com.github.dockerjava.api.command.ConnectToNetworkCmd;
import com.github.dockerjava.api.command.ContainerDiffCmd;
import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import com.github.dockerjava.api.command.CopyFileFromContainerCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateImageCmd;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.CreateVolumeCmd;
import com.github.dockerjava.api.command.DisconnectFromNetworkCmd;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectExecCmd;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.InspectNetworkCmd;
import com.github.dockerjava.api.command.InspectVolumeCmd;
import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.ListNetworksCmd;
import com.github.dockerjava.api.command.ListVolumesCmd;
import com.github.dockerjava.api.command.LoadImageCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PauseContainerCmd;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.command.RemoveNetworkCmd;
import com.github.dockerjava.api.command.RemoveVolumeCmd;
import com.github.dockerjava.api.command.RenameContainerCmd;
import com.github.dockerjava.api.command.RestartContainerCmd;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.api.command.SearchImagesCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.command.TopContainerCmd;
import com.github.dockerjava.api.command.UnpauseContainerCmd;
import com.github.dockerjava.api.command.UpdateContainerCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Identifier;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerClientDecorator implements DockerClient, DockerClientConfigAccessor {

	private DockerClient delegate;

	private DockerCmdExecFactory dockerCmdExecFactory;

	private DockerClientConfig dockerClientConfig;

	public DockerClientDecorator(DockerClient delegate, DockerCmdExecFactory dockerCmdExecFactory) {
		this.delegate = delegate;
		this.dockerCmdExecFactory = dockerCmdExecFactory;
	}

	public DockerClientDecorator(DockerClient delegate, DockerClientConfig dockerClientConfig) {
		this.delegate = delegate;
		this.dockerClientConfig = dockerClientConfig;
	}

	public LogContainerCmd logContainerCmd(String containerId) {
		if (dockerCmdExecFactory != null)
			return new CustomLogContainerCmd(dockerCmdExecFactory.createLogContainerCmdExec(), containerId);
		else
			return delegate.logContainerCmd(containerId);
	}

	@Override
	public DockerClientConfig getDockerClientConfig() {
		return dockerClientConfig;
	}

	public AuthConfig authConfig() throws DockerException {
		return delegate.authConfig();
	}

	public AuthCmd authCmd() {
		return delegate.authCmd();
	}

	public InfoCmd infoCmd() {
		return delegate.infoCmd();
	}

	public PingCmd pingCmd() {
		return delegate.pingCmd();
	}

	public VersionCmd versionCmd() {
		return delegate.versionCmd();
	}

	public PullImageCmd pullImageCmd(String repository) {
		return delegate.pullImageCmd(repository);
	}

	public PushImageCmd pushImageCmd(String name) {
		return delegate.pushImageCmd(name);
	}

	public PushImageCmd pushImageCmd(Identifier identifier) {
		return delegate.pushImageCmd(identifier);
	}

	public CreateImageCmd createImageCmd(String repository, InputStream imageStream) {
		return delegate.createImageCmd(repository, imageStream);
	}

	public LoadImageCmd loadImageCmd(InputStream imageStream) {
		return delegate.loadImageCmd(imageStream);
	}

	public SearchImagesCmd searchImagesCmd(String term) {
		return delegate.searchImagesCmd(term);
	}

	public RemoveImageCmd removeImageCmd(String imageId) {
		return delegate.removeImageCmd(imageId);
	}

	public ListImagesCmd listImagesCmd() {
		return delegate.listImagesCmd();
	}

	public InspectImageCmd inspectImageCmd(String imageId) {
		return delegate.inspectImageCmd(imageId);
	}

	public SaveImageCmd saveImageCmd(String name) {
		return delegate.saveImageCmd(name);
	}

	public ListContainersCmd listContainersCmd() {
		return delegate.listContainersCmd();
	}

	public CreateContainerCmd createContainerCmd(String image) {
		return delegate.createContainerCmd(image);
	}

	public StartContainerCmd startContainerCmd(String containerId) {
		return delegate.startContainerCmd(containerId);
	}

	public ExecCreateCmd execCreateCmd(String containerId) {
		return delegate.execCreateCmd(containerId);
	}

	public InspectContainerCmd inspectContainerCmd(String containerId) {
		return delegate.inspectContainerCmd(containerId);
	}

	public RemoveContainerCmd removeContainerCmd(String containerId) {
		return delegate.removeContainerCmd(containerId);
	}

	public WaitContainerCmd waitContainerCmd(String containerId) {
		return delegate.waitContainerCmd(containerId);
	}

	public AttachContainerCmd attachContainerCmd(String containerId) {
		return delegate.attachContainerCmd(containerId);
	}

	public ExecStartCmd execStartCmd(String execId) {
		return delegate.execStartCmd(execId);
	}

	public InspectExecCmd inspectExecCmd(String execId) {
		return delegate.inspectExecCmd(execId);
	}

	public CopyArchiveFromContainerCmd copyArchiveFromContainerCmd(String containerId, String resource) {
		return delegate.copyArchiveFromContainerCmd(containerId, resource);
	}

	@SuppressWarnings("deprecation")
	public CopyFileFromContainerCmd copyFileFromContainerCmd(String containerId, String resource) {
		return delegate.copyFileFromContainerCmd(containerId, resource);
	}

	public CopyArchiveToContainerCmd copyArchiveToContainerCmd(String containerId) {
		return delegate.copyArchiveToContainerCmd(containerId);
	}

	public ContainerDiffCmd containerDiffCmd(String containerId) {
		return delegate.containerDiffCmd(containerId);
	}

	public StopContainerCmd stopContainerCmd(String containerId) {
		return delegate.stopContainerCmd(containerId);
	}

	public KillContainerCmd killContainerCmd(String containerId) {
		return delegate.killContainerCmd(containerId);
	}

	public UpdateContainerCmd updateContainerCmd(String containerId) {
		return delegate.updateContainerCmd(containerId);
	}

	public RenameContainerCmd renameContainerCmd(String containerId) {
		return delegate.renameContainerCmd(containerId);
	}

	public RestartContainerCmd restartContainerCmd(String containerId) {
		return delegate.restartContainerCmd(containerId);
	}

	public CommitCmd commitCmd(String containerId) {
		return delegate.commitCmd(containerId);
	}

	public BuildImageCmd buildImageCmd() {
		return delegate.buildImageCmd();
	}

	public BuildImageCmd buildImageCmd(File dockerFileOrFolder) {
		return delegate.buildImageCmd(dockerFileOrFolder);
	}

	public BuildImageCmd buildImageCmd(InputStream tarInputStream) {
		return delegate.buildImageCmd(tarInputStream);
	}

	public TopContainerCmd topContainerCmd(String containerId) {
		return delegate.topContainerCmd(containerId);
	}

	public TagImageCmd tagImageCmd(String imageId, String repository, String tag) {
		return delegate.tagImageCmd(imageId, repository, tag);
	}

	public PauseContainerCmd pauseContainerCmd(String containerId) {
		return delegate.pauseContainerCmd(containerId);
	}

	public UnpauseContainerCmd unpauseContainerCmd(String containerId) {
		return delegate.unpauseContainerCmd(containerId);
	}

	public EventsCmd eventsCmd() {
		return delegate.eventsCmd();
	}

	public StatsCmd statsCmd(String containerId) {
		return delegate.statsCmd(containerId);
	}

	public CreateVolumeCmd createVolumeCmd() {
		return delegate.createVolumeCmd();
	}

	public InspectVolumeCmd inspectVolumeCmd(String name) {
		return delegate.inspectVolumeCmd(name);
	}

	public RemoveVolumeCmd removeVolumeCmd(String name) {
		return delegate.removeVolumeCmd(name);
	}

	public ListVolumesCmd listVolumesCmd() {
		return delegate.listVolumesCmd();
	}

	public ListNetworksCmd listNetworksCmd() {
		return delegate.listNetworksCmd();
	}

	public InspectNetworkCmd inspectNetworkCmd() {
		return delegate.inspectNetworkCmd();
	}

	public CreateNetworkCmd createNetworkCmd() {
		return delegate.createNetworkCmd();
	}

	public RemoveNetworkCmd removeNetworkCmd(String networkId) {
		return delegate.removeNetworkCmd(networkId);
	}

	public ConnectToNetworkCmd connectToNetworkCmd() {
		return delegate.connectToNetworkCmd();
	}

	public DisconnectFromNetworkCmd disconnectFromNetworkCmd() {
		return delegate.disconnectFromNetworkCmd();
	}

	public void close() throws IOException {
		delegate.close();
	}

}
