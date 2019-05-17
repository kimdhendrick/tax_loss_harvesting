package tlh;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.commons.lang.StringUtils;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class TLH {
    private final String myPath = "D:\\Data\\Programs-Java\\";
    private final String TWILIO_SID = "AC3e9253f3b6dbc9893a5f4b902dc96ff6";
    private final String TWILIO_TOKEN = "b82c315fe5b633eb69627d9e1ed3b038";
    private final String nL = "\r\n";
    private String[] exitD = new String[10];
    private String[] entryD = new String[10];
    private String[] rdLine = new String[10];
    public boolean[] openPos = {false, false, false, false, false, false, false, false, false, true};
    private int lastToD = 0;
    int dm = 0;
    int mktOpen, mktClosed;
    private int intTime;
    private int[] shares = new int[10];
    Double[] entryP = new Double[10];

    private Scanner console = new Scanner(System.in);

    public void initTLH(String ver) {
        String s = nL + "Tax Loss Harvesting program " + ver + nL + "      " + getDT() + nL;
        System.out.println(s);
        writeTxt(s + nL);

        // Read Console input
        dm = rdConsole("Select diagnostic mode\n  0  Disable\n  1  Enable", 0, 1);
        intTime = rdConsole("Enter display interval (1-60)", 1, 60);
        if (intTime == 60) intTime = 100;
        if (dm == 1) {
            mktOpen = addMinutes(2);
            mktClosed = addMinutes(10);
        } else {
            mktOpen = 930;
            mktClosed = 1600;
        }
    }

    public boolean displayTime(boolean first, int tod) {
        boolean interval = (tod % 100) % intTime == 0;
        if (first || (interval && (tod != lastToD))) {
            lastToD = tod;
            return true;
        } else return false;
    }

    private String getWebResponse(int i, String ETF) {
        final String api_URL = "https://api.iextrading.com/1.0/tops/last?symbols=";
        String inputLine = "";

        try {
            URL url = new URL(api_URL + ETF);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            inputLine = in.readLine();
            in.close();
        } catch (Exception ex) {
            System.out.println("GetWebResponse: Exception " + ex);
            console.nextLine();
            System.exit(0);
        }
        return inputLine;
    }

    public Double getPrice(int i, String ETF) {
        double price = 1.;

        try {
            String response = getWebResponse(i, ETF);
            String s = "\"price\":";
            int k = response.indexOf(s) + s.length();
            int m = response.indexOf('\u002C', k);
            price = Double.parseDouble(response.substring(k, m));
            //System.out.println("api response: " + response);
            //System.out.println("api price: " + price);
        } catch (Exception ex) {
            System.out.println("GetPrice: Exception " + ex);
            console.nextLine();
            System.exit(0);
        }
        return price;
    }

    public void sendSMS(String msg) {
        // Find your Account Sid and Token at twilio.com/console
        // DANGER! This is insecure. See http://twil.io/secure
        //public static final String ACCOUNT_SID = "AC3e9253f3b6dbc9893a5f4b902dc96ff6";
        //public static final String AUTH_TOKEN =	"b82c315fe5b633eb69627d9e1ed3b038";
        //final String ACCOUNT_SID = "AC3e9253f3b6dbc9893a5f4b902dc96ff6";
        //final String AUTH_TOKEN =	"b82c315fe5b633eb69627d9e1ed3b038";

        Twilio.init(TWILIO_SID, TWILIO_TOKEN);
        Message message = Message
                .creator(new PhoneNumber("+17722859812"), // to
                        new PhoneNumber("+17722911871"), // from
                        msg)
                .create();
        System.out.println(message.getSid());
    }

    public void placeCall() {
        Twilio.init(TWILIO_SID, TWILIO_TOKEN);

        String from = "+17722911871";
        String to = "+17722859812";

        try {
            Call call = Call.creator(new PhoneNumber(to), new PhoneNumber(from),
                    new URI("http://demo.twilio.com/docs/voice.xml")).create();
            //System.out.println(call.getSid());		//Don't need?
        } catch (Exception ex) {
            System.out.println("placeCall: Exception " + ex);
            console.nextLine();
            System.exit(0);
        }
    }

    private int rdConsole(String str, int min, int max) {
        int response;
        while (true) {
            try {
                System.out.println(str);
                System.out.print("  Input: ");
                response = console.nextInt();
                if (response >= min && response <= max) break;
                System.out.println("\nIllegal value, try again");
            } catch (Exception ex) {
                System.out.println("rdConsole: Exception " + ex);
                console.nextLine();
                System.exit(0);
            }
        }
        System.out.println();
        return response;
    }

    public void writeTxt(String str) {
        String s = getDT();
        s = myPath + "TLHout" + "-" + s.substring(0, 2) + s.substring(3, 5) + s.substring(6, 10) + ".txt";

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(s, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.out.println("writeTxt(): Exception ");
        }
    }

    public boolean getWkEnd(int dm) {
        int dOfWk;
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        dOfWk = calendar.get(Calendar.DAY_OF_WEEK);
        //System.out.println( dOfWk );
        if (dm == 1) return false;   //!!!show wkEnd false if in Diag mode
        return dOfWk == 1 || dOfWk == 7;
    }

    public void rdOpen(boolean last) {
        int k = 0, j;
        String input;

        try {
            File file = new File(myPath + "Open.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((input = br.readLine()) != null) {
                rdLine[k] = input;
                //System.out.println("k " + k + " openLine " + openLine[k]);
                k++;
                if (k > 9) break;
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("rdOpen(): file not found exception ");
            console.nextLine();
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("rdOpen(): IO Exception ");
            console.nextLine();
            System.exit(0);
        }

        for (int i = 0; i < k; i++) {
            if (!rdLine[i].substring(1, 3).equals(", ") || rdLine[i].length() != 27) { //Check for correct format
                System.out.println("rdOpen(): failed format test ");
                System.out.println(rdLine[i] + " comma '" + rdLine[i].substring(1, 3) + "' length " + rdLine[i].length());
                console.nextLine();
            }
            j = Integer.valueOf(rdLine[i].substring(0, 1));

            try {
                openPos[j] = true;                       //Got open position
                //entryD[j] = rdLine[i].substring(3, 13);                   //Get entry date
                entryP[j] = Double.parseDouble(rdLine[i].substring(15, 21));     //Get entry price
                //shares[j] = Integer.parseInt(rdLine[i].substring(23, 27));  //Get shares
            } catch (Exception ex) {
                System.out.println("rdOpen(): Date exception1 " + ex);
                console.nextLine();
                System.exit(0);
            }
            if (last) {
                System.out.println(rdLine[i]);
                writeTxt(rdLine[i] + nL);
            }
        }
        if (last) {
            System.out.print("\n");
            writeTxt(nL);
        }
    }

    public void rdClosed(boolean last) {
        int k = 0, j, numClosed;
        String input;

        try {
            File file = new File(myPath + "Closed.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((input = br.readLine()) != null) {
                rdLine[k] = input;
                k++;
                if (k > 9) break;
            }

            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("RdClosed: file not found exception ");
            console.nextLine();
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("RdClosed: IO Exception ");
            console.nextLine();
            System.exit(0);
        }

        for (int i = 0; i < k; i++) {
            if (!rdLine[i].substring(1, 3).equals(", ") || rdLine[i].length() != 13) {//Check for correct format
                System.out.println("rdClosed(): failed format test ");
                System.out.println(rdLine[i]);
                console.nextLine();
            }
            j = Integer.valueOf(rdLine[i].substring(0, 1));
            openPos[j] = false;

            try {
                exitD[j] = rdLine[i].substring(3, 13);

            } catch (Exception ex) {
                System.out.println("RdClosed: Date exception1 " + ex);
                console.nextLine();
                System.exit(0);
            }
            if (last) {
                System.out.println(rdLine[i]);
                writeTxt(rdLine[i] + nL);
            }
        }
        if (last) {
            writeTxt(nL);
            System.out.print("\n");
        }
    }

    public boolean sellCriteria(int i, Double price, Double percLoss) {
        int j;
        if (i % 2 == 0) j = i + 1;
        else j = i - 1;
        //System.out.println("price " + price[i] + " %loss " + percentLoss + " entryP " +  entryP[i]);
        return price < ((100 - percLoss) / 100) * entryP[i] && enoughTime(exitD[j]);
    }

    private boolean enoughTime(String exitD) {
        long diff = 0;
        try {
            Date exitDate = new SimpleDateFormat("MM/dd/yyyy").parse(exitD);
            Date today = new Date();
            diff = (today.getTime() - exitDate.getTime()) / (24 * 3600 * 1000);
        } catch (Exception ex) {
            System.out.println("RdClosed: Date exception2 " + ex);
            console.nextLine();
            System.exit(0);
        }
        //System.out.println("diff " + diff);
        return diff >= 31;
    }

    public int getToD() {
        Date today = new Date();
        //today.getTime();
        DateFormat df = new SimpleDateFormat("HH:mm");
        String sTime = df.format(today.getTime());
        sTime = sTime.substring(0, 2) + sTime.substring(3, 5);
        return Integer.parseInt(sTime);
    }

    private int addMinutes(int add) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, add);
        String snewTime = df.format(cal.getTime());
        snewTime = snewTime.substring(0, 2) + snewTime.substring(3, 5);
        return Integer.valueOf(snewTime);
    }

    protected String getDT() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    public void playMusic() {
        try {
            String musicFile = myPath + "call_to_arms2.wav";
            InputStream in = new FileInputStream(musicFile);
            AudioStream audioStream = new AudioStream(in);
            AudioPlayer.player.start(audioStream);
        } catch (Exception e) {
            System.out.println("playMusic: Exception");
            System.out.println(e.getMessage());
            System.out.println(e.getCause());
            e.printStackTrace();
        }
    }

    public String getStod(int tod) {
        String s = Integer.toString(tod);
        return StringUtils.leftPad(s, 4, "0");
    }
}
