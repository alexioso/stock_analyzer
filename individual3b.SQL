SELECT MONTH(P.Day) AS MONTH, AVG(P.Volume) AS AverageVolume
FROM Prices P
WHERE P.Ticker = ?
      AND YEAR(P.Day) = 2016
GROUP BY MONTH(P.Day);