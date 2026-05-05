import {initializeApp} from "firebase-admin/app";
import {getAuth} from "firebase-admin/auth";
import {setGlobalOptions} from "firebase-functions";
import {onRequest} from "firebase-functions/https";
import {defineSecret} from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import {Resend} from "resend";

initializeApp();

setGlobalOptions({maxInstances: 10});

const resendApiKey = defineSecret("RESEND_API_KEY");

type SendLinkRequest = {
  email?: string;
  fullName?: string;
};

const allowedOrigins = [
  "http://localhost",
  "http://127.0.0.1",
];

function setCorsHeaders(origin: string | undefined, response: any) {
  if (!origin) {
    response.set("Access-Control-Allow-Origin", "*");
  } else if (allowedOrigins.some((prefix) => origin.startsWith(prefix))) {
    response.set("Access-Control-Allow-Origin", origin);
  }
  response.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  response.set("Access-Control-Allow-Headers", "Content-Type");
}

export const sendSignInLinkEmail = onRequest(
  {secrets: [resendApiKey]},
  async (request, response) => {
    setCorsHeaders(request.headers.origin, response);

    if (request.method === "OPTIONS") {
      response.status(204).send("");
      return;
    }

    if (request.method !== "POST") {
      response.status(405).json({error: "Method not allowed"});
      return;
    }

    const body = request.body as SendLinkRequest;
    const email = body.email?.trim().toLowerCase();
    const fullName = body.fullName?.trim();

    if (!email) {
      response.status(400).json({error: "Email is required"});
      return;
    }

    try {
      const actionCodeSettings = {
        url: "https://sawa-6e057.web.app/emailSignIn",
        handleCodeInApp: true,
        android: {
          packageName: "com.example.sava",
          installApp: true,
        },
      };

      const link = await getAuth().generateSignInWithEmailLink(
        email,
        actionCodeSettings
      );

      const resend = new Resend(resendApiKey.value());
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
              <a
                href="${link}"
                style="background:#1f2937;color:#ffffff;padding:12px 20px;border-radius:8px;text-decoration:none;display:inline-block;"
              >
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

      response.status(200).json({success: true});
    } catch (error) {
      logger.error("Failed to send sign-in link email", error);
      response.status(500).json({error: "Could not send sign-in link email"});
    }
  }
);
