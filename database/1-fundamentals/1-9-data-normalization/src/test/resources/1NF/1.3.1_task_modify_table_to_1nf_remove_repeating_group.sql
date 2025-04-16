-- ====================================================================================================================
-- TASK 1.3.1: Create a table that will remove repeating groups from the `customer_contact`
-- ====================================================================================================================
-- STORY: The bank manager wants to update Petro Cherpak's Personal phone in the system. After retrieving the
-- `customer_contact` the manager sees this:
-- 'Work phone: +13591666638, Personal phone: +11833188285, Personal email: petya.cherpachok@gmail.com,'
--
-- PROBLEM: We need to update the string with customer contacts and not lose any info from this cell.
-- Finding and modifying just one part of a concatenated string is error-prone and can lead to data corruption.
--
-- SOLUTION: Split up the column into 4 separate columns:
-- - personal_phone
-- - work_phone
-- - personal_email
-- - work_email

-- TODO:
-- Copy the `customers_normalized_order_with_pk`
-- New table name: `customers_remove_groups`
-- Replace the `customer_contact` with 4 new columns:
-- - personal_phone VARCHAR(20)
-- - work_phone VARCHAR(20)
-- - personal_email VARCHAR(30)
-- - work_email VARCHAR(30)


