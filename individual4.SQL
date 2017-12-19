SELECT N1.Ticker, N1.Year, N1.Month, N1.PriceChange
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
        ORDER BY YEAR(F.FirstDay), MONTH(F.FirstDay)) N1
        LEFT JOIN
        (SELECT P1.Ticker, YEAR(P1.Day) AS Year, MONTH(P1.Day) AS Month, 
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
        ORDER BY YEAR(F.FirstDay), MONTH(F.FirstDay)) N2
        ON N1.Year = N2.Year
           AND N1.PriceChange < N2.PriceChange
WHERE N2.Month IS NULL;