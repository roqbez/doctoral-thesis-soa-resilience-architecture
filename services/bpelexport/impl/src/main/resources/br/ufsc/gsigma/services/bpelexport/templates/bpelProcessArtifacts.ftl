<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<definitions name="${processName}" targetNamespace="${processNamespace}"
	xmlns="http://schemas.xmlsoap.org/wsdl/" 
	xmlns:plnk="http://docs.oasis-open.org/wsbpel/2.0/plnktype"
	xmlns:bpws="http://docs.oasis-open.org/wsbpel/2.0/varprop"  
	xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
	xmlns:tns="${processNamespace}" 
	xmlns:vprop="http://docs.oasis-open.org/wsbpel/2.0/varprop" 
<#list serviceTypes as serviceType>
	xmlns:${serviceType.prefix}="${serviceType.namespace}"
</#list>
	xmlns:xsd="http://www.w3.org/2001/XMLSchema">

<#list serviceTypes as serviceType>
	<import location="${serviceType.wsdlLocation}" namespace="${serviceType.namespace}"/>
</#list>

<#list serviceTypes as serviceType>
	<plnk:partnerLinkType name="${serviceType.type}">
		<plnk:role name="Reference" portType="${serviceType.prefix}:${serviceType.type}" />
	</plnk:partnerLinkType>
</#list>

	<types>
		<xsd:schema attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="${processNamespace}">

			<xsd:element name="${processName}Request">
				<xsd:complexType>
					<xsd:sequence />
				</xsd:complexType>
			</xsd:element>

			<xsd:element name="${processName}Response">
				<xsd:complexType>
					<xsd:sequence>
						<xsd:element name="message" type="xsd:string"/>
						<xsd:element name="processId" type="xsd:string"/>
					</xsd:sequence>
				</xsd:complexType>
			</xsd:element>

		</xsd:schema>
	</types>

	<message name="${processName}RequestMessage">
		<part element="tns:${processName}Request" name="payload" />
	</message>
	<message name="${processName}ResponseMessage">
		<part element="tns:${processName}Response" name="payload" />
	</message>

	<portType name="${processName}">
		<operation name="execute">
			<input message="tns:${processName}RequestMessage" />
			<output message="tns:${processName}ResponseMessage" />
		</operation>
<#list services as service>
		<operation name="${service.callbackOperation}">
			<input message="${service.outputVariable.messageType}" name="${service.outputVariable.name}"/>
			<output message="${service.outputCallbackVariable.messageType}" name="${service.outputCallbackVariable.name}"/>
		</operation>
</#list>
	</portType>

	<plnk:partnerLinkType name="${processName}">
		<plnk:role name="Interface" portType="tns:${processName}" />
	</plnk:partnerLinkType>

	<binding name="${processName}Binding" type="tns:${processName}">

		<soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http" />
		<operation name="execute">
			<soap:operation soapAction="${processNamespace}/execute" />
			<input>
				<soap:body use="literal" />
			</input>
			<output>
				<soap:body use="literal" />
			</output>
		</operation>
<#list services as service>
		<operation name="${service.callbackOperation}">
			<soap:operation soapAction="${service.callbackSOAPAction}" />
			<input name="${service.outputVariable.name}">
				<soap:body use="literal" />
			</input>
			<output name="${service.outputCallbackVariable.name}">
				<soap:body use="literal" />
			</output>
		</operation>
</#list>
	</binding>
	

	<service name="${processName}Service">
		<port binding="tns:${processName}Binding" name="${processName}ServicePort">
			<soap:address location="http://executionservice.d-201603244.ufsc.br:7000/ode/processes/${processName}" />
		</port>
	</service>
	
	<bpws:property name="processId" type="xsd:string" />
	
	<bpws:propertyAlias propertyName="tns:processId" messageType="tns:${processName}ResponseMessage" part="payload">
		<bpws:query><![CDATA[/processId]]></bpws:query>
	</bpws:propertyAlias>
	
<#list services as service>
	<bpws:propertyAlias propertyName="tns:processId" messageType="${service.outputVariable.messageType}" part="payload">
		<bpws:query><![CDATA[/processContext/processId]]></bpws:query>
	</bpws:propertyAlias>
</#list>
</definitions>
