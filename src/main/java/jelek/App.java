package jelek;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import jelek.StaticCheck.StaticCheckException;

public class App {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        for (var fileName : args) {
            try {
                System.out.println(fileName + ": ");
                System.out.println(
                    new String(Files.readAllBytes(Paths.get(fileName))));

                var p = new parser(new Scanner(new FileReader(fileName)));
                var program = (Ast.Program)p.parse().value;
                StaticCheck.run(program);
                // System.out.println(gson.toJson(program));
                Ir3Printer.print(Ir3Gen.gen(program));
            } catch (StaticCheckException e) {
                System.err.println("StaticCheckException: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
