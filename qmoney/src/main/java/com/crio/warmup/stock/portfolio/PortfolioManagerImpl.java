
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedAndMetadata;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.management.RuntimeErrorException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {





  private RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }








  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    if(from.compareTo(to) >= 0){
      throw new RuntimeException();
    }
    String url = buildUri(symbol, from , to);
    TiingoCandle[] stocks = restTemplate.getForObject(url,TiingoCandle[].class);
    List<Candle> stocksList = Arrays.asList(stocks);
    return stocksList;
  }

  protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "b2aa9f126dcee21d707aadf68430de853d958c4b";
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?" + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol).replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
    return url;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    // TODO Auto-generated method stub
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    for(int i=0;i<portfolioTrades.size();i++){
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i),endDate); 
      annualizedReturns.add(annualizedReturn);
    }
    Comparator<AnnualizedReturn> sortReturn = Comparator.comparing(AnnualizedReturn :: getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns,sortReturn);
    return annualizedReturns;
  }


  private AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade, LocalDate endDate) {
    AnnualizedReturn annualizedReturn;
    String symbol = portfolioTrade.getSymbol();
    LocalDate startLocalDate = portfolioTrade.getPurchaseDate();
    try{
      List<Candle> stocksStarttoEndDate;
      stocksStarttoEndDate = getStockQuote(symbol, startLocalDate, endDate);
      Candle stockStartdate = stocksStarttoEndDate.get(0);
      Candle stockLatest = stocksStarttoEndDate.get(stocksStarttoEndDate.size()-1);
      Double buyPrice = stockStartdate.getOpen();
      Double sellPrice = stockLatest.getClose();
      double totalReturn = (sellPrice - buyPrice) / buyPrice;
      double numYears = ChronoUnit.DAYS.between(startLocalDate, endDate)/365.24;
      double annualizedReturns = Math.pow((1 + totalReturn) , (1 / numYears)) - 1;
      annualizedReturn =  new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
    }catch(JsonProcessingException e){
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    return annualizedReturn;
  }
}
