SELECT N1.Year, N1.Month, N1.Ticker AS T1, N1.TotalVolume AS T1Volume,
       N2.Ticker AS T2, N2.TotalVolume AS T2Volume,
       (N1.TotalVolume - N2.TotalVolume) AS VolumeDiff
FROM (SELECT YEAR(P.Day) AS Year, MONTH(P.Day) AS Month, P.Ticker,
               SUM(P.Volume) AS TotalVolume
        FROM Prices P
        WHERE P.Ticker = ?
        GROUP BY P.Ticker, YEAR(P.Day), MONTH(P.Day)
        ORDER BY P.Ticker, YEAR(P.Day), MONTH(P.Day)) N1,
     (SELECT YEAR(P.Day) AS Year, MONTH(P.Day) AS Month, P.Ticker,
               SUM(P.Volume) AS TotalVolume
        FROM Prices P
        WHERE P.Ticker IN ('PCLN', 'CHTR', 'AMZN', 'ISRG', 'MLM')
        GROUP BY P.Ticker, YEAR(P.Day), MONTH(P.Day)
        ORDER BY P.Ticker, YEAR(P.Day), MONTH(P.Day)) N2
WHERE N1.Year = N2.Year
      AND N1.Year = 2016
      AND N1.Month = N2.Month;