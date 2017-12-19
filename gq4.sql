SELECT a1.Ticker
FROM AdjustedPrices a1, AdjustedPrices a2, AdjustedPrices aa1, AdjustedPrices aa2
WHERE a1.Day = (SELECT MIN(Day) FROM AdjustedPrices WHERE YEAR(Day) = 2016) AND
      a2.Day = a1.Day AND
      a2.Ticker <> a1.Ticker AND
      aa1.Day = (SELECT MAX(Day) FROM AdjustedPrices WHERE YEAR(Day) = 2016) AND
      aa2.Day = aa1.Day AND
      a1.Ticker = aa1.Ticker AND
      a2.Ticker = aa2.Ticker
    
GROUP BY a1.Ticker
ORDER BY AVG(aa1.Close/aa2.Close - a1.Open/a2.Open) DESC
LIMIT 10;
