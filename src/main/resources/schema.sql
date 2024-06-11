--DROP TABLE IF EXISTS wallet_history;
--DROP TABLE IF EXISTS wallet_history_result;
--DROP TABLE IF EXISTS whitelist_token;
--DROP TABLE IF EXISTS blacklist_token;
--DROP TABLE IF EXISTS tracking_address;
--DROP TABLE IF EXISTS chain_mapping;
CREATE TABLE IF NOT EXISTS wallet_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      chain TEXT NOT NULL,
      address TEXT NOT NULL,
      nickname TEXT NOT NULL,
      status TEXT NOT NULL,
      currency TEXT NOT NULL,
      previous_balance REAL,
      trade_volume REAL,
      price TEXT,
      usd_value TEXT,
      total_balance REAL,
      average_price TEXT,
      contract_address TEXT,
      created_date TEXT
);

CREATE TABLE IF NOT EXISTS wallet_history_result (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      chain TEXT NOT NULL,
      address TEXT NOT NULL,
      nickname TEXT NOT NULL,
      currency TEXT NOT NULL,
      contract_address TEXT NOT NULL,
      profit_or_loss TEXT NOT NULL,
      total_investment TEXT,
      total_profit TEXT,
      result TEXT,
      created_date TEXT
);

CREATE TABLE IF NOT EXISTS whitelist_token
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chain TEXT NOT NULL,
    name TEXT NOT NULL,
    contract_address TEXT NOT NULL,
    created_date TEXT
);

CREATE TABLE IF NOT EXISTS blacklist_token
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chain TEXT NOT NULL,
    name TEXT NOT NULL,
    contract_address TEXT NOT NULL,
    created_date TEXT
);

CREATE TABLE IF NOT EXISTS tracking_address
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chain TEXT NOT NULL,
    address TEXT NOT NULL,
    nickname TEXT NOT NULL,
    description TEXT,
    created_date TEXT
);

CREATE TABLE IF NOT EXISTS chain_mapping
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    dextools_chain_id TEXT NOT NULL,
    moralis_chain_id TEXT NOT NULL,
    token_sniffer_id TEXT NOT NULL,
    block_explorer TEXT NOT NULL,
    dex_explorer TEXT NOT NULL
);
