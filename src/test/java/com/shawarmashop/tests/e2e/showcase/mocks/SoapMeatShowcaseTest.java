package com.shawarmashop.tests.e2e.showcase.mocks;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.e2e.support.helpers.IngredientHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.extensions.KafkaTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import com.shawarmashop.tests.kafka.KafkaTestBus;
import com.shawarmashop.tests.kafka.StockEventsTopic;
import com.shawarmashop.tests.kafka.events.StockEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@KafkaTest
@DbTest
@DisplayName("Showcase: SOAP meat-supplier")
class SoapMeatShowcaseTest {

    private static final String ACCEPTED_CODE = "MEAT_BEEF";
    private static final String REJECTED_CODE = "REJECT_TEST";

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final IngredientHelper ingredientHelper = new IngredientHelper();

    @Test
    @DisplayName("ACCEPTED создаёт invoice и публикует RESTOCK_PLACED")
    void restockAccepted(IntegrationStubs stubs, KafkaTestBus kafka) {
        int ingredientId =
                ingredientHelper.createSoapIngredientId("ACCEPTED", ACCEPTED_CODE);

        StockEventsTopic stockEvents = kafka.stock();

        owner.ingredients()
                .restock(ingredientId, 100)
                .assertStatus(200);

        assertThat(
                stubs.meat()
                        .recordedEnvelopesForIngredient(ingredientId)
        )
                .as("SOAP-запрос для ingredientId=%s", ingredientId)
                .singleElement()
                .satisfies(request -> assertThat(request)
                        .contains(
                                "<orderRef>restock-"
                                        + ingredientId
                                        + "-"
                        )
                        .contains("<code>MEAT_BEEF</code>")
                        .contains("<quality>STANDARD</quality>"));

        StockEvent event = stockEvents.waitRestockPlaced(ingredientId);

        assertThat(event.getIngredientId())
                .isEqualTo(ingredientId);

        assertThat(event.getInvoiceNumber())
                .isNotBlank();

        assertThat(event.getStatus())
                .isIn("ACCEPTED", "WAITLIST");
    }

    @Test
    @DisplayName("REJECTED публикует RESTOCK_FAILED")
    void restockRejected(IntegrationStubs stubs, KafkaTestBus kafka) {
        int ingredientId =
                ingredientHelper.createSoapIngredientId("REJECTED", REJECTED_CODE);

        StockEventsTopic stockEvents = kafka.stock();

        owner.ingredients()
                .restock(ingredientId, 50)
                .assertStatus(200);

        assertThat(
                stubs.meat()
                        .recordedEnvelopesForIngredient(ingredientId)
        )
                .as("SOAP-запрос для ingredientId=%s", ingredientId)
                .singleElement()
                .satisfies(request -> assertThat(request)
                        .contains("<code>REJECT_TEST</code>")
                        .contains("<kg>1</kg>")
                        .contains("<quality>STANDARD</quality>"));

        StockEvent event =
                stockEvents.waitRestockFailed(ingredientId);

        assertThat(event.getIngredientId())
                .isEqualTo(ingredientId);

        assertThat(event.getReason())
                .contains("REJECTED");
    }

}
