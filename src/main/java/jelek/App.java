package jelek;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import jelek.StaticCheck.StaticCheckException;

public class App {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        for (var fileName : args) {
            try {
                var p = new parser(new Scanner(new FileReader(fileName)));
                var program = p.parse().value;
                StaticCheck.run((Ast.Program)program);
                System.out.println(fileName + ": " + gson.toJson(program));
            } catch (StaticCheckException e) {
                System.err.println("StaticCheckException: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
