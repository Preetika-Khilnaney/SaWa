const admin = require("firebase-admin");
const {Resend} = require("resend");

function initializeFirebaseAdmin() {
  if (admin.apps.length > 0) {
    return;
  }

  const privateKey = process.env.FIREBASE_PRIVATE_KEY
    ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n")
    : undefined;

  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey,
    }),
  });
}

function setCorsHeaders(req, res) {
  const origin = req.headers.origin;
  if (!origin) {
    res.setHeader("Access-Control-Allow-Origin", "*");
  } else if (
    origin.startsWith("http://localhost") ||
    origin.startsWith("http://127.0.0.1")
  ) {
    res.setHeader("Access-Control-Allow-Origin", origin);
  }
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
}

module.exports = async (req, res) => {
  setCorsHeaders(req, res);

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  if (req.method !== "POST") {
    res.status(405).json({error: "Method not allowed"});
    return;
  }

  const email = req.body?.email?.trim()?.toLowerCase();
  const fullName = req.body?.fullName?.trim();

  if (!email) {
    res.status(400).json({error: "Email is required"});
    return;
  }

  try {
    initializeFirebaseAdmin();

    const link = await admin.auth().generateSignInWithEmailLink(email, {
      url: "https://sawa-6e057.web.app/emailSignIn",
      handleCodeInApp: true,
      android: {
        packageName: "com.example.sava",
        installApp: true,
      },
    });

    const resend = new Resend(process.env.RESEND_API_KEY);
    const displayName = fullName && fullName.length > 0 ? fullName : "there";

    await resend.emails.send({
      from: "SaWa <support@sawa.org.in>",
      replyTo: "support@sawa.org.in",
      to: [email],
      subject: "Verify your email for SaWa",
      html: `
        <div style="font-family: Arial, sans-serif; color: #1e1e1e; line-height: 1.6;">
          <p>Hi ${displayName},</p>
          <p>Tap the button below to verify your email address for SaWa and continue creating your profile.</p>
          <p style="margin: 24px 0;">
            <a href="${link}" style="background:#1f2937;color:#ffffff;padding:12px 20px;border-radius:8px;text-decoration:none;display:inline-block;">
              Verify email
            </a>
          </p>
          <p>If the button does not work, copy and paste this link into your browser:</p>
          <p><a href="${link}">${link}</a></p>
          <p>If you did not request this email, you can safely ignore it.</p>
          <p>Thanks,<br />The SaWa team</p>
        </div>
      `,
    });

    res.status(200).json({success: true});
  } catch (error) {
    console.error("Failed to send sign-in link email", error);
    res.status(500).json({
      error: "Could not send sign-in link email",
      details: error && error.message ? error.message : String(error),
    });
  }
};
