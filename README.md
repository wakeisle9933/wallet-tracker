
<img width="651" alt="preview1" src="https://github.com/wakeisle9933/wallet-tracker/assets/73478472/695d8463-0bc9-459d-899b-5cd18d2c657b">
<img width="662" alt="preview2" src="https://github.com/wakeisle9933/wallet-tracker/assets/73478472/501035fc-70b7-49a3-88e5-88fc56cb5761">
<img width="662" alt="preview3" src="https://github.com/wakeisle9933/wallet-tracker/assets/73478472/aa361fc5-a243-4ac6-8b94-78fdba0e7eab">


# **🔍 Wallet Tracker by My Codegate**

**✨Features**

**🆓 Track your portfolio and balances for free without the need for paid APIs!**  
**🎁 Don't miss a single airdrop!**  
**📧 Receive notifications every 5 minutes via email!**  
**🛑 Automatically blocks new scams, keeping you safe from the threat of fraud!**  
**📊 Send email a report after tracking account finish a transaction!**  
**🧑‍🚀 Manage various features easily with Postman! It's already set up, so there's no need for additional configuration!**  
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
