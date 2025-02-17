--liquibase formatted sql
--changeset dima.peregorodiev:1

INSERT INTO accounts (id, balance, created_at)
VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 1000.00, NOW()),
    ('550e8400-e29b-41d4-a716-446655440000', 500.00, NOW());