package tlh;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ETClientApp extends AppCommandLine {
    public final static String lineSeperator = System.lineSeparator();
    public final static PrintStream out = System.out;
    final String version = "V 0.53";
    final String[] ETF = {"VTI", "SCHB", "VEA", "SCHF", "VWO", "IEMG", "VIG", "SCHD", "VTEB", "TFI"};
    final String dashes = "-----------------------------------------------------------";
    final String nL = "\r\n";
    protected Logger log = Logger.getLogger(String.valueOf(ETClientApp.class));
    String displayStr = "";
    String stoD = "";
    Double[] price1 = new Double[10];
    Double[] price2 = new Double[10];
    Double[] price = new Double[10];
    Double percentLoss = 0.1;
    int toD;
    boolean firstClose = true;
    boolean firstOpen = true;
    boolean wkEnd;
    boolean[] sellActive = {false, false, false, false, false, false, false, false, false, true};
    AnnotationConfigApplicationContext ctx = null;
    Map<String, String> acctListMap = new HashMap();
    boolean isLive = false;
    SessionData sessionData = null;
    TLH t = new TLH();

    //public ETClientApp(){}
    public ETClientApp(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ETClientApp obj = new ETClientApp(args);
        obj.startUp();
    }

    public void init(boolean flag) {
        try {
            log.debug("Current Thread :" + Thread.currentThread().getName() + ", Id : " + Thread.currentThread().getId());

            if (ctx != null)
                ctx.close();

            if (flag) {
                ctx = new AnnotationConfigApplicationContext();
                ctx.register(OOauthConfig.class);
                ctx.refresh();
                sessionData = null;
            } else {
                ctx = new AnnotationConfigApplicationContext();
                ctx.register(SandBoxConfig.class);
                ctx.refresh();
                sessionData = null;
            }

        } catch (Exception e) {
            out.println(" Sorry we are not able to initiate oauth request at this time..");
            log.error("Oauth Initialization failed ", e);
        }
        log.debug(" Context initialized for " + (isLive ? "Live Environment" : " Sandbox Environment"));
    }

    private void initOOauth() {
        log.debug("Initializing the oauth ");
        try {
            if (sessionData == null) {
                log.debug(" Re-Initalizing oauth ...");
                OauthController controller = ctx.getBean(OauthController.class);

                controller.fetchToken();

                controller.authorize();
                sessionData = controller.fetchAccessToken();
                log.debug(" Oauth initialized ");
            }

        } catch (Exception e) {
            log.debug(e);
            out.println(" Sorry we are not able to continue at this time, please restart the client..");
        }
    }

    public void startUp() {
        init(true);
        initOOauth();
        t.initTLH(version);
        //getAccountList();
        //getBalance("1");
        goForeverLoop();
        //comparePrices();

        /*getAccountList();
        String acctKey = "1";
        getBalance(acctKey);
        getPortfolio(acctKey);
        getOrders(acctKey);*/
    }

    public void goForeverLoop() {
        // Forever loop
        while (true) {
            // End of day / Start of new day (so initialize everything)
            wkEnd = t.getWkEnd(t.dm);
            firstClose = true;
            firstOpen = true;
            toD = t.getToD();
            t.rdOpen(false);
            t.rdClosed(false);
            System.out.println("toD " + t.getStod(toD) + " mktOpen " + t.getStod(t.mktOpen) + " mktClosed " + t.getStod(t.mktClosed) + "\n");

            // Market Closed loop
            while (toD < t.mktOpen || toD > t.mktClosed || wkEnd) {
                if (t.displayTime(firstClose, toD)) {
                    System.out.println("Mkt closed: ToD " + t.getStod(toD));
                    firstClose = false;
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                    System.out.println("main: sleep Exception " + ex);
                }
                toD = t.getToD();
            }

            t.rdOpen(true);
            t.rdClosed(true);

            // Market Open loop
            while (toD >= t.mktOpen && toD <= t.mktClosed && !wkEnd) {

                //Display to Console at Desired Intervals
                if (t.displayTime(firstOpen, toD)) {
                    displayStr = "";
                    for (int i = 0; i < 10; i++) {
                        if (t.openPos[i]) {
                            //price[i] = t.getPrice(i,ETF[i]);
                            price[i] = getQuotes(ETF[i]);
                            if (sellActive[i])
                                displayStr = displayStr + ETF[i] + "* " + price[i] + " ";
                            else
                                displayStr = displayStr + ETF[i] + " " + price[i] + " ";
                        }
                    }
                    System.out.println("Mkt open: ToD " + t.getStod(toD) + " " + displayStr);
                    firstOpen = false;
                }

                // Check prices for Sell/Buy criteria
                for (int i = 0; i < 10; i++) {
                    if (t.openPos[i]) {
                        //price[i] = t.getPrice(i,ETF[i]);
                        price[i] = getQuotes(ETF[i]);
                        if (t.sellCriteria(i, price[i], percentLoss) && !sellActive[i]) {
                            sellActive[i] = true;
                            int j;
                            t.playMusic();
                            if (i % 2 == 0) j = i + 1;
                            else j = i - 1;
                            Double k = 100 * (t.entryP[i] - price[i]) / t.entryP[i];
                            String m = String.valueOf(k).substring(0, 4);
                            String s1 = t.getDT() + nL + "Sell " + ETF[i] + " and Buy " + ETF[j] +
                                    " entryP " + t.entryP[i] + " price " + price[i] + " %loss " + m;
                            String s2 = nL + dashes + nL + s1 + nL + dashes + nL;
                            System.out.println(s2);
                            t.writeTxt(s2);
                            //sendSMS( s1 );
                            //placeCall();
                        }
                    }
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                    System.out.println("goForeverLoop: error " + ex);
                }
                toD = t.getToD();
            } // End Open mkt loop
        } // End While(true) loop
    }

    private void comparePrices() {
        while (true) {
            //Get etrade and api price quotess
            stoD = t.getStod(t.getToD());
            String s1 = "etrade " + stoD;
            String s2 = "api    " + stoD;
            for (int i = 0; i < 10; i++) {
                price1[i] = getQuotes(ETF[i]);  //etrade
                price2[i] = t.getPrice(i, ETF[i]);   //api
                s1 = s1 + " " + ETF[i] + " " + price1[i].toString();
                s2 = s2 + " " + ETF[i] + " " + price2[i].toString();
            }
            out.println(s1);
            out.println(s2);
            out.println();

            try {
                Thread.sleep(10000);
            } catch (Exception ex) {
                System.out.println("main: sleep Exception " + ex);
            }
        }
    }

    public void previewOrder() {

        OrderPreview client = ctx.getBean(OrderPreview.class);

        client.setSessionData(sessionData);

        Map<String, String> inputs = client.getOrderDataMap();

        String accountIdKey;

        out.print("Please select an account index for which you want to preview Order: ");
        String acctKeyIndx = KeyIn.getKeyInString();
        try {
            accountIdKey = getAccountIdKeyForIndex(acctKeyIndx);

        } catch (ApiException e) {
            return;
        }
        out.print(" Enter Symbol : ");

        String symbol = KeyIn.getKeyInString();
        inputs.put("SYMBOL", symbol);

        // Shows Order Action Menu
        printMenu(orderActionMenu);
        // Accept OrderAction choice
        int choice = KeyIn.getKeyInInteger();
        // Fills data to service
        client.fillOrderActionMenu(choice, inputs);

        out.print(" Enter Quantity : ");
        int qty = KeyIn.getKeyInInteger();
        inputs.put("QUANTITY", String.valueOf(qty));

        // Shows Order PriceType  Menu
        printMenu(orderPriceTypeMenu);

        // Accept PriceType choice
        choice = KeyIn.getKeyInInteger();

        // Fills data to service
        client.fillOrderPriceMenu(choice, inputs);

        if (choice == 2) {
            out.print(" Enter limit price : ");
            double limitPirce = KeyIn.getKeyInDouble();
            inputs.put("LIMIT_PRICE", String.valueOf(limitPirce));
        }

        // Shows Order Duration  Menu
        printMenu(durationTypeMenu);

        choice = KeyIn.getKeyInInteger();

        client.fillDurationMenu(choice, inputs);


        client.previewOrder(accountIdKey, inputs);

    }

    public void getAccountList() {
        initOOauth();
        AccountListClient client = ctx.getBean(AccountListClient.class);
        client.setSessionData(sessionData);
        String response;
        try {
            response = client.getAccountList();
            out.println(String.format("\n%20s %25s %25s %25s %25s\n", "Number", "AccountId", "AccountIdKey", "AccountDesc", "InstitutionType"));

            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
                JSONObject acctLstResponse = (JSONObject) jsonObject.get("AccountListResponse");
                JSONObject accounts = (JSONObject) acctLstResponse.get("Accounts");
                JSONArray acctsArr = (JSONArray) accounts.get("Account");
                Iterator itr = acctsArr.iterator();
                long count = 1;
                while (itr.hasNext()) {
                    JSONObject innerObj = (JSONObject) itr.next();
                    String acctIdKey = (String) innerObj.get("accountIdKey");
                    String acctStatus = (String) innerObj.get("accountStatus");
                    if (acctStatus != null && !acctStatus.equals("CLOSED")) {
                        acctListMap.put(String.valueOf(count), acctIdKey);
                        out.println(String.format("%20s %25s %25s %25s %25s\n", count, innerObj.get("accountId"), acctIdKey, innerObj.get("accountDesc"), innerObj.get("institutionType")));
                        count++;
                    }
                }

            } catch (Exception e) {
                log.error(" Exception on get accountlist : " + e.getMessage());
                log.error(e);
                e.printStackTrace();
            }

        } catch (ApiException e) {
            out.println();
            out.println(String.format("HttpStatus: %20s", e.getHttpStatus()));
            out.println(String.format("Message: %23s", e.getMessage()));
            out.println(String.format("Error Code: %20s", e.getCode()));
            out.println();
            out.println();
        } catch (UnsupportedEncodingException e) {
            log.error(" getAccountList : UnsupportedEncodingException ", e);
        } catch (GeneralSecurityException e) {
            log.error(" getAccountList : GeneralSecurityException ", e);
        } catch (Exception e) {
            log.error(" getAccountList : GenericException ", e);
        }
    }

    public void getBalance(String acctIndex) {
        BalanceClient client = ctx.getBean(BalanceClient.class);
        client.setSessionData(sessionData);
        String response = "";
        String accountIdKey;
        try {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e) {
            return;
        }

        try {
            log.debug(" Response String : " + response);
            response = client.getBalance(accountIdKey);
            log.debug(" Response String : " + response);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            log.debug(" JSONObject : " + jsonObject);
            //jsonObject.g
            JSONObject balanceResponse = (JSONObject) jsonObject.get("BalanceResponse");
            String accountId = (String) balanceResponse.get("accountId");
            out.println(String.format("%s\t\t\tBalances for %s %s%s", lineSeperator, accountId, lineSeperator, lineSeperator));

            JSONObject computedRec = (JSONObject) balanceResponse.get("Computed");
            JSONObject realTimeVal = (JSONObject) computedRec.get("RealTimeValues");
            if (computedRec.get("accountBalance") != null) {
                if (Double.class.isAssignableFrom(computedRec.get("accountBalance").getClass())) {
                    Double accountBalance = (Double) computedRec.get("accountBalance");
                    out.println("\t\tCash purchasing power:   $" + accountBalance);
                } else if (Long.class.isAssignableFrom(computedRec.get("accountBalance").getClass())) {
                    Long accountBalance = (Long) computedRec.get("accountBalance");
                    out.println("\t\tCash purchasing power:   $" + accountBalance);
                }
            }
            if (realTimeVal.get("totalAccountValue") != null) {
                if (Double.class.isAssignableFrom(realTimeVal.get("totalAccountValue").getClass())) {
                    Double totalAccountValue = (Double) realTimeVal.get("totalAccountValue");
                    out.println("\t\tLive Account Value:      $" + totalAccountValue);
                } else if (Long.class.isAssignableFrom(realTimeVal.get("totalAccountValue").getClass())) {
                    Long totalAccountValue = (Long) realTimeVal.get("totalAccountValue");
                    out.println("\t\tLive Account Value:      $" + totalAccountValue);
                }
            }
            if (computedRec.get("marginBuyingPower") != null) {
                if (Double.class.isAssignableFrom(computedRec.get("marginBuyingPower").getClass())) {
                    Double marginBuyingPower = (Double) computedRec.get("marginBuyingPower");
                    out.println("\t\tMargin Buying Power:     $" + marginBuyingPower);
                } else if (Long.class.isAssignableFrom(computedRec.get("marginBuyingPower").getClass())) {
                    Long totalAccountValue = (Long) computedRec.get("marginBuyingPower");
                    out.println("\t\tMargin Buying Power:     $" + totalAccountValue);
                }
            }
            if (computedRec.get("cashBuyingPower") != null) {
                if (Double.class.isAssignableFrom(computedRec.get("cashBuyingPower").getClass())) {
                    Double cashBuyingPower = (Double) computedRec.get("cashBuyingPower");
                    out.println("\t\tCash Buying Power:       $" + cashBuyingPower);
                } else if (Long.class.isAssignableFrom(computedRec.get("cashBuyingPower").getClass())) {
                    Long cashBuyingPower = (Long) computedRec.get("cashBuyingPower");
                    out.println("\t\tCash Buying Power:       $" + cashBuyingPower);
                }
            }
            System.out.println("\n");


        } catch (ApiException e) {
            out.println();
            out.println(String.format("HttpStatus: %20s", e.getHttpStatus()));
            out.println(String.format("Message: %23s", e.getMessage()));
            out.println(String.format("Error Code: %20s", e.getCode()));
            out.println();
            out.println();
        } catch (UnsupportedEncodingException e) {
            log.error(" getBalance : UnsupportedEncodingException ", e);
        } catch (GeneralSecurityException e) {
            log.error(" getBalance : GeneralSecurityException ", e);
        } catch (Exception e) {
            log.error(" getBalance : GenericException ", e);
        }
    }

    public void getPortfolio(String acctIndex) {
        PortfolioClient client = ctx.getBean(PortfolioClient.class);
        client.setSessionData(sessionData);
        String response = "";
        String accountIdKey;

        try {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e) {
            return;
        }
        try {
            response = client.getPortfolio(accountIdKey);
            log.debug(" Response String : " + response);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            //out.println(" JSONObject : " + jsonObject);
            out.println("*************************************");
            JSONObject portfolioResponse = (JSONObject) jsonObject.get("PortfolioResponse");
            //out.println(" portfolioResponse : " + portfolioResponse);
            out.println("*************************************");
            JSONArray accountPortfolioArr = (JSONArray) portfolioResponse.get("AccountPortfolio");
            Object[] responseData = new Object[8];
            Iterator acctItr = accountPortfolioArr.iterator();

            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);

            StringBuilder acctIdBuf = new StringBuilder();

            while (acctItr.hasNext()) {
                JSONObject acctObj = (JSONObject) acctItr.next();

                String accountId = (String) acctObj.get("accountId");

                acctIdBuf.append(lineSeperator).append("\t\t Portfolios for ").append(accountId).append(lineSeperator).append(lineSeperator);

                JSONArray positionArr = (JSONArray) acctObj.get("Position");

                Iterator itr = positionArr.iterator();


                while (itr.hasNext()) {
                    StringBuilder formatString = new StringBuilder();
                    JSONObject innerObj = (JSONObject) itr.next();

                    JSONObject prdObj = (JSONObject) innerObj.get("Product");
                    responseData[0] = prdObj.get("symbol");
                    formatString.append("%25s");

                    responseData[1] = innerObj.get("quantity");
                    formatString.append(" %25s");

                    responseData[2] = prdObj.get("securityType");
                    formatString.append(" %25s");

                    JSONObject quickObj = (JSONObject) innerObj.get("Quick");

                    if (Double.class.isAssignableFrom(quickObj.get("lastTrade").getClass())) {
                        responseData[3] = quickObj.get("lastTrade");
                        formatString.append(" %25f");
                    } else {
                        responseData[3] = quickObj.get("lastTrade");
                        formatString.append(" %25d");
                    }

                    if (Double.class.isAssignableFrom(innerObj.get("pricePaid").getClass())) {
                        responseData[4] = innerObj.get("pricePaid");
                        formatString.append(" %25f");
                    } else {
                        responseData[4] = innerObj.get("pricePaid");
                        formatString.append(" %25d");
                    }
                    if (Double.class.isAssignableFrom(innerObj.get("totalGain").getClass())) {
                        responseData[5] = innerObj.get("totalGain");
                        formatString.append(" %25f");
                    } else {
                        responseData[5] = innerObj.get("totalGain");
                        formatString.append(" %25d");
                    }
                    if (Double.class.isAssignableFrom(innerObj.get("marketValue").getClass())) {
                        responseData[6] = innerObj.get("marketValue");
                        formatString.append(" %25f").append(lineSeperator);
                    } else {
                        responseData[6] = innerObj.get("marketValue");
                        formatString.append(" %25d").append(lineSeperator);
                    }

                    fmt.format(formatString.toString(), responseData[0], responseData[1], responseData[2], responseData[3], responseData[4], responseData[5], responseData[6]);
                }

            }
            out.println(acctIdBuf.toString());

            String titleFormat = new StringBuilder("%25s %25s %25s %25s %25s %25s %25s").append(System.lineSeparator()).toString();

            out.printf(titleFormat, "Symbol", "Quantity", "Type", "LastPrice", "PricePaid", "TotalGain", "Value");
            out.println(sbuf.toString());
            out.println();
            out.println();

        } catch (ApiException e) {
            out.println();
            out.println(String.format("HttpStatus: %20s", e.getHttpStatus()));
            out.println(String.format("Message: %23s", e.getMessage()));
            out.println(String.format("Error Code: %20s", e.getCode()));
            out.println();
            out.println();
        } catch (Exception e) {
            log.error(" getPortfolio ", e);
        }
    }

    public Double getQuotes(String symbol) {
        QuotesClient client = ctx.getBean(QuotesClient.class);
        String response = "";
        Double price = 0.;
        try {
            response = client.getQuotes(symbol);
            log.debug(" Response String : " + response);
            //plh Code starts here ---------------------
            String s = "\"lastTrade\":";
            int k = response.indexOf(s) + s.length();
            int m = response.indexOf('\u002C', k);
            //out.println("Etrade response: " + response);
            //out.println("Etrade price: " + Double.parseDouble(response.substring(k, m)) );
            return Double.parseDouble(response.substring(k, m));
            //plh Code ends here ---------------------
        } catch (ApiException e) {
            out.println();
            out.println(String.format("HttpStatus: %20s", e.getHttpStatus()));
            out.println(String.format("Message: %23s", e.getMessage()));
            out.println(String.format("Error Code: %20s", e.getCode()));
            out.println();
            out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }

    public void getOrders(final String acctIndex) {
        OrderClient client = ctx.getBean(OrderClient.class);
        client.setSessionData(sessionData);
        String response = "";
        String accountIdKey = "";
        try {
            accountIdKey = getAccountIdKeyForIndex(acctIndex);
        } catch (ApiException e) {
            return;
        }
        try {
            response = client.getOrders(accountIdKey);
            log.debug(" Get Order response : " + response);
            if (response != null) {

                StringBuilder acctIdBuf = new StringBuilder();

                acctIdBuf.append(lineSeperator).append("\t\t Orders for selected account index : ").append(acctIndex).append(lineSeperator).append(lineSeperator);

                out.println(acctIdBuf.toString());

                client.parseResponse(response);

            } else {
                out.println("No records...");
            }

        } catch (ApiException e) {
            out.println();
            out.println(String.format("HttpStatus: %20s", e.getHttpStatus()));
            out.println(String.format("Message: %23s", e.getMessage()));
            out.println(String.format("Error Code: %20s", e.getCode()));
            out.println();
            out.println();
        } catch (UnsupportedEncodingException e) {
            log.error(" getBalance : UnsupportedEncodingException ", e);
        } catch (GeneralSecurityException e) {
            log.error(" getBalance : GeneralSecurityException ", e);
        } catch (Exception e) {
            log.error(" getBalance : GenericException ", e);
        }
    }

    private String getAccountIdKeyForIndex(String acctIndex) throws ApiException {
        String accountIdKey = "";
        try {
            accountIdKey = acctListMap.get(acctIndex);
            if (accountIdKey == null) {
                out.println(" Error : !!! Invalid account index selected !!! ");
            }
        } catch (Exception e) {
            log.error(" getAccountIdKeyForIndex ", e);
        }
        if (accountIdKey == null) {
            throw new ApiException(0, "0", "Invalid selection for accountId index");
        }
        return accountIdKey;
    }

    private String getPrice(PriceType priceType, JSONObject orderDetail) {

        String value = "";

        if (PriceType.LIMIT == priceType) {
            value = String.valueOf(orderDetail.get("limitPrice"));
        } else if (PriceType.MARKET == priceType) {
            value = "Mkt";
        } else {
            value = priceType.getValue();
        }

        return value;

    }

    private String getTerm(OrderTerm orderTerm) {

        String value = "";

        if (OrderTerm.GOOD_FOR_DAY == orderTerm) {
            value = "Day";
        } else {
            value = orderTerm.getValue();
        }

        return value;

    }

    private String convertLongToDate(Long ldate) {
        LocalDateTime dte = LocalDateTime.ofInstant(Instant.ofEpochMilli(ldate), ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

        return formatter.format(dte);
    }
}
