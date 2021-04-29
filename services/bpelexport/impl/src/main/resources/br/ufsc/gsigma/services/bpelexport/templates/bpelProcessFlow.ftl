<#macro createFlow flow>
            <bpel:flow name="${flow.name}">
<#if flow.initializeAssign?has_content>
                <bpel:assign validate="no" name="${flow.initializeAssign.name}">
 <#list flow.initializeAssign.copies as copy>                   
                    <bpel:copy>
                        <#if copy.from.literal?has_content>
                        <bpel:from><bpel:literal>${copy.from.literal}</bpel:literal></bpel:from>
                        <#else>
						<bpel:from part="${copy.from.part}" variable="${copy.from.variable}">
							<#if copy.from.query?has_content>
							<bpel:query queryLanguage="${copy.from.queryLanguage}">${copy.from.query}</bpel:query>
							</#if>
						</bpel:from>                        
                        </#if>
                        <bpel:to variable="${copy.to.variable}" part="${copy.to.part}">
							<#if copy.to.query?has_content>
							<bpel:query queryLanguage="${copy.to.queryLanguage}">${copy.to.query}</bpel:query>
							</#if>
                        </bpel:to>
                    </bpel:copy>
 </#list>
 	                <bpel:sources>
<#list flow.initializeAssign.sourceLinks as sourceLink>
	                    <bpel:source linkName="${sourceLink}">
	                    <#if sourceLink.transitionCondition?has_content>
	                    	<bpel:transitionCondition>${sourceLink.transitionCondition}</bpel:transitionCondition>
	                    </#if>
	                    </bpel:source>
</#list>
	                </bpel:sources>                   
            	</bpel:assign>
</#if>
            	
<#list flow.assigns as assign>
				<bpel:assign name="${assign.name}">
<#list assign.copies as copy>
                    <bpel:copy>
<#if copy.from?has_content>
                        <bpel:from part="${copy.from.part}" variable="${copy.from.variable}">
                            <bpel:query queryLanguage="${copy.from.queryLanguage}">
                                ${copy.from.query}
                            </bpel:query>
                        </bpel:from>
</#if>
<#if copy.to?has_content>
                        <bpel:to part="${copy.to.part}" variable="${copy.to.variable}">
                            <bpel:query queryLanguage="${copy.to.queryLanguage}">
                                ${copy.to.query}
                            </bpel:query>
                        </bpel:to>
                    </bpel:copy>
</#if>
</#list>
	                <bpel:targets>
<#list assign.targetLinks as targetLink>
	                    <bpel:target linkName="${targetLink}"/>
</#list>
	                </bpel:targets>
	                <bpel:sources>
<#list assign.sourceLinks as sourceLink>
	                    <bpel:source linkName="${sourceLink}">
	                    <#if sourceLink.transitionCondition?has_content>
	                    	<bpel:transitionCondition>${sourceLink.transitionCondition}</bpel:transitionCondition>
	                    </#if>
	                    </bpel:source>
</#list>
	                </bpel:sources>
				</bpel:assign>
</#list>
            	
<#list flow.invokes as invoke>
				<bpel:sequence>
	                <bpel:invoke name="${invoke.name}" suppressJoinFailure="yes" partnerLink="${invoke.service.partnerLinkName}" operation="${invoke.service.operationName}" portType="${invoke.service.portType}" inputVariable="${invoke.service.inputVariable.name}"/>
					<bpel:receive name="${invoke.receiveName}" partnerLink="${processName}" portType="tns:${processName}" operation="${invoke.callbackOperation}" variable="${invoke.service.outputVariable.name}">
							<bpel:correlations>
								<bpel:correlation initiate="no" set="ProcessId"/>
							</bpel:correlations>
					</bpel:receive>
					<bpel:reply name="${invoke.replyName}" partnerLink="${processName}" portType="tns:${processName}" operation="${invoke.callbackOperation}" variable="${invoke.service.outputCallbackVariable.name}"/>
	                <bpel:targets>
   	                    <#if invoke.joinCondition?has_content>
	                    	<bpel:joinCondition>${invoke.joinCondition}</bpel:joinCondition>
	                    </#if>
<#list invoke.targetLinks as targetLink>
	                    <bpel:target linkName="${targetLink}"/>
</#list>
	                </bpel:targets>
	                <bpel:sources>
<#list invoke.sourceLinks as sourceLink>
	                    <bpel:source linkName="${sourceLink}">
	                    <#if sourceLink.transitionCondition?has_content>
	                    	<bpel:transitionCondition>${sourceLink.transitionCondition}</bpel:transitionCondition>
	                    </#if>
	                    </bpel:source>
</#list>
	                </bpel:sources>
	                <#if invoke.endEvent>
		               	<bpel:assign validate="no" name="assign_abort_process_${invoke.name}">
							<bpel:copy>
								<bpel:from>true()</bpel:from>
								<bpel:to variable="abort_process" />
							</bpel:copy>
						</bpel:assign>
					</#if>
	                
				</bpel:sequence>
</#list>
<#list flow.whiles as while>
				<bpel:while name="${while.name}">
					<bpel:condition>${while.condition}</bpel:condition>
<#list while.flows as flow>
<@createFlow flow=flow/>				
</#list>
	                <bpel:targets>
<#list while.targetLinks as targetLink>
	                    <bpel:target linkName="${targetLink}"/>
</#list>
	                </bpel:targets>
	                <bpel:sources>
<#list while.sourceLinks as sourceLink>
	                    <bpel:source linkName="${sourceLink}">
	                    </bpel:source>
</#list>
	                </bpel:sources>				
				</bpel:while>
</#list>
                <bpel:links>
<#list flow.links as link>
                	<bpel:link name="${link}"/>
</#list>
            	</bpel:links>
			</bpel:flow>
</#macro>