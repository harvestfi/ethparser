create user grafana
    valid until 'infinity';
create user backup;
create user hv_dev;

create table a_eth_address
(
    address varchar(255) not null
        primary key,
    idx     bigint
        constraint uk_lyyq39oejm6m49nbd9ijhupfg
            unique
);
alter table a_eth_address
    owner to hv_dev;
grant select on a_eth_address to grafana;
grant select on a_eth_address to backup;

create table a_eth_hash
(
    hash varchar(255) not null
        primary key,
    idx  bigint
        constraint uk_eccojqwcjpvg9g6clh6drv36f
            unique
);
alter table a_eth_hash
    owner to hv_dev;
grant select on a_eth_hash to grafana;
grant select on a_eth_hash to backup;


create table a_eth_block
(
    number           bigint  not null
        primary key,
    author           varchar(255),
    difficulty       varchar(255),
    extra_data       text,
    gas_limit        bigint  not null,
    gas_used         bigint  not null,
    nonce            varchar(255),
    size             bigint  not null,
    timestamp        bigint  not null,
    total_difficulty varchar(255),
    hash             bigint
        constraint uk_n7r41qxlm5x3rc5vwrpqedhxo
            unique
        constraint fkffdolbifluijxubt7ff53lf4o
            references a_eth_hash (idx),
    miner            bigint
        constraint fk2hme9la57iqv3mj3rgma5d514
            references a_eth_address (idx),
    parent_hash      bigint
        constraint fk5rymjl6kop16ll230q8frrxj9
            references a_eth_hash (idx),
    network          integer not null
);
alter table a_eth_block
    owner to hv_dev;
create index idx_eth_block_hash
    on a_eth_block (hash);
create index idx_eth_block_network
    on a_eth_block (network);
create index idx_eth_block_timestamp
    on a_eth_block (timestamp);
grant select on a_eth_block to grafana;
grant select on a_eth_block to backup;

create sequence a_eth_tx_id_seq;
create table a_eth_tx
(
    id                  bigserial
        primary key,
    creates             varchar(255),
    cumulative_gas_used bigint not null,
    gas                 bigint not null,
    gas_price           bigint not null,
    gas_used            bigint not null,
    input               text,
    nonce               varchar(255),
    public_key          varchar(255),
    raw                 varchar(255),
    revert_reason       varchar(255),
    root                varchar(255),
    status              varchar(255),
    transaction_index   bigint not null,
    value               varchar(255),
    block_number        bigint
        constraint fkt6utu5q2n3l2cprwxolf4ievh
            references a_eth_block
            on delete cascade,
    contract_address    bigint
        constraint fkcvpylqyqq9csklhntdsxc3uwf
            references a_eth_address (idx),
    from_address        bigint
        constraint fkbwe6xntwov7u176f4ycl6a6gr
            references a_eth_address (idx),
    hash                bigint
        constraint idx_eth_txs_hash
            unique
        constraint fkggohnme0pqf1rbo7ho4bnjj55
            references a_eth_hash (idx),
    to_address          bigint
        constraint fkf7i3tsjk7j6fwjnqq3hh4k8xt
            references a_eth_address (idx)
);
alter table a_eth_tx
    owner to hv_dev;
grant select on sequence a_eth_tx_id_seq to grafana;
grant select on sequence a_eth_tx_id_seq to backup;
create index idx_eth_txs_block_number
    on a_eth_tx (block_number);
create index idx_eth_txs_contract_address
    on a_eth_tx (contract_address);
create index idx_eth_txs_from_address
    on a_eth_tx (from_address);
create index idx_eth_txs_to_address
    on a_eth_tx (to_address);
grant select on a_eth_tx to grafana;
grant select on a_eth_tx to backup;
alter sequence a_eth_tx_id_seq owner to hv_dev;
alter sequence a_eth_tx_id_seq owned by a_eth_tx.id;
grant select on sequence a_eth_tx_id_seq to grafana;
grant select on sequence a_eth_tx_id_seq to backup;


