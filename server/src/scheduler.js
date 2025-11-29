import cron from "node-cron";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc.js";
import timezone from "dayjs/plugin/timezone.js";
import mysql from "mysql2/promise";
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

async function handleDailyDelivery() {
  const today = dayjs().tz(TAIPEI_TZ).format("YYYY-MM-DD");
  console.log(`[scheduler] Running delivery check for ${today} (UTC${dayjs().tz(TAIPEI_TZ).format("Z")})`);

  try {
    const [rows] = await pool.query(
      `SELECT id, subject, email, content, receive_date AS receiveDate
       FROM future_mail
       WHERE receive_date = ?`,
      [today]
    );

    if (rows.length === 0) {
      console.log("[scheduler] No mails to deliver today.");
      return;
    }

    rows.forEach((mail) => {
      console.log(`[scheduler] Mail ${mail.id} scheduled for delivery. Subject="${mail.subject}" Email="${mail.email || "N/A"}"`);
      // TODO: Integrate actual push/email sending logic here (e.g., FCM, email queuing, etc.).
    });
  } catch (err) {
    console.error("[scheduler] Failed to query mails for delivery:", err);
  }
}

cron.schedule(
  "0 9 * * *",
  () => {
    handleDailyDelivery().catch((err) => console.error("[scheduler] Unexpected failure:", err));
  },
  {
    timezone: TAIPEI_TZ
  }
);

console.log("[scheduler] Daily delivery scheduler started, waiting for next 09:00 Asia/Taipei run.");
