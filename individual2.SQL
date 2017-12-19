SELECT AC.Ticker, AC.Year, AC.AbsoluteChange, RC.RelativeChange, TV.TotalVolume,
       AV.AverageVolume, AP.AverageClosing
FROM (SELECT O.Ticker, YEAR(O.Day) AS Year, (C.Close - O.Open) AS AbsoluteChange
        FROM (SELECT P.Ticker, P.Close, P.Day
                FROM Prices P,
             (SELECT MAX(P.Day) AS CloseDay
                        FROM Prices P
                        GROUP BY YEAR(P.Day)) N
                WHERE P.Day = CloseDay) C,
             (SELECT P.Ticker, P.Open, P.Day
                FROM Prices P,
                     (SELECT MIN(P.Day) AS OpenDay
                        FROM Prices P
                        GROUP BY YEAR(P.Day)) N
                WHERE P.Day = OpenDay) O
        WHERE O.Ticker = C.Ticker
              AND YEAR(O.Day) = YEAR(C.Day)
              AND O.Ticker = ?) AC,
     (SELECT O.Ticker, YEAR(O.Day) AS Year, (C.Close/O.Open) AS RelativeChange
        FROM (SELECT P.Ticker, P.Close, P.Day
                FROM Prices P,
             (SELECT MAX(P.Day) AS CloseDay
                        FROM Prices P
                        GROUP BY YEAR(P.Day)) N
                WHERE P.Day = CloseDay) C,
             (SELECT P.Ticker, P.Open, P.Day
                FROM Prices P,
                     (SELECT MIN(P.Day) AS OpenDay
                        FROM Prices P
                        GROUP BY YEAR(P.Day)) N
                WHERE P.Day = OpenDay) O
        WHERE O.Ticker = C.Ticker
              AND YEAR(O.Day) = YEAR(C.Day)
              AND O.Ticker = ?) RC,
     (SELECT P.Ticker, YEAR(P.Day) AS YEAR, SUM(P.Volume) AS TotalVolume
        FROM Prices P
        WHERE P.Ticker = ?
        GROUP BY Year(P.Day)) TV,
     (SELECT P.Ticker, YEAR(P.Day) AS YEAR, AVG(P.Volume) AS AverageVolume
        FROM Prices P
        WHERE P.Ticker = ?
        GROUP BY Year(P.Day)) AV,
     (SELECT P.Ticker, YEAR(P.Day) AS YEAR, AVG(P.Close) AS AverageClosing
        FROM Prices P
        WHERE P.Ticker = ?
        GROUP BY Year(P.Day)) AP
WHERE AC.Year = RC.Year
      AND RC.Year = TV.Year
      AND TV.Year = AV.Year
      AND AV.Year = AP.Year;
