package br.ufsc.gsigma.binding.support;

import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;

public interface ClusterListener {

	public void viewChanged(ViewChangedEvent event);

}
