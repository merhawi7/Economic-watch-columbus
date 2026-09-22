public class FredTest {

    public static void main(String[] args) {

        try {

            double unemployment =
                    FredApi.getLatestValue("COLU139UR");

            System.out.println("FRED connection successful!");
            System.out.println(
                    "Columbus unemployment: "
                    + unemployment
                    + "%"
            );

        } catch (Exception e) {

            System.out.println(
                    "FRED connection failed: "
                    + e.getMessage()
            );
        }
    }
}