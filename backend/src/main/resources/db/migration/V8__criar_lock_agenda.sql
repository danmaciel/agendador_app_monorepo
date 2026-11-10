-- A single lock row serializes the ordinary shared-agenda write flows.
-- The forced-booking admin override intentionally remains outside this lock.
CREATE TABLE agenda_write_lock (
    id INTEGER PRIMARY KEY
);
INSERT INTO agenda_write_lock (id) VALUES (1);
