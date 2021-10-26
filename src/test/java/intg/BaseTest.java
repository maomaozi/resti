package intg;


import com.mmaozi.example.MyApp;
import com.mmaozi.resti.RestiApplication;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseTest {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @BeforeAll
    public static void setup() {
        System.out.println("start container!");
        RestAssured.defaultParser = Parser.JSON;
        executorService.submit(() -> new RestiApplication().run(MyApp.class));
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("shutdown container!");
        executorService.shutdown();
    }
}
