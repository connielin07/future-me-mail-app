import cron from "node-cron";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc.js";
import timezone from "dayjs/plugin/timezone.js";
import mysql from "mysql2/promise";
import nodemailer from "nodemailer";
import admin from "firebase-admin";
import dotenv from "dotenv";

// 載入 .env 環境變數
dotenv.config();

// 啟用 dayjs 時區支援
dayjs.extend(utc);
dayjs.extend(timezone);

// 台灣時區
const TAIPEI_TZ = "Asia/Taipei";

// MariaDB 連線池
const pool = mysql.createPool({
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || "futureme",
  password: process.env.DB_PASS || "",
  database: process.env.DB_NAME || "futureme",
  waitForConnections: true,
  connectionLimit: 10,
  timezone: "Z"
});

// 初始化 Firebase Admin（使用服務帳戶憑證）
if (!admin.apps.length) {
  try {
    admin.initializeApp({
      credential: admin.credential.applicationDefault()
    });
  } catch (err) {
    console.error("[scheduler] Failed to initialize Firebase Admin SDK:", err);
  }
}

const messaging = admin.messaging();

// 更新資料庫的已寄送狀態
async function markDelivered(id) {
  try {
    await pool.execute("UPDATE future_mail SET delivered = 1, delivered_at = CURRENT_TIMESTAMP WHERE id = ?", [id]);
  } catch (err) {
    console.error(`[scheduler] Failed to mark mail ${id} as delivered:`, err);
  }
}

// 建立 SMTP 連線（若未配置則回傳 null）
function createSmtpTransport() {
  if (!process.env.SMTP_HOST || !process.env.SMTP_USER || !process.env.SMTP_PASS) {
    console.warn("[scheduler] Missing SMTP configuration, skip email delivery.");
    return null;
  }

  return nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: Number(process.env.SMTP_PORT || 587),
    secure: process.env.SMTP_SECURE === "true",
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS
    }
  });
}

// 寄送 Email 通知
async function sendEmailNotification(mail) {
  if (!mail.email) {
    console.warn(`[scheduler] Mail ${mail.id} missing email address, skip email.`);
    return false;
  }

  const transporter = createSmtpTransport();

  if (!transporter) {
    return false;
  }

  const subject = `FutureMe 未來信件：「${mail.subject || "無主題"}」已抵達`;
  const plainBody = [
    "你好，這裡是 FutureMe 未來信",
    "",
    `主題：${mail.subject || "無主題"}`,
    `寄達日期：${mail.receiveDate}`,
    "",
    "內容：",
    mail.content || "",
    "",
    "祝一切順利！"
  ].join("\n");

  try {
    await transporter.sendMail({
      from: process.env.SMTP_FROM || process.env.SMTP_USER,
      to: mail.email,
      subject,
      text: plainBody
    });
    console.log(`[scheduler] Email notification sent for mail ${mail.id} to ${mail.email}.`);
    return true;
  } catch (err) {
    console.error(`[scheduler] Failed to send email for mail ${mail.id}:`, err);
    return false;
  }
}

// 發送 FCM 推播通知
async function sendPushNotification(mail) {
  if (!mail.deviceToken) {
    console.warn(`[scheduler] Mail ${mail.id} missing device token, skip push.`);
    return false;
  }

  if (!messaging) {
    console.warn("[scheduler] Firebase messaging not initialized, skip push.");
    return false;
  }

  const message = {
    token: mail.deviceToken,
    android: {
      priority: "HIGH"
    },
    data: {
      mailId: String(mail.id),
      receiveDate: String(mail.receiveDate || ""),
      subject: String(mail.subject || ""),
      email: String(mail.email || ""),
      title: "信件已送達！",
      body: mail.subject || "點擊查看你的未來來信"
    }
  };

  try {
    await messaging.send(message);
    console.log(`[scheduler] Push notification sent for mail ${mail.id}.`);
    return true;
  } catch (err) {
    console.error(`[scheduler] Failed to send push for mail ${mail.id}:`, err);
    return false;
  }
}

// 每日寄送流程
async function handleDailyDelivery() {
  const today = dayjs().tz(TAIPEI_TZ).format("YYYY-MM-DD");
  console.log(`[scheduler] Running delivery check for ${today} (UTC${dayjs().tz(TAIPEI_TZ).format("Z")})`);

  try {
    const [rows] = await pool.query(
      `SELECT id, subject, email, content, device_token AS deviceToken, receive_date AS receiveDate
       FROM future_mail
       WHERE receive_date = ? AND COALESCE(delivered, 0) = 0`,
      [today]
    );

    if (rows.length === 0) {
      console.log("[scheduler] No mails to deliver today.");
      return;
    }

    for (const mail of rows) {
      console.log(`[scheduler] Mail ${mail.id} scheduled for delivery. Subject="${mail.subject}" Email="${mail.email || "N/A"}"`);
      const [pushSent, emailSent] = await Promise.all([
        sendPushNotification(mail),
        sendEmailNotification(mail)
      ]);

      // 只要其中一種成功就標記為已寄送
      if (pushSent || emailSent) {
        await markDelivered(mail.id);
      } else {
        console.warn(`[scheduler] Mail ${mail.id} delivery skipped because both push/email failed.`);
      }
    }
  } catch (err) {
    console.error("[scheduler] Failed to query mails for delivery:", err);
  }
}

// 依排程執行（目前每分鐘一次）
cron.schedule(
  "* * * * *",
  () => {
    handleDailyDelivery().catch((err) => console.error("[scheduler] Unexpected failure:", err));
  },
  {
    timezone: TAIPEI_TZ
  }
);

console.log("[scheduler] Daily delivery scheduler started, waiting for next 09:00 Asia/Taipei run.");