create sequence a_eth_log_id_seq;
create table a_eth_log
(
    id                bigserial
        primary key,
    data              text,
    log_id            bigint  not null,
    removed           integer not null,
    topics            text,
    transaction_index bigint  not null,
    type              varchar(255),
    address           bigint
        constraint fk7f1b2cyhljaje7ndtadxtdthc
            references a_eth_address (idx),
    block_number      bigint
        constraint fkm1bby09fkeo27p1boj9wc1w0r
            references a_eth_block,
    first_topic       bigint
        constraint fk515nptp7wer9rq11pi317v0vl
            references a_eth_hash (idx),
    tx_id             bigint
        constraint fkdbi2jp2fga70304mo4cjyulc
            references a_eth_tx
            on delete cascade,
    constraint idx_eth_log_tx_id_log_id
        unique (tx_id, log_id)
);
alter table a_eth_log
    owner to hv_dev;
grant select on sequence a_eth_log_id_seq to grafana;
grant select on sequence a_eth_log_id_seq to backup;
create index idx_eth_log_address
    on a_eth_log (address);
create index idx_eth_log_block_number
    on a_eth_log (block_number);
create index idx_eth_log_first_topic
    on a_eth_log (first_topic);
grant select on a_eth_log to grafana;
grant select on a_eth_log to backup;
alter sequence a_eth_log_id_seq owner to hv_dev;
alter sequence a_eth_log_id_seq owned by a_eth_log.id;
grant select on sequence a_eth_log_id_seq to grafana;
grant select on sequence a_eth_log_id_seq to backup;


create sequence b_contract_events_id_seq;
create table b_contract_events
(
    id       bigserial
        primary key,
    block    bigint
        constraint fkta3rg7xqns9jj26rgpyflbxsq
            references a_eth_block
            on delete cascade,
    contract bigint
        constraint fkkyifgyelocpd5tnq5bhgra8wk
            references a_eth_address (idx),
    constraint b_contract_events_block_contract
        unique (block, contract)
);
alter table b_contract_events
    owner to hv_dev;
grant select on sequence b_contract_events_id_seq to grafana;
grant select on sequence b_contract_events_id_seq to backup;
grant select on b_contract_events to grafana;
grant select on b_contract_events to backup;
alter sequence b_contract_events_id_seq owner to hv_dev;
alter sequence b_contract_events_id_seq owned by b_contract_events.id;
grant select on sequence b_contract_events_id_seq to grafana;
grant select on sequence b_contract_events_id_seq to backup;

create table b_func_hashes
(
    method_id varchar(255) not null
        primary key,
    name      varchar(255)
);
alter table b_func_hashes
    owner to hv_dev;
create index b_func_hashes_name
    on b_func_hashes (name);
grant select on b_func_hashes to grafana;
grant select on b_func_hashes to backup;


create sequence b_contract_txs_id_seq;
create table b_contract_txs
(
    id        bigserial
        primary key,
    func_data jsonb,
    func_hash varchar(255)
        constraint fk15n0nwgc592lqjpue85o8fqqr
            references b_func_hashes,
    tx_id     bigint
        constraint b_contract_txs_tx
            unique
        constraint fk5gv98kcrok11boi2yenwfjkot
            references a_eth_tx
);
alter table b_contract_txs
    owner to hv_dev;

grant select on sequence b_contract_txs_id_seq to grafana;
grant select on sequence b_contract_txs_id_seq to backup;
create index b_contract_txs_func_hash
    on b_contract_txs (func_hash);
grant select on b_contract_txs to grafana;
grant select on b_contract_txs to backup;
alter sequence b_contract_txs_id_seq owner to hv_dev;
alter sequence b_contract_txs_id_seq owned by b_contract_txs.id;
grant select on sequence b_contract_txs_id_seq to grafana;
grant select on sequence b_contract_txs_id_seq to backup;




create table b_contract_event_to_tx
(
    event_id bigint not null
        constraint fkm241gqpq79nknje280fvu30w5
            references b_contract_events,
    tx_id    bigint not null
        constraint fkovb71c3arwj9r2ejp7mmdwblp
            references b_contract_txs,
    primary key (event_id, tx_id)
);
alter table b_contract_event_to_tx
    owner to hv_dev;
grant select on b_contract_event_to_tx to grafana;
grant select on b_contract_event_to_tx to backup;

create table b_log_hashes
(
    method_id   varchar(255) not null
        primary key,
    method_name varchar(255),
    topic_hash  bigint
        constraint fkp9f5yq694sbmt8r1mpb4ha4n0
            references a_eth_hash (idx)
);
alter table b_log_hashes
    owner to hv_dev;
