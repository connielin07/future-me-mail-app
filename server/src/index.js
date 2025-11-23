import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import nodemailer from "nodemailer";
import crypto from "crypto";
import mysql from "mysql2/promise";

dotenv.config();

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());

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

app.get("/health", (_, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

app.post("/api/future-mails", async (req, res) => {
  const { writeDate, receiveDate, subject, content, email } = req.body || {};

  if (!writeDate || !receiveDate || !subject || !content) {
    return res.status(400).json({ message: "Missing required fields." });
  }

  const entry = {
    id: crypto.randomUUID(),
    writeDate,
    receiveDate,
    subject,
    content,
    email: email || null,
    createdAt: new Date().toISOString()
  };

  try {
    await pool.execute(
      `INSERT INTO future_mail (id, write_date, receive_date, subject, content, email, created_at)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [
        entry.id,
        entry.writeDate,
        entry.receiveDate,
        entry.subject,
        entry.content,
        entry.email,
        entry.createdAt
      ]
    );
  } catch (dbErr) {
    console.error("Failed to save future mail:", dbErr);
    return res.status(500).json({ message: "Failed to save future mail." });
  }

  if (process.env.SMTP_HOST && process.env.SMTP_USER && email) {
    try {
      const transporter = nodemailer.createTransport({
        host: process.env.SMTP_HOST,
        port: Number(process.env.SMTP_PORT || 587),
        secure: process.env.SMTP_SECURE === "true",
        auth: {
          user: process.env.SMTP_USER,
          pass: process.env.SMTP_PASS
        }
      });

      await transporter.verify();
    } catch (err) {
      console.warn("SMTP verify failed:", err.message);
    }
  }

  res.status(201).json({ id: entry.id, status: "ok" });
});

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ message: "Internal server error" });
});

app.listen(PORT, () => {
  console.log(`FutureMe backend listening on port ${PORT}`);
});
