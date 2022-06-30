import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.ListResourceResponse;
import models.SingleResourceResponse;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestApiTests {

    @Test
    void getListUsersTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/users");
        RestAssured.given()
                .log().all()
                .spec(spec).queryParam("page", "2")
                .when()
                .get()
                .then().contentType(ContentType.JSON)
                .statusCode(200)
                .body("data", hasSize(6));

    }

    @Test
    void getSingleUserTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/users/2");
        RestAssured.given()
                .log().all()
                .spec(spec)
                .when()
                .get()
                .then().contentType(ContentType.JSON)
                .statusCode(200)
                .body("data.id", equalTo(2), "support.text", hasToString("To keep ReqRes free, contributions towards server costs are appreciated!"));


    }

    @Test
    void getSingleUserNotFoundTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/users/23");
        given().log().all().spec(spec)
                .when()
                .get()
                .then()
                .statusCode(404);
    }

    @Test
    void getListResourceWithModelTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api/unknown");

        ListResourceResponse responseJson = given()
                .spec(spec)
                .when()
                .get().then().statusCode(200).extract().body().as(ListResourceResponse.class);

        int userCount = responseJson.getData().size();
        String name = responseJson.getData().get(0).getName();

        Assertions.assertEquals(6, userCount);
        Assertions.assertEquals("cerulean", name);

    }

    @Test
    void getSingleResourceWithModelTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api/unknown");

        SingleResourceResponse responseJson = given()
                .spec(spec)
                .when()
                .get("/2").then().statusCode(200).extract().body().as(SingleResourceResponse.class);

        int year = responseJson.getData().getYear();
        String color = responseJson.getData().getColor();

        Assertions.assertEquals(2001, year);
        Assertions.assertEquals("#C74375", color);

    }

    @Test
    void getSingleResourceNotFoundTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/unknown");
        given().log().all().spec(spec)
                .when()
                .get("/23")
                .then()
                .statusCode(404);
    }


    @Test
    void postCreateFakerTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");
        Faker faker = new Faker();

        String testName = faker.cat().name(), testJob = faker.cat().breed();

        JSONObject json = new JSONObject("{\n" + "\"name\":\"" + testName + "\",\n" + "\"job\":\"" + testJob + "\"\n" + "}");

        given().contentType(ContentType.JSON).spec(spec)
                .body(json.toString())
                .log().all()
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(201)
                .body("name", is(testName));

    }

    @Test
    void postCreateRegularTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        JSONObject json = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"leader\"\n" + "}");

        given().contentType(ContentType.JSON).spec(spec)
                .body(json.toString())
                .log().all()
                .when()
                .post("/users")
                .then()
                .log().all()
                .statusCode(201)
                .body("name", startsWith("morph"));

    }

    @Test
    void putUpdateTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");
        JSONObject json = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"leader\"\n" + "}");
        JSONObject jsonUpdate = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"zion resident\"\n" + "}");

        //create user
        given().contentType(ContentType.JSON).spec(spec)
                .body(json.toString())
                .when()
                .post("/users");

        //update user
        given().contentType(ContentType.JSON)
                .spec(spec)
                .body(jsonUpdate.toString())
                .log().all()
                .when()
                .put("/users/2")
                .then()
                .log().all()
                .statusCode(200)
                .body("job", startsWith("zion"));

    }

    @Test
    void patchCustomUpdateTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");
        JSONObject json = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"leader\"\n" + "}");
        JSONObject jsonUpdate = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"zion resident\"\n" + "}");

        //create user
        given().contentType(ContentType.JSON).spec(spec)
                .body(json.toString())
                .when()
                .post("/users");

        //patch user
        given().contentType(ContentType.JSON)
                .spec(spec)
                .body(jsonUpdate.toString())
                .log().all()
                .when()
                .patch("/users/2")
                .then()
                .log().all()
                .statusCode(200)
                .body("job", startsWith("zion"))
                .body("updatedAt", notNullValue());

    }


    @Test
    void deleteDeleteTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        JSONObject json = new JSONObject("{\n" + "\"name\":\"morpheus\",\n" + "\"job\":\"leader\"\n" + "}");


        //create user
        given().contentType(ContentType.JSON).spec(spec)
                .body(json.toString())
                .when()
                .post("/users");

        //delete user
        given().contentType(ContentType.JSON)
                .spec(spec)
                .log().all()
                .when()
                .delete("/users/2")
                .then()
                .log().all()
                .statusCode(204);

    }

    @Test
    void postRegularRegisterSuccessfulTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        String authOK = "{\n" + "\"email\":\"eve.holt@reqres.in\",\n" + "\"password\":\"pistol\"\n" + "}";

        //register user
        given().contentType(ContentType.JSON).spec(spec)
                .body(authOK)
                .log().all()
                .when()
                .post("/register")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", instanceOf(String.class))
                .body("token", notNullValue())
                .body("id", is(4));
    }

    @Test
    void postRegularRegisterUnsuccessfulTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        String authFailed = "{\n" + "\"email\":\"sydney@fife\"\n" + "}";

        //register user
        given().contentType(ContentType.JSON).spec(spec)
                .body(authFailed)
                .log().all()
                .when()
                .post("/register")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Missing password"));
    }

    @Test
    void postLoginSuccessfulTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        //test goes OK even with password "pistol". does api behaves well?

        String loginOK = "{\n" + "\"email\":\"eve.holt@reqres.in\",\n" + "\"password\":\"cityslicka\"\n" + "}";

        //log in
        given().contentType(ContentType.JSON).spec(spec)
                .body(loginOK)
                .log().all()
                .when()
                .post("/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", is("QpwL5tke4Pnpja7X4"));
    }

    @Test
    void postLoginUnsuccessfulTest() {
        RequestSpecification spec = given().baseUri("https://reqres.in/api");

        String loginFail = "{\n" + "\"email\":\"eve.holt@reqres.in\"" + "}";

        //log in
        given().contentType(ContentType.JSON).spec(spec)
                .body(loginFail)
                .log().all()
                .when()
                .post("/login")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Missing password"));
    }

    @Test
    void getDelayedResponseTest() {

        final Long DELAY_OF_MILLIS = Long.valueOf(4000);

        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/users");

             RestAssured.given()
                .log().all()
                .spec(spec).queryParam("delay", 3)
                .when()
                .get()
                .then().contentType(ContentType.JSON)
                .statusCode(200)
                .time(Matchers.lessThan(DELAY_OF_MILLIS+1500))
                .body("data", hasSize(6));
    }

    @Test
    void getDelayedResponseSpecTest() {

        final Long DELAY_OF_MILLIS = Long.valueOf(4000);

        RequestSpecification spec = given().baseUri("https://reqres.in").basePath("/api/users");

        //setting up waiting time for response
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder();
        resBuilder.expectResponseTime(Matchers.lessThan(DELAY_OF_MILLIS));
        RestAssured.responseSpecification = resBuilder.build();


        RestAssured.given()
                .log().all()
                .spec(spec).queryParam("delay", 3)
                .when()
                .get()
                .then().contentType(ContentType.JSON)
                .statusCode(200)
                .body("data", hasSize(6));
    }


}