create index b_log_hashes_method_name
    on b_log_hashes (method_name);
create index b_log_hashes_topic_hash
    on b_log_hashes (topic_hash);
grant select on b_log_hashes to grafana;
grant select on b_log_hashes to backup;

create sequence b_contract_logs_id_seq;
create table b_contract_logs
(
    id             bigserial
        primary key,
    log_idx        bigint not null,
    logs           jsonb,
    address        bigint
        constraint fkrkkdnf7mom8emboqbyqfwrdwf
            references a_eth_address (idx),
    contract_tx_id bigint
        constraint fk2wp23byi8261k79hban35a52h
            references b_contract_txs
            on delete cascade,
    topic          varchar(255)
        constraint fk6aoeyu9exo4d24rrvwqesi3h2
            references b_log_hashes,
    constraint b_contract_logs_contract_tx_id_log_idx
        unique (contract_tx_id, log_idx)
);
alter table b_contract_logs
    owner to hv_dev;
grant select on sequence b_contract_logs_id_seq to grafana;
grant select on sequence b_contract_logs_id_seq to backup;
create index b_contract_logs_address
    on b_contract_logs (address);
create index b_contract_logs_topic
    on b_contract_logs (topic);
grant select on b_contract_logs to grafana;
grant select on b_contract_logs to backup;
alter sequence b_contract_logs_id_seq owner to hv_dev;
alter sequence b_contract_logs_id_seq owned by b_contract_logs.id;
grant select on sequence b_contract_logs_id_seq to grafana;
grant select on sequence b_contract_logs_id_seq to backup;

create sequence b_contract_states_id_seq;
create table b_contract_states
(
    id                bigserial
        primary key,
    name              varchar(255),
    value             jsonb,
    contract_event_id bigint
        constraint fknqso7038g1ri26ie22hna1pkm
            references b_contract_events
            on delete cascade,
    constraint uk80s6o9bviindcpv15lpnnyu4x
        unique (contract_event_id, name)
);
alter table b_contract_states
    owner to hv_dev;
grant select on sequence b_contract_states_id_seq to grafana;
grant select on sequence b_contract_states_id_seq to backup;
grant select on b_contract_states to grafana;
grant select on b_contract_states to backup;
alter sequence b_contract_states_id_seq owner to hv_dev;
alter sequence b_contract_states_id_seq owned by b_contract_states.id;
grant select on sequence b_contract_states_id_seq to grafana;
grant select on sequence b_contract_states_id_seq to backup;

create table bancor_tx
(
    id                 varchar(255) not null
        primary key,
    amount             double precision,
    amount_bnt         double precision,
    amount_farm        double precision,
    block              bigint,
    block_date         bigint,
    coin               varchar(255),
    coin_address       varchar(255),
    farm_as_source     boolean,
    hash               varchar(255),
    last_gas           double precision,
    last_price         double precision,
    log_id             bigint,
    other_amount       double precision,
    other_coin         varchar(255),
    other_coin_address varchar(255),
    owner              varchar(255),
    price_bnt          double precision,
    price_farm         double precision,
    type               varchar(255)
);
alter table bancor_tx
    owner to hv_dev;

create table block_cache
(
    block      bigint not null
        primary key,
    block_date bigint not null,
    network    varchar(255)
);

alter table block_cache
    owner to hv_dev;
create index idx_block_cache
    on block_cache (block_date);
create index idx_block_cache_net
    on block_cache (network);
grant select on block_cache to grafana;
grant select on block_cache to backup;

create table deployer_tx
(
    id           varchar(255) not null
        primary key,
    block        bigint       not null,
    block_date   bigint       not null,
    confirmed    integer      not null,
    from_address varchar(255),
    gas_limit    numeric(19, 2),
    gas_price    numeric(19, 2),
    gas_used     numeric(19, 2),
    idx          bigint       not null,
    method_name  varchar(255),
    to_address   varchar(255),
    type         varchar(255),
    value        numeric(19, 2),
    name         varchar(255),
    network      varchar(255)
);
alter table deployer_tx
    owner to hv_dev;
create index idx_deployer_tx
    on deployer_tx (block);
create index idx_deployer_tx_network
    on deployer_tx (network);
grant select on deployer_tx to grafana;
grant select on deployer_tx to backup;

create sequence error_parse_id_seq
    as integer;
