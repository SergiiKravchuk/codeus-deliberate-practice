------------------------------------------------------------------------
-- Populate Quotes table
------------------------------------------------------------------------
INSERT INTO quotes (person, quote) VALUES
('Albert Einstein', 'Imagination is more important than knowledge.'),
('Isaac Newton', 'If I have seen further it is by standing on the shoulders of Giants.'),
('Marie Curie', 'Nothing in life is to be feared, it is only to be understood.'),
('Nikola Tesla', 'The present is theirs, the future, for which I really worked, is mine.'),
('Ada Lovelace', 'That brain of mine is something more than merely mortal, as time will show.'),
('Alan Turing', 'Those who can imagine anything, can create the impossible.'),
('Carl Sagan', 'Somewhere, something incredible is waiting to be known.'),
('Rosalind Franklin', 'Science and everyday life cannot and should not be separated.'),
('Galileo Galilei', 'All truths are easy to understand once they are discovered.'),
('Richard Feynman', 'I would rather have questions that can’t be answered than answers that can’t be questioned.');
------------------------------------------------------------------------
-- Populate Posts table
------------------------------------------------------------------------
INSERT INTO posts (title, body)
VALUES
    ('Introduction to PostgreSQL', 'This is an introductory post about PostgreSQL. It covers basic concepts and features.'),
    ('Advanced PostgresSQL Techniques', 'In this post, we delve into advanced PostgreSQL techniques for efficient querying and data manipulation.'),
    ('PostgreSQL Optimization Strategies', 'This post explores various strategies for optimizing PostgreSQL database performance and efficiency.');
------------------------------------------------------------------------
-- Populate History Facts table
------------------------------------------------------------------------
INSERT INTO history_facts (title, details) VALUES
('Moon Landing', '{"year": 1969, "tags": ["space", "NASA", "milestone"], "country": "USA"}'),
('Fall of Berlin Wall', '{"year": 1989, "tags": ["Germany", "freedom", "Europe"], "country": "Germany"}'),
('Printing Press Invented', '{"year": 1440, "tags": ["invention", "communication"], "inventor": "Johannes Gutenberg"}'),
('French Revolution', '{"year": 1789, "tags": ["revolution", "France", "freedom"], "country": "France"}'),
('Discovery of Penicillin', '{"year": 1928, "tags": ["medicine", "science", "health"], "discoverer": "Alexander Fleming"}');
