
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedAndMetadata;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    
    File profile = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrade = objectMapper.readValue(profile, PortfolioTrade[].class);
    List<String> jsonToPersonList = new ArrayList<String>();
    for(PortfolioTrade pt : portfolioTrade){
      jsonToPersonList.add(pt.getSymbol());
    }
        return jsonToPersonList;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/anmolgarg433-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5542c4ed";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "29:1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
     ObjectMapper mapper = getObjectMapper();
     LocalDate endDate = LocalDate.parse(args[1]);
     File file = resolveFileFromResources(args[0]);
     PortfolioTrade []trades = mapper.readValue(file, PortfolioTrade[].class);
     List<TotalReturnsDto> list = new ArrayList<>();
     String token = getToken();
     for(PortfolioTrade trade: trades)
     {
        String url = prepareUrl(trade, endDate, token);
        TiingoCandle[] fetchedResult = restTemplate.getForObject(url, TiingoCandle[].class);

        String symbol = trade.getSymbol();

        list.add(new TotalReturnsDto(symbol, fetchedResult[0].getClose()));
 
     }

    Collections.sort(list, new Comparator<TotalReturnsDto>() {

      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2) {
        // TODO Auto-generated method stub
        if(o1.getClosingPrice()>o2.getClosingPrice())
        {
          return 1;
        }
        else if(o1.getClosingPrice()<o2.getClosingPrice())
        {
          return -1;
        }
        return 0;
      }  
    });
    
    List<String> symbolsList = new ArrayList<>();

    for(TotalReturnsDto t: list)
    {
      symbolsList.add(t.getSymbol());
    }
     return symbolsList;
  }

  static String getToken()
   {
      // return  "4a319d638dcad02c0dfac5573afbe6f6802bd8b5";
      return "b2aa9f126dcee21d707aadf68430de853d958c4b";
   }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    
    File profile = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrade = objectMapper.readValue(profile, PortfolioTrade[].class);
    List<PortfolioTrade> trades = new ArrayList<PortfolioTrade>();
    for(PortfolioTrade pt : portfolioTrade){
      trades.add(pt);
    }
        return trades;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = new String(); 
    url = "https://api.tiingo.com/tiingo/daily/"+ trade.getSymbol() +"/prices?startDate="+ trade.getPurchaseDate() +"&endDate="+ endDate +"&token=" + token;
    return url;
  }



  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {

     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
     ObjectMapper mapper = getObjectMapper();
      String url = prepareUrl(trade, endDate, token);
      TiingoCandle[] fetchedResult = restTemplate.getForObject(url, TiingoCandle[].class);
     
      return Arrays.asList(fetchedResult);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
      LocalDate endDate = LocalDate.parse(args[1]);
      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
      List<PortfolioTrade> trades = readTradesFromJson(args[0]);
      ObjectMapper mapper = getObjectMapper();
      for(int i=0;i<trades.size();i++){
        List<Candle> candles=fetchCandles(trades.get(i), endDate, getToken());
        annualizedReturns.add(calculateAnnualizedReturns(endDate, trades.get(i), getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles)));
      }
      Comparator<AnnualizedReturn> sortReturn = Comparator.comparing(AnnualizedReturn :: getAnnualizedReturn).reversed();
      Collections.sort(annualizedReturns,sortReturn);
      return annualizedReturns;
      
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      double totalReturn = (sellPrice - buyPrice) / buyPrice;
      double numYears = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.24;
      double annualizedReturns = Math.pow((1 + totalReturn) , (1 / numYears)) - 1;
      return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturn);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));

  }
}

