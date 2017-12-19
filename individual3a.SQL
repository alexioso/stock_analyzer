SELECT *
FROM (SELECT P.Ticker, AVG(P.Close) AS AverageClosing
        FROM Prices P
        WHERE P.Ticker = ?
              AND YEAR(P.Day) = 2016) AC,
    (SELECT MAX(P.High) AS HighestPrice
        FROM Prices P
        WHERE P.Ticker = ?
              AND YEAR(P.Day) = 2016) H,
    (SELECT MIN(P.Low) AS LowestPrice
        FROM Prices P
        WHERE P.Ticker = ?
              AND YEAR(P.Day) = 2016) L;
