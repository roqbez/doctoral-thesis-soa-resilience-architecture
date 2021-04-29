<#include "bpelProcessFlow.ftl">
<bpel:process name="${processName}" targetNamespace="${processNamespace}" suppressJoinFailure="yes"
	xmlns:tns="${processNamespace}" 
	xmlns:bpel="http://docs.oasis-open.org/wsbpel/2.0/process/executable" 
	xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
<#list serviceTypes as serviceType>
	xmlns:${serviceType.prefix}="${serviceType.namespace}"
</#list>
	queryLanguage="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"
	expressionLanguage="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0">

	<bpel:import location="${processName}Artifacts.wsdl" namespace="${processNamespace}" importType="http://schemas.xmlsoap.org/wsdl/" />

	<bpel:partnerLinks>
		<bpel:partnerLink name="${processName}" partnerLinkType="tns:${processName}" myRole="Interface" />
<#list serviceTypes as serviceType>
		<bpel:partnerLink name="${serviceType.partnerLinkName}" partnerLinkType="tns:${serviceType.type}" partnerRole="Reference" initializePartnerRole="no"/>
</#list>
	</bpel:partnerLinks>

	<bpel:variables>
		<bpel:variable name="process_input" messageType="tns:${processName}RequestMessage"/>
		<bpel:variable name="process_output" messageType="tns:${processName}ResponseMessage"/>
		<bpel:variable name="abort_process" type="xsd:boolean"/>
	</bpel:variables>
	

    <bpel:correlationSets>
        <bpel:correlationSet name="ProcessId" properties="tns:processId"/>
    </bpel:correlationSets>


	<bpel:sequence name="${processName}">
		
		<bpel:receive name="receiveInput" partnerLink="${processName}" portType="tns:${processName}" operation="execute" variable="process_input"
			createInstance="yes" />

		<bpel:assign validate="no" name="assign_abort_process">
			<bpel:copy>
					<bpel:from>false()</bpel:from>
					<bpel:to variable="abort_process" />
			</bpel:copy>
		</bpel:assign>

		<bpel:assign validate="no" name="assign_result">
			<bpel:copy>
				<bpel:from>
					<bpel:literal xml:space="preserve">
						<tns:${processName}Response>
							<message>Process initiated</message>
							<processId></processId>
						</tns:${processName}Response>
					</bpel:literal>
				</bpel:from>
				<bpel:to part="payload" variable="process_output"/>
			</bpel:copy>
			<bpel:copy>
				<bpel:from>$ode:pid</bpel:from>
				<bpel:to part="payload" variable="process_output">
					<bpel:query queryLanguage="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"><![CDATA[/processId]]></bpel:query>
				</bpel:to>
			</bpel:copy>
		</bpel:assign>
		
		<bpel:reply name="replyOutput" partnerLink="${processName}" portType="tns:${processName}" operation="execute" variable="process_output">
		    <bpel:correlations>
                <bpel:correlation initiate="yes" set="ProcessId"/>
            </bpel:correlations>			
		</bpel:reply>
		
		<bpel:wait name="waitAfterReply">
			<bpel:for>'PT0.1S'</bpel:for>
		</bpel:wait>

        <bpel:scope name="process_scope">
            <bpel:variables>
<#list services as service>
                <bpel:variable name="${service.inputVariable.name}" messageType="${service.inputVariable.messageType}"/>
                <bpel:variable name="${service.outputVariable.name}" messageType="${service.outputVariable.messageType}"/>
                <bpel:variable name="${service.outputCallbackVariable.name}" messageType="${service.outputCallbackVariable.messageType}"/>
</#list>
            </bpel:variables>
<#list flows as flow>
<@createFlow flow=flow/>
</#list>
		</bpel:scope>
	
	</bpel:sequence>
</bpel:process>

