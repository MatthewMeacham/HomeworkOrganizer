# --- !Ups
ALTER TABLE assignment ADD COLUMN test_evolution_col VARCHAR(255);

# --- !Downs
ALTER TABLE assignment DROP test_evolution_col;