create table error_parse
(
    id          serial
        primary key,
    error_class varchar(255),
    json        text,
    network     varchar(255),
    status      integer
);
alter table error_parse
    owner to hv_dev;
grant select on sequence error_parse_id_seq to grafana;
grant select on sequence error_parse_id_seq to backup;
grant select on error_parse to grafana;
grant select on error_parse to backup;
alter sequence error_parse_id_seq owner to hv_dev;
alter sequence error_parse_id_seq owned by error_parse.id;
grant select on sequence error_parse_id_seq to grafana;
grant select on sequence error_parse_id_seq to backup;

create sequence eth_contract_source_codes_id_seq
    as integer;
create table eth_contract_source_codes
(
    id                    serial
        primary key,
    abi                   text,
    address               varchar(255) not null,
    compiler_version      varchar(255),
    constructor_arguments text,
    contract_name         varchar(255) not null,
    created_at            timestamp,
    evmversion            varchar(255),
    implementation        varchar(255),
    library               varchar(255),
    license_type          varchar(255),
    network               varchar(255) not null,
    optimization_used     boolean,
    proxy                 boolean,
    runs                  varchar(255),
    source_code           text,
    swarm_source          varchar(255),
    updated_at            timestamp,
    constraint eth_contract_source_codes_adr_net_pk
        unique (address, network)
);
alter table eth_contract_source_codes
    owner to hv_dev;
grant select on sequence eth_contract_source_codes_id_seq to grafana;
grant select on sequence eth_contract_source_codes_id_seq to backup;
create index idx_eth_contract_source_codes_address
    on eth_contract_source_codes (address);
create index idx_eth_contract_source_codes_name
    on eth_contract_source_codes (contract_name);
grant select on eth_contract_source_codes to grafana;
grant select on eth_contract_source_codes to backup;
alter sequence eth_contract_source_codes_id_seq owner to hv_dev;
alter sequence eth_contract_source_codes_id_seq owned by eth_contract_source_codes.id;
grant select on sequence eth_contract_source_codes_id_seq to grafana;
grant select on sequence eth_contract_source_codes_id_seq to backup;

create sequence eth_contracts_id_seq
    as integer;
create table eth_contracts
(
    id           serial
        primary key,
    address      varchar(255),
    created      bigint,
    name         varchar(255),
    network      varchar(255),
    type         integer not null,
    underlying   varchar(255),
    updated      bigint,
    created_date bigint,
    updated_date bigint,
    constraint eth_c_adr_net_pk
        unique (address, network)
);
alter table eth_contracts
    owner to hv_dev;
grant select on sequence eth_contracts_id_seq to grafana;
grant select on sequence eth_contracts_id_seq to backup;
create index idx_eth_contracts_address
    on eth_contracts (address);
create index idx_eth_contracts_name
    on eth_contracts (name);
grant select on eth_contracts to grafana;
grant select on eth_contracts to backup;
alter sequence eth_contracts_id_seq owner to hv_dev;
alter sequence eth_contracts_id_seq owned by eth_contracts.id;
grant select on sequence eth_contracts_id_seq to grafana;
grant select on sequence eth_contracts_id_seq to backup;

create sequence eth_pools_id_seq
    as integer;
create table eth_pools
(
    id            serial
        primary key,
    updated_block bigint,
    contract      integer
        constraint uk_5udgya1dmr3v5qtd91fk8wv48
            unique
        constraint fk10ejggrjn5iw4rumqvk6mu5yk
            references eth_contracts,
    controller    integer
        constraint fkpnih15629ecxme9rgofq22u1q
            references eth_contracts,
    governance    integer
        constraint fkik78udlo8lek4dusd872ftko9
            references eth_contracts,
    lp_token      integer
        constraint fk93eitbhw4da8p2a3caom2hck1
            references eth_contracts,
    owner         integer
        constraint fk4su50ff4lc76ng8keety1n2wq
            references eth_contracts,
    reward_token  integer
        constraint fk3ldve4891mi7w0hde9kknwalv
            references eth_contracts
);
alter table eth_pools
    owner to hv_dev;
grant select on sequence eth_pools_id_seq to grafana;
grant select on sequence eth_pools_id_seq to backup;
create index idx_eth_pools
    on eth_pools (contract);
