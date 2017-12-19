SELECT N1.Year, N1.Month, N1.Ticker AS T1, N1.PriceChange AS T1Change, 
       N2.Ticker AS T2, N2.PriceChange AS T2Change,
       (N1.PriceChange - N2.PriceChange) AS ChangeDifference
FROM (SELECT P1.Ticker, YEAR(P1.Day) AS Year, MONTH(P1.Day) AS Month, 
                (L.Close - F.Open) AS PriceChange
        FROM Prices P1, Prices P2,
             (SELECT P.Open, MIN(P.Day) AS FirstDay
                FROM Prices P
                WHERE P.Ticker = ?
                GROUP BY YEAR(P.Day), MONTH(P.Day)) F,
             (SELECT P.Close, MAX(P.Day) AS LastDay
                FROM Prices P
                WHERE P.Ticker = ?
                GROUP BY YEAR(P.Day), MONTH(P.Day)) L
        WHERE P1.Ticker = P2.Ticker
              AND P1.Ticker = ?
              AND P1.Day = F.FirstDay
              AND P2.Day = L.LastDay
              AND YEAR(F.FirstDay) = YEAR(L.LastDay)
              AND MONTH(F.FirstDay) = MONTH(L.LastDay)
        ORDER BY YEAR(F.FirstDay), MONTH(F.FirstDay)) N1,
     (SELECT P1.Ticker, YEAR(P1.Day) AS Year, MONTH(P1.Day) AS Month, 
                (L.Close - F.Open) AS PriceChange
        FROM Prices P1, Prices P2,
             (SELECT P.Open, MIN(P.Day) AS FirstDay
                FROM Prices P
                WHERE P.Ticker IN ('PCLN', 'CHTR', 'AMZN', 'ISRG', 'MLM')
                GROUP BY YEAR(P.Day), MONTH(P.Day)) F,
             (SELECT P.Close, MAX(P.Day) AS LastDay
                FROM Prices P
                WHERE P.Ticker IN ('PCLN', 'CHTR', 'AMZN', 'ISRG', 'MLM')
                GROUP BY YEAR(P.Day), MONTH(P.Day)) L
        WHERE P1.Ticker = P2.Ticker
              AND P1.Ticker IN ('PCLN', 'CHTR', 'AMZN', 'ISRG', 'MLM')
              AND P1.Day = F.FirstDay
              AND P2.Day = L.LastDay
              AND YEAR(F.FirstDay) = YEAR(L.LastDay)
              AND MONTH(F.FirstDay) = MONTH(L.LastDay)
        ORDER BY P1.Ticker, YEAR(F.FirstDay), MONTH(F.FirstDay)) N2
WHERE N1.Year = N2.Year
      AND N1.Year = 2016
      AND N2.Year = 2016
      AND N1.Month = N2.Month;