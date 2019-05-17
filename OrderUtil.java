package tlh;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONObject;

public class OrderUtil {
	public static String getPrice(tlh.PriceType priceType, JSONObject orderDetail) {

		String value = "";

		if( tlh.PriceType.LIMIT == priceType ||
				tlh.PriceType.NET_CREDIT == priceType
				|| tlh.PriceType.NET_DEBIT == priceType) {
			value = String.valueOf(orderDetail.get("limitPrice"));
		}else if( tlh.PriceType.MARKET == priceType) {
			value = "Mkt";
		}else {
			value = priceType.getValue();
		}

		return value;

	}
	public static String getTerm(OrderTerm orderTerm) {

		String value = "";

		if( OrderTerm.GOOD_FOR_DAY == orderTerm ) {
			value = "Day";
		}else {
			value = orderTerm.getValue();
		}

		return value;

	}
	public static String convertLongToDate(Long ldate) {
		LocalDateTime dte = LocalDateTime.ofInstant(Instant.ofEpochMilli(ldate), ZoneId.of("America/New_York"));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");

		return formatter.format(dte);
	}
}
