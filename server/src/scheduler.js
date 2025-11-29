import cron from "node-cron";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc.js";
import timezone from "dayjs/plugin/timezone.js";
import mysql from "mysql2/promise";
import admin from "firebase-admin";
import dotenv from "dotenv";

dotenv.config();

dayjs.extend(utc);
dayjs.extend(timezone);

const TAIPEI_TZ = "Asia/Taipei";

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

async function markDelivered(id) {
  try {
    await pool.execute("UPDATE future_mail SET delivered = 1, delivered_at = CURRENT_TIMESTAMP WHERE id = ?", [id]);
  } catch (err) {
    console.error(`[scheduler] Failed to mark mail ${id} as delivered:`, err);
  }
}

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
    notification: {
      title: "信件已送達！",
      body: mail.subject || "點擊查看你的未來來信"
    },
    data: {
      mailId: mail.id,
      receiveDate: mail.receiveDate
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
      const sent = await sendPushNotification(mail);
      if (sent) {
        await markDelivered(mail.id);
      }
    }
  } catch (err) {
    console.error("[scheduler] Failed to query mails for delivery:", err);
  }
}

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
