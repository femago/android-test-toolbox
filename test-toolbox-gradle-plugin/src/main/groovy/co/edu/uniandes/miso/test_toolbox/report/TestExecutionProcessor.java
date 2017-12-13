package co.edu.uniandes.miso.test_toolbox.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TestExecutionProcessor {

   private String executionLog;
   private String executionSummary;
   private HashMap<String, TestSuite> suites = new HashMap<>();

   public TestExecutionProcessor(String executionLog) {
      this.executionLog = executionLog;
   }

   public Collection<TestSuite> parse() {
      String MARK_END_CASE = "---end-case---";
      String content = executionLog.replaceAll("INSTRUMENTATION_STATUS: ", "###")
            .replaceAll("INSTRUMENTATION_STATUS_CODE: 1.*", "")
            .replaceAll("INSTRUMENTATION_STATUS_CODE: .+", MARK_END_CASE)
            .replaceAll("(?m)^[ \t]*\r?\n", "");
      List<String> tests = new ArrayList<>(Arrays.asList(content.split(MARK_END_CASE)));
      executionSummary = tests.remove(tests.size() - 1);
      for (String test : tests) {
         String[] split = test.split("###");
         List<String> attrs = arrayToList(split);
         attrs.remove(0);
         processAttrs(attrs);
      }
      return suites.values();
   }

   private TestCase processAttrs(List<String> attrs) {
      TestCase result = new TestCase();
      for (String attr : attrs) {
         int i = attr.indexOf("=");
         String key = attr.substring(0, i);
         String value = attr.substring(i + 1);
         mapAttrToTestCase(result, key, value);
      }
      return result;
   }

   private void mapAttrToTestCase(TestCase result, String key, String value) {
      TestSuite testSuite;
      String newValue;
      switch (key) {
         case "test":
            newValue = value.replaceAll("\\n", "").replaceAll("\\r", "");
            result.setName(newValue);
            break;
         case "class":
            if (result.getClassname() == null) {
               newValue = value.replaceAll("\\n", "").replaceAll("\\r", "");
               result.setClassname(newValue);
               testSuite = locateSuite(newValue);
               testSuite.increaseTest();
               testSuite.addTest(result);
            }
            break;
         case "stack":
            result.setFailure(new Failure(value));
            testSuite = locateSuite(result.getClassname());
            testSuite.increaseFailures();
            break;
      }
   }

   private TestSuite locateSuite(String testSuiteName) {
      if (testSuiteName == null)
         throw new IllegalArgumentException("Invalid identifier for a TestSuite " + testSuiteName);
      TestSuite testSuite = Optional.ofNullable(suites.get(testSuiteName)).orElse(new TestSuite(testSuiteName));
      suites.put(testSuiteName, testSuite);
      return testSuite;
   }

   private List<String> arrayToList(String[] split) {
      return new ArrayList<>(Arrays.asList(split));
   }

}
