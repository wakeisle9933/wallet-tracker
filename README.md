
<img width="464" alt="intro-0423" src="https://github.com/wakeisle9933/wallet-tracker/assets/73478472/2a14c0b8-1656-4d3b-b3d1-a4647193867b">
<img width="464" alt="intro2-0423" src="https://github.com/wakeisle9933/wallet-tracker/assets/73478472/f1ce24a8-d951-4491-8e62-70c022c91ee1">

# **🔍 Wallet Tracker by My Codegate**

**✨Features**

**🆓 Track your portfolio and balances for free without the need for paid APIs!**  
**📧 Receive notifications every 10 minutes via email!**  
**🧑‍🚀 Manage various features easily with Postman! It's already set up, so there's no need for additional configuration!**  
**🛑 Automatically blocks new scams, keeping you safe from the threat of fraud!**  
**🐋 Dockerfile included for easy setup!**  

For the full development log, please refer to the following link. [Development Log](https://mycodegate.com/1100/)

1. Insert your email/password/moralis.api an in `application.properties` (The password should be an app password from your Google account).  
   [Moralis : Create API for free!](https://moralis.io/api/)
2. Place the Address file in the `resources/base` directory and enter the data in a wallet_address Nickname format, e.g., `0x... Nickname`.
3. Place the Emails file in the `resources/email` directory and enter the email addresses to which you want to send notifications.
4. Easily monitor changes to your wallet via email !

# 🚀 Running with Docker
**🐳 This project provides a Dockerfile for easy deployment. 🛠️**
1. Build the Docker image  
   ```docker build -t wallet-tracker .```
3. Run the tracker in detached mode  
   ```docker run -d -p 9000:8080 --name wallet-app wallet-tracker```
4. (Optional) Run the tracker with automatic restarts  
   ```docker run -d --restart unless-stopped -p 9000:8080 --name wallet-app wallet-tracker```