grant select on eth_pools to grafana;
grant select on eth_pools to backup;
alter sequence eth_pools_id_seq owner to hv_dev;
alter sequence eth_pools_id_seq owned by eth_pools.id;
grant select on sequence eth_pools_id_seq to grafana;
grant select on sequence eth_pools_id_seq to backup;

create sequence eth_strategies_id_seq
    as integer;
create table eth_strategies
(
    id            serial
        primary key,
    updated_block bigint,
    contract      integer
        constraint uk_mm5ag0ivmy35w6u8mwavvk6kh
            unique
        constraint fkc0kwtg4tlo1el67d0ulxcdkv1
            references eth_contracts
);
alter table eth_strategies
    owner to hv_dev;
grant select on sequence eth_strategies_id_seq to grafana;
grant select on sequence eth_strategies_id_seq to backup;
create index idx_eth_strategies_contracts
    on eth_strategies (contract);
grant select on eth_strategies to grafana;
grant select on eth_strategies to backup;
alter sequence eth_strategies_id_seq owner to hv_dev;
alter sequence eth_strategies_id_seq owned by eth_strategies.id;
grant select on sequence eth_strategies_id_seq to grafana;
grant select on sequence eth_strategies_id_seq to backup;

create sequence eth_uni_pairs_id_seq
    as integer;
create table eth_uni_pairs
(
    id            serial
        primary key,
    decimals      bigint,
    type          integer not null,
    updated_block bigint,
    contract      integer
        constraint uk_chgws7al9e6gy2i81674gsmx6
            unique
        constraint fkth7ib1cocfiqfp1d0wx5g53ol
            references eth_contracts,
    token0_id     integer
        constraint fkt60ljxkxdle0yvh2mnssy5kkr
            references eth_contracts,
    token1_id     integer
        constraint fkcacgxuk04t17glca80y7hi38o
            references eth_contracts
);
alter table eth_uni_pairs
    owner to hv_dev;
grant select on sequence eth_uni_pairs_id_seq to grafana;
grant select on sequence eth_uni_pairs_id_seq to backup;
create index idx_eth_uni_pairs
    on eth_uni_pairs (contract);
grant select on eth_uni_pairs to grafana;
grant select on eth_uni_pairs to backup;
alter sequence eth_uni_pairs_id_seq owner to hv_dev;
alter sequence eth_uni_pairs_id_seq owned by eth_uni_pairs.id;
grant select on sequence eth_uni_pairs_id_seq to grafana;
grant select on sequence eth_uni_pairs_id_seq to backup;

create sequence eth_tokens_id_seq
    as integer;
create table eth_tokens
(
    id            serial
        primary key,
    decimals      bigint,
    name          varchar(255),
    symbol        varchar(255),
    updated_block bigint,
    contract      integer
        constraint uk_4mjr2gwj0w6ncvr6f0uq04s3q
            unique
        constraint fkimgaagi5vuont8s0ew6gxqqai
            references eth_contracts
);
alter table eth_tokens
    owner to hv_dev;
grant select on sequence eth_tokens_id_seq to grafana;
grant select on sequence eth_tokens_id_seq to backup;
create index idx_eth_tokens
    on eth_tokens (contract);
grant select on eth_tokens to grafana;
grant select on eth_tokens to backup;
alter sequence eth_tokens_id_seq owner to hv_dev;
alter sequence eth_tokens_id_seq owned by eth_tokens.id;
grant select on sequence eth_tokens_id_seq to grafana;
grant select on sequence eth_tokens_id_seq to backup;

create sequence eth_token_to_uni_pair_id_seq
    as integer;
create table eth_token_to_uni_pair
(
    id          serial
        primary key,
    block_start bigint,
    token_id    integer not null
        constraint fki8id42hiwjjwvtifm3ugjon33
            references eth_tokens,
    uni_pair_id integer not null
        constraint fkrulm04mmvh87yeprexxh1o5xj
            references eth_uni_pairs
);
alter table eth_token_to_uni_pair
    owner to hv_dev;
grant select on sequence eth_token_to_uni_pair_id_seq to grafana;
grant select on sequence eth_token_to_uni_pair_id_seq to backup;
grant select on eth_token_to_uni_pair to grafana;
grant select on eth_token_to_uni_pair to backup;
alter sequence eth_token_to_uni_pair_id_seq owner to hv_dev;
alter sequence eth_token_to_uni_pair_id_seq owned by eth_token_to_uni_pair.id;
grant select on sequence eth_token_to_uni_pair_id_seq to grafana;
grant select on sequence eth_token_to_uni_pair_id_seq to backup;

