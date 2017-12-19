SELECT E.Ticker, E.EarliestDate, L.LatestDate
FROM (SELECT P.Ticker, MIN(P.Day) AS EarliestDate
        FROM Prices P
        WHERE P.Ticker = ?) E,
     (SELECT P.Ticker, MAX(P.Day) AS LatestDate
        FROM Prices P
        WHERE P.Ticker = ?) L;