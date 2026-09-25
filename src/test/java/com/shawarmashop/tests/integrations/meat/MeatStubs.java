package com.shawarmashop.tests.integrations.meat;


import com.shawarmashop.tests.integrations.wiremock.StubBuilder;
import com.shawarmashop.tests.integrations.wiremock.WireMockAdminClient;
import com.shawarmashop.tests.integrations.wiremock.WireMockStubBase;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static com.shawarmashop.tests.integrations.wiremock.StubBuilder.post;

public final class MeatStubs extends WireMockStubBase {

    private static final String SOAP_PATH = "/soap/meat";
    private static final String SOAP_NAMESPACE = "http://shawarmashop.com/partners/meat";
    private static final int OVERRIDE_PRIORITY = 1;

    private static final JAXBContext JAXB_CONTEXT = createJaxbContext();

    public MeatStubs(WireMockAdminClient admin) {
        super(admin);
    }

    public void respondAccepted(MeatOrderResponse response) {
        admin.addMapping(post(SOAP_PATH)
                .withPriority(OVERRIDE_PRIORITY)
                .willReturnXml(200, marshalToSoap(response.withStatus("ACCEPTED"))));
    }

    public void respondAcceptedForIngredient(long ingredientId, MeatOrderResponse response) {
        admin.addMapping(mappingForIngredient(ingredientId)
                .withPriority(OVERRIDE_PRIORITY)
                .willReturnXml(200, marshalToSoap(response.withStatus("ACCEPTED"))));
    }

    public void respondUnavailableForIngredient(long ingredientId) {
        admin.addMapping(mappingForIngredient(ingredientId)
                .withPriority(OVERRIDE_PRIORITY)
                .willReturnXml(503, "<error>meat supplier unavailable</error>"));
    }

    public List<String> recordedEnvelopesForIngredient(long ingredientId) {
        return findBodies(requestCriteriaForIngredient(ingredientId));
    }

    public String requireSingleEnvelopeForIngredient(long ingredientId) {
        List<String> envelopes = recordedEnvelopesForIngredient(ingredientId);
        if (envelopes.size() != 1) {
            throw new AssertionError("Ожидался ровно один SOAP-запрос restock для ingredientId="
                    + ingredientId + ", но найдено: " + envelopes.size());
        }
        return envelopes.getFirst();
    }

    public List<String> recordedEnvelopes() {
        return findBodies(Map.of("method", "POST", "url", SOAP_PATH));
    }

    private static StubBuilder mappingForIngredient(long ingredientId) {
        String orderRefPrefix = "restock-" + ingredientId + "-";
        String xpath = "//*[local-name()='orderRef' and starts-with(normalize-space(.), '" + orderRefPrefix + "')]";
        return post(SOAP_PATH).withXpath(xpath);
    }

    private static Map<String, Object> requestCriteriaForIngredient(long ingredientId) {
        return mappingForIngredient(ingredientId).buildRequest();
    }

    private List<String> findBodies(Map<String, Object> criteria) {
        return admin.findRequests(criteria).stream()
                .map(request -> request.get("body"))
                .map(body -> body == null ? "" : body.toString())
                .toList();
    }

    private static String marshalToSoap(MeatOrderResponse response) {
        try {
            Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter bodyWriter = new StringWriter();
            marshaller.marshal(response, bodyWriter);
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                      xmlns:m="%s">
                      <soapenv:Body>
                        %s
                      </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(SOAP_NAMESPACE, bodyWriter.toString().trim());
        } catch (JAXBException e) {
            throw new IllegalStateException("Не удалось сформировать SOAP-ответ", e);
        }
    }

    private static JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(MeatOrderResponse.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