create sequence eth_vaults_id_seq
    as integer;
create table eth_vaults
(
    id              serial
        primary key,
    decimals        bigint,
    name            varchar(255),
    symbol          varchar(255),
    underlying_unit bigint,
    updated_block   bigint,
    contract        integer
        constraint uk_jcwcjqrfly836mv8b6i9p6wp6
            unique
        constraint fkbj9vb6wndcb0pr44uqwf1rfhw
            references eth_contracts,
    controller_id   integer
        constraint fk9unq8mb4dwbt600hj503vr8nj
            references eth_contracts,
    governance_id   integer
        constraint fksvfll9hksjiwqodxy2auwdhxv
            references eth_contracts,
    strategy_id     integer
        constraint fkohdsk6q4bk3dk52hvykj3533
            references eth_contracts,
    underlying_id   integer
        constraint fkh4gl719e2x448rqt9grxsdx34
            references eth_contracts
);
alter table eth_vaults
    owner to hv_dev;
grant select on sequence eth_vaults_id_seq to grafana;
grant select on sequence eth_vaults_id_seq to backup;
create index idx_eth_vaults
    on eth_vaults (contract);
grant select on eth_vaults to grafana;
grant select on eth_vaults to backup;
alter sequence eth_vaults_id_seq owner to hv_dev;
alter sequence eth_vaults_id_seq owned by eth_vaults.id;
grant select on sequence eth_vaults_id_seq to grafana;
grant select on sequence eth_vaults_id_seq to backup;

create table events_tx
(
    id            varchar(255) not null
        primary key,
    block         bigint,
    block_date    bigint,
    event         varchar(255),
    hash          varchar(255),
    info          text,
    mint_amount   double precision,
    new_strategy  varchar(255),
    old_strategy  varchar(255),
    vault         varchar(255),
    network       varchar(255),
    vault_address varchar(255)
);
alter table events_tx
    owner to hv_dev;
create index idx_events_network
    on events_tx (network);
create index idx_events_tx
    on events_tx (block_date);
grant select on events_tx to grafana;
grant select on events_tx to backup;

create table hard_work
(
    id                    varchar(255)     not null
        primary key,
    all_profit            double precision not null,
    apr                   double precision not null,
    block                 bigint           not null,
    block_date            bigint           not null,
    calls_quantity        integer          not null,
    eth_price             double precision not null,
    farm_buyback          double precision not null,
    farm_buyback_eth      double precision not null,
    farm_buyback_sum      double precision not null,
    farm_price            double precision not null,
    fee                   double precision not null,
    fee_eth               double precision not null,
    full_reward_usd       double precision not null,
    full_reward_usd_total double precision not null,
    gas_used              double precision not null,
    idle_time             bigint           not null,
    invested              double precision not null,
    investment_target     double precision not null,
    perc                  double precision not null,
    period_of_work        bigint,
    pool_users            integer          not null,
    ps_apr                double precision not null,
    ps_period_of_work     bigint,
    ps_tvl_usd            double precision not null,
    saved_gas_fees        double precision not null,
    saved_gas_fees_sum    double precision not null,
    share_change          double precision not null,
    tvl                   double precision,
    vault                 varchar(255),
    weekly_all_profit     double precision not null,
    weekly_average_tvl    double precision,
    weekly_profit         double precision not null,
    buy_back_rate         double precision,
    profit_sharing_rate   double precision,
    auto_stake            integer,
    network               varchar(255),
    vault_address         varchar(255)
);
alter table hard_work
    owner to hv_dev;
create index idx_hard_work
    on hard_work (block_date);
create index idx_hard_work_2
    on hard_work (full_reward_usd);
create index idx_hard_work_network
    on hard_work (network);
create index idx_hard_work_vault
    on hard_work (vault);
create index idx_hard_work_vault_address
    on hard_work (vault_address);
grant select on hard_work to grafana;
grant select on hard_work to backup;

create table harvest_tvl
(
    calculate_hash        varchar(255) not null
        primary key,
    calculate_time        bigint,
    last_all_owners_count integer      not null,
    last_owners_count     integer      not null,
    last_price            double precision,
    last_tvl              double precision,
    network               varchar(255)
);
alter table harvest_tvl
    owner to hv_dev;
