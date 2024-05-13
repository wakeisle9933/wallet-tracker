--DROP TABLE wallet_history;
--DROP TABLE wallet_history_result;
--DROP TABLE whitelist_token;
--DROP TABLE blacklist_token;
CREATE TABLE IF NOT EXISTS wallet_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
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

--DROP TABLE blacklist_token;
CREATE TABLE IF NOT EXISTS blacklist_token
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chain TEXT NOT NULL,
    name TEXT NOT NULL,
    contract_address TEXT NOT NULL,
    created_date TEXT
);
