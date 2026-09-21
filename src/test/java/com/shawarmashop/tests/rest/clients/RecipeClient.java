package com.shawarmashop.tests.rest.clients;


import com.fasterxml.jackson.core.type.TypeReference;
import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.dto.*;
import com.shawarmashop.tests.dto.recipes.RecipeIngredientsResponse;
import com.shawarmashop.tests.dto.recipes.RecipeResponse;
import com.shawarmashop.tests.dto.recipes.ReviewPageResponse;
import com.shawarmashop.tests.dto.recipes.UpdateRecipeRequest;
import com.shawarmashop.tests.rest.ApiResult;
import com.shawarmashop.tests.rest.BaseClient;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class RecipeClient extends BaseClient {

    private static final String RECIPES_PATH = "/recipes";
    private static final String RECIPE_BY_ID = "/recipes/{id}";
    private static final String RECIPE_INGREDIENTS = "/recipes/{id}/ingredients";
    private static final String RECIPE_REVIEWS = "/recipes/{id}/reviews";

    private static final String RECIPE_IMAGE = "/recipes/{id}/image";

    public RecipeClient(ApiClient client) {
        super(client);
    }

    public ApiResult<Page<RecipeResponse>> list() {
        return list(Map.of());
    }

    @Step("GET /recipes (queryParams={0})")
    public ApiResult<Page<RecipeResponse>> list(Map<String, Object> queryParams) {
        Response response =  given(spec())
                .queryParams(queryParams)
                .get(RECIPES_PATH);

        return ApiResult.from(response, new TypeReference<>() {
        });
    }

    @Step("GET /recipes/{0}")
    public ApiResult<RecipeResponse> get(long id) {
        Response response = given(spec())
                .pathParam("id", id)
                .get(RECIPE_BY_ID);

        return ApiResult.from(response, RecipeResponse.class);
    }

    @Step("GET /recipes/{0}/ingredients")
    public ApiResult<RecipeIngredientsResponse> ingredients(long id) {
        Response response = given(spec())
                .pathParam("id", id)
                .get(RECIPE_INGREDIENTS);

        return ApiResult.from(response, RecipeIngredientsResponse.class);
    }

    @Step("GET /recipes/{0}/reviews (page={1})")
    public ApiResult<ReviewPageResponse> reviews(long id, int page) {
        Response response = given(spec())
                .pathParam("id", id)
                .queryParam("page", page)
                .get(RECIPE_REVIEWS);

        return ApiResult.from(response, ReviewPageResponse.class);
    }

    @Step("PATCH /recipes/{0}")
    public ApiResult<RecipeResponse> update(long id, UpdateRecipeRequest request) {
        Response response = given(spec())
                .pathParam("id", id)
                .body(request)
                .patch(RECIPE_BY_ID);

        return ApiResult.from(response, RecipeResponse.class);
    }

    @Step("POST /recipes/{0}/image (filename={1}, contentType={2})")
    public ApiResult<Void> uploadImage(long id, String filename, String contentType, byte[] bytes) {
        Response response = given(spec())
                .contentType(ContentType.MULTIPART)
                .pathParam("id", id)
                .multiPart("file", filename, bytes, contentType)
                .post(RECIPE_IMAGE);

        return ApiResult.from(response, Void.class);
    }
}

