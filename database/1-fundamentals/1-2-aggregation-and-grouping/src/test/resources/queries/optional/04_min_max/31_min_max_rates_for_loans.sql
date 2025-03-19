-- 31: Find the minimum and maximum interest rates for loans with terms longer than 36 months
SELECT MIN(interest_rate) AS lowest_rate,
       MAX(interest_rate) AS highest_rate
FROM loans
WHERE term_months > 36;