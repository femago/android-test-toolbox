package co.edu.uniandes.miso.test_toolbox.report;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class TestSuite {

   private static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
         "<testsuite name=\"$name\" tests=\"$tests\" skipped=\"0\" failures=\"$failures\"" +
         " errors=\"0\" timestamp=\"$timestamp\" hostname=\"$hostname\" time=\"0.0\">\n" +
         "  <properties/>\n" +
         "  $testcases\n" +
         "  <system-out><![CDATA[]]></system-out>\n" +
         "  <system-err><![CDATA[]]></system-err>\n" +
         "</testsuite>";


   String name;
   int tests;
   int failures;
   String timestamp = "";

   String hostname = "";
   String time = "";

   Set<TestCase> cases = new HashSet<>();

   public Set<TestCase> getCases() {
      return cases;
   }

   public TestSuite(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public String getHostname() {
      return hostname;
   }

   public void setHostname(String hostname) {
      this.hostname = hostname;
   }

   public void increaseTest() {
      tests = tests + 1;
   }

   public void increaseFailures() {
      failures = failures + 1;
   }

   public void addTest(TestCase _case) {
      cases.add(_case);
   }

   public void setName(String name) {
      this.name = name;
   }

   public String toXml() {
      return TEMPLATE.replace("$name", name)
            .replace("$tests", "" + tests)
            .replace("$failures", "" + failures)
            .replace("$timestamp", timestamp)
            .replace("$hostname", hostname)
            .replace("$testcases", casesToXml());
   }

   private String casesToXml() {
      StringJoiner joiner = new StringJoiner("\n");
      for (TestCase _case : cases) {
         joiner.add(_case.toXml());
      }
      return joiner.toString();
   }
}
