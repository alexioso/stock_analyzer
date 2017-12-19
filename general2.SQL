SELECT P.Ticker, S.Name, SUM(P.Volume) AS TotalTraded
FROM Prices P, AdjustedPrices A , Securities S
WHERE P.Ticker = A.Ticker 
      AND P.Ticker = S.Ticker
      AND A.Ticker = S.Ticker
      AND P.day = A.day 
      AND YEAR(P.day) = 2016
GROUP BY P.Ticker
ORDER BY TotalTraded DESC
LIMIT 10;