create index idx_harvest_tvl
    on harvest_tvl (calculate_time);
grant select on harvest_tvl to grafana;
grant select on harvest_tvl to backup;

create table harvest_tx
(
    id                     varchar(255) not null
        primary key,
    all_owners_count       integer,
    all_pools_owners_count integer,
    amount                 double precision,
    amount_in              double precision,
    block                  bigint,
    block_date             bigint,
    confirmed              integer      not null,
    hash                   varchar(255),
    last_all_usd_tvl       double precision,
    last_gas               double precision,
    last_tvl               double precision,
    last_usd_tvl           double precision,
    lp_stat                text,
    method_name            varchar(255),
    migrated               boolean      not null,
    owner                  varchar(255),
    owner_balance          double precision,
    owner_balance_usd      double precision,
    owner_count            integer,
    prices                 text,
    profit                 double precision,
    profit_usd             double precision,
    share_price            double precision,
    total_amount           double precision,
    underlying_price       double precision,
    usd_amount             bigint,
    vault                  varchar(255),
    network                varchar(255),
    vault_address          varchar(255),
    underlying_address     varchar(255)
);
alter table harvest_tx
    owner to hv_dev;
create index idx_harvest_block_date
    on harvest_tx (block_date);
create index idx_harvest_method_name
    on harvest_tx (method_name);
create index idx_harvest_network
    on harvest_tx (network);
create index idx_harvest_tx
    on harvest_tx (block_date);
create index idx_harvest_tx2
    on harvest_tx (method_name, vault);
create index idx_harvest_vault
    on harvest_tx (vault);
create index idx_harvest_vault_address
    on harvest_tx (vault_address);
grant select on harvest_tx to grafana;
grant select on harvest_tx to backup;

create table income
(
    id             varchar(255)     not null
        primary key,
    amount         double precision not null,
    amount_sum     double precision not null,
    amount_sum_usd double precision not null,
    amount_usd     double precision not null,
    perc           double precision not null,
    ps_tvl         double precision not null,
    ps_tvl_usd     double precision not null,
    timestamp      bigint           not null,
    week_perc      double precision not null
);
alter table income
    owner to hv_dev;
create index idx_income
    on income (timestamp);
grant select on income to grafana;
grant select on income to backup;

create table layer_seq
(
    seq bigint not null
        primary key
);
alter table layer_seq
    owner to hv_dev;
grant select on layer_seq to grafana;
grant select on layer_seq to backup;

create table log_last
(
    network varchar(255) not null
        primary key,
    block   bigint
);
alter table log_last
    owner to hv_dev;
grant select on log_last to grafana;
grant select on log_last to backup;


create table prices
(
    id                  varchar(255) not null
        primary key,
    block               bigint,
    block_date          bigint,
    buy                 integer,
    lp_token0pooled     double precision,
    lp_token1pooled     double precision,
    lp_total_supply     double precision,
    other_token         varchar(255),
    other_token_amount  double precision,
    price               double precision,
    source              varchar(255),
    token               varchar(255),
    token_amount        double precision,
    network             varchar(255),
    other_token_address varchar(255),
    source_address      varchar(255),
    token_address       varchar(255),
    owner               varchar(255),
    recipient           varchar(255)
);
alter table prices
    owner to hv_dev;
create index idx_prices
    on prices (block);
create index idx_prices_network
    on prices (network);
create index idx_prices_source
    on prices (source);
create index idx_prices_source_address
    on prices (source_address);
create index prices_block_date_index
    on prices (block_date);
create index prices_network_index
    on prices (network);
create index prices_source_index
    on prices (source);
grant select on prices to grafana;
grant select on prices to backup;

create table rewards
(
    id               varchar(255)     not null
        primary key,
    apy              double precision not null,
    block            bigint           not null,
    block_date       bigint           not null,
    farm_balance     double precision not null,
    period_finish    bigint           not null,
    reward           double precision not null,
    tvl              double precision not null,
    vault            varchar(255),
    weekly_apy       double precision not null,
    network          varchar(255),
    is_weekly_reward integer,
    vault_address    varchar(255),
    pool_address     varchar(255)
);
alter table rewards
    owner to hv_dev;
create index idx_rewards
    on rewards (block_date);
