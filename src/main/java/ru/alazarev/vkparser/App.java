package ru.alazarev.vkparser;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.alazarev.vkparser.logic.Parser;

/**
 * Main app class.
 */
public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Parser.class);
        Parser parser = context.getBean("parser", Parser.class);
        parser.parse();
    }
}
