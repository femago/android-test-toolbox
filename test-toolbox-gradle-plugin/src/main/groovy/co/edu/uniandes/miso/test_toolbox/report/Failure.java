package co.edu.uniandes.miso.test_toolbox.report;

import java.util.Scanner;

public class Failure {
   private static final String TEMPLATE = " <failure message=\"$message\" type=\"$type\">\n" +
         " $stacktrace" +
         "</failure>";

   String message;
   String type;
   String stackTrace;

   public Failure(String stackTrace) {
      Scanner scanner = new Scanner(stackTrace);
      String firstLine = scanner.nextLine();
      this.message = firstLine;
      this.type = firstLine.split(":")[0];
      this.stackTrace = stackTrace;
   }

   public String toXml() {
      return TEMPLATE.replace("$message", message)
            .replace("$type", type)
            .replace("$stacktrace", stackTrace);
   }
}