create index idx_rewards_network
    on rewards (network);
create index idx_rewards_vault_address
    on rewards (vault_address);
grant select on rewards to grafana;
grant select on rewards to backup;


create table strat_info
(
    id                          varchar(255) not null
        primary key,
    apr                         double precision,
    apy                         double precision,
    block                       bigint       not null,
    block_date                  bigint,
    network                     varchar(255),
    percent_of_invested         double precision,
    percent_of_pool             double precision,
    platform                    varchar(255),
    pool_address                varchar(255),
    pool_balance                double precision,
    pool_extra_info1            varchar(255),
    pool_extra_info2            varchar(255),
    pool_extra_info3            varchar(255),
    pool_specific_underlying    varchar(255),
    pool_total_supply           double precision,
    reward_period               bigint,
    reward_tokens_raw           text,
    strategy_address            varchar(255),
    strategy_balance            double precision,
    strategy_balance_usd        double precision,
    strategy_created            bigint,
    strategy_created_date       bigint,
    strategy_name               varchar(255),
    strategy_underlying_address varchar(255),
    strategy_underlying_name    varchar(255),
    strategy_underlying_price   double precision,
    vault_address               varchar(255)
);
alter table strat_info
    owner to hv_dev;
create index idx_strat_info
    on strat_info (block);
create index idx_strat_info_network
    on strat_info (network);
create index idx_strat_info_source_vadr
    on strat_info (vault_address);
create index idx_strat_info_stadr
    on strat_info (strategy_address);
grant select on strat_info to grafana;
grant select on strat_info to backup;

create table transaction_last
(
    network varchar(255) not null
        primary key,
    block   bigint
);
alter table transaction_last
    owner to hv_dev;
grant select on transaction_last to grafana;
grant select on transaction_last to backup;


create table transfers
(
    id                varchar(255)     not null
        primary key,
    balance_owner     double precision not null,
    balance_recipient double precision not null,
    block             bigint           not null,
    block_date        bigint           not null,
    method_name       varchar(255),
    name              varchar(255),
    owner             varchar(255),
    price             double precision not null,
    profit            double precision,
    profit_usd        double precision,
    recipient         varchar(255),
    type              varchar(255),
    value             double precision not null,
    network           varchar(255),
    token_address     varchar(255)
);
alter table transfers
    owner to hv_dev;
create index idx_transfers_date
    on transfers (block_date);
create index idx_transfers_method_name
    on transfers (method_name);
create index idx_transfers_name
    on transfers (name);
create index idx_transfers_network
    on transfers (network);
create index idx_transfers_owner
    on transfers (owner);
create index idx_transfers_type
    on transfers (type);
grant select on transfers to grafana;
grant select on transfers to backup;

create table uni_tx
(
    id                 varchar(255)     not null
        primary key,
    amount             double precision not null,
    block              numeric(19, 2),
    block_date         bigint,
    coin               varchar(255),
    confirmed          boolean          not null,
    hash               varchar(255),
    last_gas           double precision,
    last_price         double precision,
    lp                 varchar(255),
    method_name        varchar(255),
    other_amount       double precision not null,
    other_coin         varchar(255),
    owner              varchar(255),
    owner_balance      double precision,
    owner_balance_usd  double precision,
    owner_count        integer,
    ps_income_usd      double precision,
    ps_week_apy        double precision,
    type               varchar(255),
    coin_address       varchar(255),
    lp_address         varchar(255),
    other_coin_address varchar(255)
);
alter table uni_tx
    owner to hv_dev;
create index idx_uni_block_date
    on uni_tx (block_date);
create index idx_uni_coin_address
    on uni_tx (coin_address);
create index idx_uni_owner
    on uni_tx (owner);
create index idx_uni_owner_balance_usd
    on uni_tx (owner_balance_usd);
create index idx_uni_tx
    on uni_tx (block_date);
grant select on uni_tx to grafana;
grant select on uni_tx to backup;


create table covalenthq_vault_tx
(
    id                 bigserial
        primary key,
    network             varchar(255),
    block              numeric(19, 2),
    transaction_hash         varchar(255),
    contract_decimal     numeric(19, 2),
    contract_address               varchar(255),
    owner_address                 varchar(255),
    value        numeric(19, 2),
    signed_at       timestamp,
    type         varchar(255)
);


