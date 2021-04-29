package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

@Indexed
public class InfinispanXmlDataDAO implements XmlDataDAO, Serializable {

	private static final long serialVersionUID = 1L;

	private String data;

	private transient Node node;

	private boolean isSimpleType;

	@Field(analyze = Analyze.NO)
	private String name;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long scopeId;

	@Field(analyze = Analyze.NO)
	private String processId;

	@Field(analyze = Analyze.NO, bridge = @FieldBridge(impl = LongBridge.class))
	private Long processInstanceId;

	private Map<String, String> props = new HashMap<String, String>();

	public InfinispanXmlDataDAO(InfinispanScopeDAO scope, String name) {
		this.scopeId = scope.getId();
		this.processInstanceId = ((InfinispanProcessInstanceDAO) scope.getProcessInstance()).getId();
		this.processId = ((InfinispanProcessDAO) scope.getProcessInstance().getProcess()).getId();
		this.name = name;
	}

	@Override
	public Node get() {
		if (node == null && data != null) {
			if (isSimpleType) {
				Document d = DOMUtils.newDocument();
				// we create a dummy wrapper element
				// prevents some apps from complaining
				// when text node is not actual child of document
				Element e = d.createElement("text-node-wrapper");
				Text tnode = d.createTextNode(data);
				d.appendChild(e);
				e.appendChild(tnode);
				this.node = tnode;
			} else {
				try {
					this.node = DOMUtils.stringToDOM(data);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return node;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProperty(String propertyName) {
		return props.get(propertyName);
	}

	@Override
	public ScopeDAO getScopeDAO() {
		return InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, scopeId);
	}

	@Override
	public boolean isNull() {
		return data == null;
	}

	@Override
	public void remove() {

	}

	@Override
	public void set(Node val) {
		this.node = val;
		if (val instanceof Element) {
			this.isSimpleType = false;
			this.data = DOMUtils.domToString(val);
		} else if (node != null) {
			this.isSimpleType = true;
			this.data = node.getNodeValue();
		}

		InfinispanScopeDAO scope = InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, scopeId);
		scope.updateVariable(this);
		InfinispanDatabase.updateCacheEntry(scope);
	}

	@Override
	public void setProperty(String pname, String pvalue) {
		this.props.put(pname, pvalue);

		InfinispanScopeDAO scope = InfinispanDatabase.getCacheEntry(InfinispanScopeDAO.class, scopeId);
		scope.updateVariable(this);
		InfinispanDatabase.updateCacheEntry(scope);
	}

}
