<#setting locale="en_US">
<?xml version="1.0" encoding="UTF-8"?>
<wbim:model xmlns:wbim="http://www.ibm.com/wbim/bomSchema1.0"
	schemaVersion="6.1.0">
	<wbim:catalogs>
		<#if resourceModel.resourceCatalogs?has_content><#list resourceModel.resourceCatalogs as resourceCatalog><wbim:resourceCatalogs>
			<wbim:catalog id="${resourceCatalog.id}" name="${resourceCatalog.name}" />
		</wbim:resourceCatalogs></#list></#if>
		<#if processModel.processCatalogs?has_content><#list processModel.processCatalogs as processCatalog><wbim:processCatalogs>
			<wbim:catalog id="${processCatalog.id}" name="${processCatalog.name}" />
		</wbim:processCatalogs></#list></#if>
		<#if dataModel.dataCatalogs?has_content><#list dataModel.dataCatalogs as dataCatalog><wbim:dataCatalogs>
			<wbim:catalog id="${dataCatalog.id}" name="${dataCatalog.name}" />
		</wbim:dataCatalogs></#list></#if>
		<wbim:businessServiceCatalogs />
		<wbim:businessServiceObjectCatalogs />
	</wbim:catalogs>
	<wbim:dataModel>
		<wbim:businessItems>
			<#list dataModel.dataCatalogs as dataCatalog><#list dataCatalog.businessItems as businessItem>
			<wbim:businessItem name="${businessItem.name}">
			       <wbim:complexDataType/>
			</wbim:businessItem>
			</#list></#list>
		</wbim:businessItems>
	</wbim:dataModel>
	<wbim:resourceModel>
		<wbim:roles>
			<#list resourceModel.resourceCatalogs as resourceCatalog><#list resourceCatalog.resources as resource>
			<#if resource.type = 'Role'>
			<wbim:role name="${resource.name}" />
			</#if>
			</#list></#list>
		</wbim:roles>
	</wbim:resourceModel>
	<wbim:processModel>
		<wbim:processes>
		<#list processModel.processCatalogs as processCatalog><#list processCatalog.processes as process>
			<wbim:process name="${process.name}">
				<wbim:inputs>
					<wbim:inputCriterion name="Input Criterion" />
				</wbim:inputs>
				<wbim:outputs>
					<wbim:outputCriterion name="Output Criterion">
						<wbim:relatedInputCriteria>
							<wbim:inputCriterion name="Input Criterion" />
						</wbim:relatedInputCriteria>
					</wbim:outputCriterion>
				</wbim:outputs>
				<wbim:flowContent>
					<#list process.startNodes as startNode>
					<wbim:startNode name="${startNode.name}" />
					</#list>
					<#list process.endNodes as endNode>
					<wbim:endNode name="${endNode.name}" />
					</#list>
					<#list process.stopNodes as stopNode>
					<wbim:stopNode associatedOutputCriterion="Output Criterion" name="${stopNode.name}" />
					</#list>
					<#list process.decisions as decision>
					<wbim:decision isInclusive="false" name="${decision.name}">
						<#list decision.inputBranches as inputBranch>
						<wbim:inputBranch name="${inputBranch.name}">
							<#list inputBranch.inputContactPoints as inputContactPoint>
							<#if inputContactPoint.associatedData?has_content>
							<wbim:input name="${inputContactPoint}" associatedData="${inputContactPoint.associatedData.name}" isOrdered="false" isUnique="false" maximum="1" minimum="1"/>
							<#else>
							<wbim:input name="${inputContactPoint}" />
							</#if>
							</#list>
						</wbim:inputBranch>
						</#list>
            			<#list decision.outputBranches as outputBranch>
						<wbim:outputBranch name="${outputBranch.name}">
							<#list outputBranch.outputContactPoints as outputContactPoint>
							<wbim:output name="${outputContactPoint}"/>
							</#list>
							<#if outputBranch.condition?has_content>	
							<wbim:condition name="${outputBranch.condition}"/>
							</#if>
							<#if outputBranch.probabilityPercentage?has_content>
							<wbim:operationalData>
								<wbim:probability>
									<wbim:literalValue>${outputBranch.probabilityPercentage}</wbim:literalValue>
								</wbim:probability>
							</wbim:operationalData>
							</#if>	
						</wbim:outputBranch>
            			</#list>
					</wbim:decision>
					</#list>
					<#list process.forks as fork>
					<wbim:fork name="${fork.name}">
						<#list fork.inputBranches as inputBranch>
						<wbim:inputBranch name="${inputBranch.name}">
							<#list inputBranch.inputContactPoints as inputContactPoint>
							<#if inputContactPoint.associatedData?has_content>
							<wbim:input name="${inputContactPoint}" associatedData="${inputContactPoint.associatedData.name}" isOrdered="false" isUnique="false" maximum="1" minimum="1"/>
							<#else>
							<wbim:input name="${inputContactPoint}" />
							</#if>
							</#list>
						</wbim:inputBranch>
						</#list>
            			<#list fork.outputBranches as outputBranch>
						<wbim:outputBranch name="${outputBranch.name}">
							<#list outputBranch.outputContactPoints as outputContactPoint>
							<wbim:output name="${outputContactPoint}"/>
							</#list>
						</wbim:outputBranch>
            			</#list>
					</wbim:fork>
					</#list>
					<#list process.merges as merge>
					<wbim:merge name="${merge.name}">
						<#list merge.inputBranches as inputBranch>
						<wbim:inputBranch name="${inputBranch.name}">
							<#list inputBranch.inputContactPoints as inputContactPoint>
							<#if inputContactPoint.associatedData?has_content>
							<wbim:input name="${inputContactPoint}" associatedData="${inputContactPoint.associatedData.name}" isOrdered="false" isUnique="false" maximum="1" minimum="1"/>
							<#else>
							<wbim:input name="${inputContactPoint}" />
							</#if>
							</#list>
						</wbim:inputBranch>
						</#list>
            			<#list merge.outputBranches as outputBranch>
						<wbim:outputBranch name="${outputBranch.name}">
							<#list outputBranch.outputContactPoints as outputContactPoint>
							<wbim:output name="${outputContactPoint}"/>
							</#list>
						</wbim:outputBranch>
            			</#list>
					</wbim:merge>
					</#list>
					<#list process.tasks as task>
					<wbim:${task.type} name="${task.name}">
						<wbim:description>${task.taskInformationExtension.xmlEscaped}</wbim:description>
						<wbim:inputs>
							<#list task.inputContactPoint as inputContactPoint>
							<#if inputContactPoint.associatedData?has_content>
							<wbim:input name="${inputContactPoint}" associatedData="${inputContactPoint.associatedData.name}" isOrdered="false" isUnique="false" maximum="1" minimum="1"/>
							<#else>
							<wbim:input name="${inputContactPoint}" />
							</#if>
							</#list>
							<#if !task.inputContactPoint?has_content>
							<wbim:inputCriterion name="Input Criterion" />
							<#else>
							<wbim:inputCriterion name="Input Criterion">
								<#list task.inputContactPoint as inputContactPoint>
								<wbim:input name="${inputContactPoint}" />
								</#list>
							</wbim:inputCriterion>
							</#if>
						</wbim:inputs>
						<wbim:outputs>
							<#list task.outputContactPoint as outputContactPoint>
							<#if outputContactPoint.associatedData?has_content>
							<wbim:output name="${outputContactPoint}" associatedData="${outputContactPoint.associatedData.name}" isOrdered="false" isUnique="false" maximum="1" minimum="1"/>
							<#else>
							<wbim:output name="${outputContactPoint}" />
							</#if>
							</#list>
							<wbim:outputCriterion name="Output Criterion">
								<#list task.outputContactPoint as outputContactPoint>
								<wbim:output name="${outputContactPoint}" />
								</#list>
								<wbim:relatedInputCriteria>
									<wbim:inputCriterion name="Input Criterion" />
								</wbim:relatedInputCriteria>
							</wbim:outputCriterion>
						</wbim:outputs>
						<#if task.resourceRequisites?has_content>
						<wbim:resources>
							<#list task.resourceRequisites as resourceRequisite>
							<#if resourceRequisite.type == 'Role'>
							<wbim:roleRequirement name="${resourceRequisite.name}" role="${resourceRequisite.role.name}" />
							</#if>
							</#list>
						</wbim:resources>
						</#if>
					</wbim:${task.type}>
					</#list>
					<#assign k = 0>
					<#list process.connections as connection>
					<wbim:connection name="Connection${k}">
						<wbim:source contactPoint="${connection.source.name}" node="${connection.source.component.name}" />
						<wbim:target contactPoint="${connection.destination.name}" node="${connection.destination.component.name}" />
					</wbim:connection>
					<#assign k = k + 1></#list>
				</wbim:flowContent>
			</wbim:process>
		</#list></#list>
		</wbim:processes>
	</wbim:processModel>
</wbim:model>