SELECT N1.Ticker
FROM (SELECT O.Ticker, YEAR(O.Day) AS Yr, (C.Close - O.Open) AS AbsoluteIncrease
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
              AND C.Close > O.Open) N1
WHERE (SELECT COUNT(*)
       FROM (SELECT O.Ticker, YEAR(O.Day) AS Yr, (C.Close - O.Open) AS AbsoluteIncrease
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
              AND C.Close > O.Open) N2
        WHERE N1.Yr = N2.Yr
              AND N1.Yr = 2016
              AND N2.AbsoluteIncrease >= N1.AbsoluteIncrease) <= 5
        AND N1.Yr = 2016
ORDER BY N1.Yr, N1.AbsoluteIncrease DESC;
