package co.edu.uniandes.miso.test_toolbox.report;

public class TestCase {

   private static final String TEMPLATE = "<testcase name=\"$name\" classname=\"$classname\" time=\"0.0\">\n" +
         " $failure" +
         "  \n</testcase>";
   String name;
   String classname;
   String time;
   Failure failure;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getClassname() {
      return classname;
   }

   public void setClassname(String classname) {
      this.classname = classname;
   }

   public String getTime() {
      return time;
   }

   public void setTime(String time) {
      this.time = time;
   }

   public Failure getFailure() {
      return failure;
   }

   public void setFailure(Failure failure) {
      this.failure = failure;
   }

   public String toXml() {
      return TEMPLATE.replace("$name", name)
            .replace("$classname", classname)
            .replace("$failure", failure == null ? "" : failure.toXml());

   }
}
