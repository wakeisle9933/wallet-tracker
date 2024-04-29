--DROP TABLE wallet_history;
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
      average_price REAL,
      contract_address TEXT,
      created_date TEXT
);