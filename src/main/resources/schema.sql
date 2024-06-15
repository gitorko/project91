create table if not exists public.employee
(
    id    bigint,
    name  varchar,
    email varchar
);

create table if not exists public.company
(
    id    bigint,
    name  varchar
);
