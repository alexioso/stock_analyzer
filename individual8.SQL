SELECT N1.Yr AS Year, N1.Ticker AS T1, N1.RelativeIncrease AS T1Increase, 
       N2.Ticker AS T2, N2.RelativeIncrease AS T2Increase,
       (N1.RelativeIncrease - N2.RelativeIncrease) AS Difference
FROM (SELECT O.Ticker, YEAR(O.Day) AS Yr, (C.Close/O.Open) AS RelativeIncrease
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
              AND O.Ticker = ?
              AND YEAR(O.Day) = YEAR(C.Day)
              AND YEAR(O.Day) = 2016) N1,
     (SELECT O.Ticker, YEAR(O.Day) AS Yr, (C.Close/O.Open) AS RelativeIncrease
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
              AND O.Ticker = ?
              AND YEAR(O.Day) = YEAR(C.Day)
              AND YEAR(O.Day) = 2016) N2;
