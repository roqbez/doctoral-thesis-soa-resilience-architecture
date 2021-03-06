package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;

/**
 * This class was generated by Apache CXF 2.2.9
 * Sun Oct 31 15:56:16 BRST 2010
 * Generated source version: 2.2.9
 * 
 */
 
@WebService(targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", name = "UBL_CreateCatalogueProcess_ProviderParty_SendRejection")
@XmlSeeAlso({oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonextensioncomponents_2.ObjectFactory.class, br.ufsc.gsigma.processcontext.ObjectFactory.class, ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ObjectFactory.class, un.unece.uncefact.codelist.specification._54217._2001.ObjectFactory.class, un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.ObjectFactory.class, oasis.names.specification.ubl.schema.xsd.qualifieddatatypes_2.ObjectFactory.class})
public interface UBLCreateCatalogueProcessProviderPartySendRejection extends ServiceAccessContract {

    @WebResult(name = "output", targetNamespace = "")
    @RequestWrapper(localName = "sendRejection", targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", className = "services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.SendRejection")
    @WebMethod(action = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection/sendRejection")
    @ResponseWrapper(localName = "sendRejectionResponse", targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", className = "services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.SendRejectionResponse")
    public oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ApplicationResponseType sendRejection(
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.CatalogueRequestType input
    );

    @WebResult(name = "output", targetNamespace = "")
    @RequestWrapper(localName = "alive", targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", className = "services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.Alive")
    @WebMethod(action = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection/alive")
    @ResponseWrapper(localName = "aliveResponse", targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", className = "services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.AliveResponse")
    public java.lang.Boolean alive();

    @Oneway
    @RequestWrapper(localName = "sendRejectionAsync", targetNamespace = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection", className = "services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection.SendRejectionAsync")
    @WebMethod(action = "http://ubl.oasis.services/sourcing/catalogueprovision/createcatalogueprocess/providerParty/sendRejection/sendRejectionAsync")
    public void sendRejectionAsync(
        @WebParam(name = "processContext", targetNamespace = "")
        br.ufsc.gsigma.processcontext.ProcessContext processContext,
        @WebParam(name = "input", targetNamespace = "")
        oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.CatalogueRequestType input
    );
}
