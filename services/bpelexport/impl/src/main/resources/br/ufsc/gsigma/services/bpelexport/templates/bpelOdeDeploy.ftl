<?xml version="1.0" encoding="UTF-8"?>
<deploy xmlns="http://www.apache.org/ode/schemas/dd/2007/03" 
	xmlns:pns="${processNamespace}"
<#list serviceTypes as serviceType>
	xmlns:${serviceType.prefix}="${serviceType.namespace}"
</#list>>
	<process name="pns:${processName}">
		<in-memory>${processInMemory}</in-memory>
		<active>true</active>
		<provide partnerLink="${processName}">
			<service name="pns:${processName}Service" port="${processName}ServicePort" />
		</provide>
<#list serviceTypes as serviceType>
		<invoke partnerLink="${serviceType.type}_Partner">
			<service name="${serviceType.prefix}:${serviceType.type}Service" port="notused" />
		</invoke>
</#list>
<!--
		<cleanup on="always" >
	            <category>all</category>
	    </cleanup>
-->
	</process>

</deploy>